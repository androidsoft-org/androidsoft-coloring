package org.androidsoft.coloring.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import eu.quelltext.images.ArrayMapper;
import eu.quelltext.images.ClusteredColors;
import eu.quelltext.images.ConnectedComponents;
import eu.quelltext.images.FastMaximumShiftFilter;
import eu.quelltext.images.KMeansOnRGBColors;
import eu.quelltext.images.LineasAroundAreas;
import eu.quelltext.images.Measurement;
import eu.quelltext.images.RandomColorGenerator;

// see https://developer.android.com/reference/java/lang/Thread
public class ImageProcessing implements Runnable {

    private static final int NUMBER_OF_COLORS = 9;
    private static final int LINE_WIDTH = 7;
    private static final int MAX_SHIFT_KERNEL_DIAMETER = 10;
    private static final int COLOR_LINE = Color.BLACK;
    private static final int COLOR_BACKGROUND = Color.WHITE;

    // at least LINE_WIDTH  wide between the lines so one can actually touch it
    private static final int MINIMUM_AREA = 2 * (LINE_WIDTH * 2 * LINE_WIDTH * 2);

    private int width;
    private int height;
    private ClusteredColors cluster;
    private int[] colors;

    public interface ImagePreview {
        void setImage(Bitmap image);
        double getWidth();
        double getHeight();
        InputStream openInputStream(Uri uri) throws FileNotFoundException;
        void done(Bitmap bitmap);
    }

    private final LoadImageProgress progress;
    private final ImagePreview imagePreview;
    private final Uri imageUri;

    public ImageProcessing(Uri imageUri, LoadImageProgress progress, ImagePreview imagePreview) {
        this.imageUri = imageUri;
        this.progress = progress;
        this.imagePreview = imagePreview;
    }

    @Override
    public void run() {
        // create a preview image
        try {
            progress.stepInputPreview();
            Bitmap image = getThumbnail(imageUri, imagePreview.getWidth(), imagePreview.getHeight());
            width = image.getWidth();
            height = image.getHeight();
            colors = getPixels(image);
            showImage(colors);
        } catch (IOException e) {
            e.printStackTrace();
            progress.stepFail();
            return;
        }
        // run cluster the colors used in the image
        try {
            classifyColors();
        } catch (Exception e) {
            e.printStackTrace();
            progress.stepFail();
            return;
        }
        removeSmallAreasByKernel();
        removeSmallAreasByConnectedComponents();
        drawLinesAroundTheAreas();
        progress.stepDone();
        imagePreview.done(getBitmap(colors));
    }

    private void drawLinesAroundTheAreas() {
        progress.stepDrawLinesAround();
        LineasAroundAreas areaLines = new LineasAroundAreas(colors, width, height);
        colors = areaLines.draw(LINE_WIDTH, COLOR_BACKGROUND, COLOR_LINE);
        showImage(colors);
    }

    private void removeSmallAreasByConnectedComponents() {
        progress.stepConnectingComponents();
        ConnectedComponents connectedComponents = new ConnectedComponents(colors, width, height);
        ConnectedComponents.Result result = connectedComponents.compute();
        progress.stepMeasuringAreas();
        Measurement measurement = result.computeMeasurement();
        // remove areas until a certain size is reached
        while (measurement.getSmallestComponentSize() < MINIMUM_AREA) {
            measurement.mergeSmallestAreaIntoItsBiggestNeighbor();
        }
        int[] components = measurement.computeArea();
        int[] areaColors = new RandomColorGenerator().bright(measurement.getNumberOfComponents());
        colors = ArrayMapper.mapFrom(components, areaColors).getArray();
        progress.stepShowComponents();
        showImage(colors);

    }

    private void removeSmallAreasByKernel() {
        progress.stepRemovingNoise();
        // TODO: speed up the algorithm by using the function getArrayWithClusterIds()
        //       and an array counter
        FastMaximumShiftFilter filter = new FastMaximumShiftFilter(cluster.getClassifiedColors(), width, height);
        colors = filter.compute(MAX_SHIFT_KERNEL_DIAMETER);
        progress.stepShowSmoothedImage();
        showImage(colors);
    }

    public static int[] getPixels(Bitmap bitmap) {
        int[] pixels = new int[bitmap.getHeight() * bitmap.getWidth()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        return pixels;
    }

    private void classifyColors() throws Exception {
        progress.stepPreparingClustering();
        KMeansOnRGBColors kmeans = new KMeansOnRGBColors(colors, NUMBER_OF_COLORS);

        progress.stepSampleDataForClassification();
        kmeans.step01RandomSampling();

        progress.stepClusteringData();
        kmeans.step02clustering();

        progress.stepCreateClusterImage();
        cluster = kmeans.step03ClassifyData();
        progress.stepShowClusterImage();
        showImage(cluster.getClassifiedColors());
    }

    private void showImage(int[] colors) {
        imagePreview.setImage(getBitmap(colors));
    }

    private Bitmap getBitmap(int[] colors) {
        // build a colored bitmap from the classification
        // see https://stackoverflow.com/a/10180908/1320237
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(colors, 0, width, 0, 0, width, height);
        return bitmap;
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
