package org.androidsoft.coloring.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

// see https://developer.android.com/reference/java/lang/Thread
public class ImageProcessing implements Runnable {

    public interface ImagePreview {
        void setImage(Bitmap image);
        double getWidth();
        double getHeight();
        InputStream openInputStream(Uri uri) throws FileNotFoundException;
        void done(Bitmap bitmap);
    }

    protected final LoadImageProgress progress;
    protected final ImagePreview imagePreview;
    protected final Uri imageUri;
    protected int width;
    protected int height;

    public ImageProcessing(Uri imageUri, LoadImageProgress progress, ImagePreview imagePreview) {
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
        width = image.getWidth();
        height = image.getHeight();
        runWithBitmap(image);
    }

    protected void runWithBitmap(Bitmap image) {

    }

    private Bitmap getThumbnail(Uri uri, double maxWidth, double maxHeight) throws IOException {
        // from https://stackoverflow.com/a/6228188/1320237
        InputStream input = imagePreview.openInputStream(uri);

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
        input = imagePreview.openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }
}
