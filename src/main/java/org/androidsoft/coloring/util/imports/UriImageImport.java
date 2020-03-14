package org.androidsoft.coloring.util.imports;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;
import org.androidsoft.coloring.util.cache.Cache;
import org.androidsoft.coloring.util.cache.NullCache;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

// see https://developer.android.com/reference/java/lang/Thread
public class UriImageImport implements Runnable {

    private byte[] rawBytesFromTheSource = null;

    protected final LoadImageProgress progress;
    protected final ImagePreview imagePreview;
    protected final Uri imageUri;
    protected int width;
    protected int height;
    private Thread thread = null;
    private Cache cache = new NullCache();

    public UriImageImport(Uri imageUri, LoadImageProgress progress, ImagePreview imagePreview) {
        this.imageUri = imageUri;
        this.progress = progress;
        this.imagePreview = imagePreview;
    }

    @Override
    public void run() {
        // create a preview image
        Bitmap image;
        try {
            progress.stepInputPreview();
            image = getThumbnail(imageUri, imagePreview.getWidth(), imagePreview.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
            progress.stepFail();
            return;
        }
        rawBytesFromTheSource = null; // clean up
        // set attributes
        width = image.getWidth();
        height = image.getHeight();
        runWithBitmap(image);
    }

    protected void runWithBitmap(Bitmap image) {
        progress.stepDone();
        imagePreview.done(image);
    }

    private Bitmap getThumbnail(Uri uri, double maxWidth, double maxHeight) throws IOException {
        InputStream input = getInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig= Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        if ((maxHeight < maxWidth) != (onlyBoundsOptions.outHeight < onlyBoundsOptions.outWidth)) {
            // fit the image in the direction it is biggest
            double t = maxWidth;
            maxWidth = maxHeight;
            maxHeight = t;
        }

        double imageRatio = (double)onlyBoundsOptions.outWidth / (double)onlyBoundsOptions.outHeight;
        double ratio;
        if (maxWidth / maxHeight < imageRatio) {
            ratio = (double)onlyBoundsOptions.outWidth / maxWidth;
        } else {
            ratio = (double)onlyBoundsOptions.outHeight / maxHeight;
        }

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true; //optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//
        input = getInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private InputStream getInputStream(Uri uri) throws IOException {
        if (rawBytesFromTheSource != null) {
            return new ByteArrayInputStream(rawBytesFromTheSource);
        }
        if (uri.getScheme().startsWith("http")) {
            // download file, see https://stackoverflow.com/a/51271706/1320237
            InputStream stream = cache.openStreamIfAvailable(new URL(uri.toString()));
            rawBytesFromTheSource = IOUtils.toByteArray(stream);
            return new ByteArrayInputStream(rawBytesFromTheSource);
        } else {
            // from https://stackoverflow.com/a/6228188/1320237
            return imagePreview.openInputStream(uri);
        }
    }

    private int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }

    public synchronized void start() {
        if (thread == null) {
            thread = new Thread(this);
        }
        thread.start();
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public void startWith(Cache cache) {
        setCache(cache);
        start();
    }
}
