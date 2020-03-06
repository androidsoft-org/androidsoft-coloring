package org.androidsoft.coloring.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.ManhattanDistance;
import weka.core.logging.ConsoleLogger;

// see https://developer.android.com/reference/java/lang/Thread
public class ImageProcessing implements Runnable {

    private static final int NUMBER_OF_COLORS = 9;
    private static final int LINE_WIDTH = 10;
    private static final int AREA_RADIUS = 10;

    private static final int LINE_WIDTH_HALF = LINE_WIDTH / 2 + LINE_WIDTH % 2;
    private static final int LINE_WIDTH_HALF_SQUARED = LINE_WIDTH_HALF * LINE_WIDTH_HALF;
    private static final int NOT_TRANSPARENT = 0xff000000;

    private static final int COLOR_LINE = Color.BLACK;
    private static final int COLOR_BACKGROUND = Color.WHITE;

    private Attribute red;
    private Attribute green;
    private Attribute blue;
    private Bitmap previewThumb;
    private Bitmap classifiedColors;
    private int[][] centroids;
    private int[][] classifiedPixels;
    private Bitmap smoothedColors;
    private int[] centroidColors;
    private int[][] smoothedPixels;
    private Bitmap lineImage;

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
            previewThumb = getThumbnail(imageUri, imagePreview.getWidth(), imagePreview.getHeight());
            imagePreview.setImage(previewThumb);
        } catch (IOException e) {
            e.printStackTrace();
            progress.stepFail();
            return;
        }
        // run cluster the colors used in the image
        try {
            classifyColors();
            imagePreview.setImage(classifiedColors);

        } catch (Exception e) {
            e.printStackTrace();
            progress.stepFail();
            return;
        }
        removeSmallAreas();
        imagePreview.setImage(smoothedColors);
        drawLinesAroundTheAreas();
        imagePreview.setImage(lineImage);
        progress.stepDone();
        imagePreview.done(lineImage);
    }

    private void drawLinesAroundTheAreas() {
        progress.stepDrawLinesAround();
        int width = smoothedPixels.length;
        int height = smoothedPixels[0].length;

        lineImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int maxX = width -1;
        int maxY = height - 1;
        for (int x = 0; x < maxX - 1; x++) {
            for (int y = 0; y < maxY - 1; y++) {
                int cls = smoothedPixels[x][y];
                if (cls != smoothedPixels[x+1][y] || cls != smoothedPixels[x][y+1]) {
                    // we are at a border - draw a circle!
                    for (int dx = -LINE_WIDTH_HALF; dx <= LINE_WIDTH_HALF; dx++) {
                        for (int dy = -LINE_WIDTH_HALF; dy <= LINE_WIDTH_HALF; dy++) {
                            // check that we paint a dot
                            if (dx*dx + dy*dy > LINE_WIDTH_HALF_SQUARED) {
                                continue;
                            }
                            // check bounds
                            int kx = x + dx;
                            if (kx < 0 || kx >= width) {
                                continue;
                            }
                            int ky = y + dy;
                            if (ky < 0 || ky >= height) {
                                continue;
                            }
                            lineImage.setPixel(kx,ky, COLOR_LINE);
                        }
                    }
                } else if (lineImage.getPixel(x, y) != COLOR_LINE) {
                    lineImage.setPixel(x, y, COLOR_BACKGROUND);
                }
            }
        }
    }

    private void removeSmallAreas() {
        progress.stepRemovingNoise();

        int width = classifiedPixels.length;
        int height = classifiedPixels[0].length;

        smoothedColors = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        smoothedPixels = new int[width][height];

        int resultColorsIndex = 0;
        int[] resultColors = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] surroundingCls = new int[NUMBER_OF_COLORS];
                int kx_min = Math.max(x - AREA_RADIUS, 0);
                int ky_min = Math.max(y - AREA_RADIUS, 0);
                int kx_max = Math.min(x + AREA_RADIUS + 1, width);
                int ky_max = Math.min(y + AREA_RADIUS + 1, height);
                // use a kernel like this
                // +--+--+
                // |  |  |
                // +--+--+
                // |  |  |
                // +--+--+
                for (int ky = ky_min; ky < ky_max; ky++) {
                    if (ky_min <= ky && ky < ky_max) {
                        for (int kx = kx_min; kx < kx_max; kx += AREA_RADIUS) {
                            int cls = classifiedPixels[kx][ky];
                            surroundingCls[cls]++;
                        }
                    }
                }
                for (int kx = kx_min; kx < kx_max; kx++) {
                    if (kx_min <= kx && kx < kx_max) {
                        for (int ky = ky_min; ky < ky_max; ky += AREA_RADIUS) {
                            int cls = classifiedPixels[kx][ky];
                            surroundingCls[cls]++;
                        }
                    }
                }
                int maxValue = surroundingCls[0];
                int bestClass = 0;
                for (int i = 1; i < NUMBER_OF_COLORS; i++) {
                    if (maxValue < surroundingCls[i]) {
                        maxValue = surroundingCls[i];
                        bestClass = i;
                    }
                }
                smoothedPixels[x][y] = bestClass;
                int color = centroidColors[bestClass];
                resultColors[resultColorsIndex] = color;
                resultColorsIndex++;
            }
        }
        progress.stepShowSmoothedImage();
        smoothedColors.setPixels(resultColors, 0, width, 0, 0, width, height);

    }

    private void classifyColors() throws Exception {
        progress.stepPreparingClustering();
        // for an example run, see
        // see https://www.programcreek.com/2014/02/k-means-clustering-in-java/
        // for SimpleKMeans
        // see https://weka.sourceforge.io/doc.dev/weka/clusterers/SimpleKMeans.html
        SimpleKMeans kmeans = new SimpleKMeans();
        // see https://weka.sourceforge.io/doc.dev/weka/core/DistanceFunction.html
        kmeans.setDistanceFunction(new ManhattanDistance()); // manhattan should speed things up
        kmeans.setPreserveInstancesOrder(false);
        kmeans.setFastDistanceCalc(true);
        kmeans.setNumClusters(NUMBER_OF_COLORS);
        // computing the number of colors to get from the image
        // see https://medium.com/@equipintelligence/java-algorithms-the-k-nearest-neighbor-classifier-4faca7ad26b2
        //     in the Section "Determining the value of K"
        // formula: K = sqrt ( number of samples in dataset ) / 2
        int capacity = 4 * NUMBER_OF_COLORS * NUMBER_OF_COLORS;
        // for attributes
        // see https://weka.sourceforge.io/doc.dev/weka/core/Attribute.html
        ArrayList<Attribute> attributes = new ArrayList<>(3);
        red = new Attribute("red");
        attributes.add(red);
        green = new Attribute("green");
        attributes.add(green);
        blue = new Attribute("blue");
        attributes.add(blue);

        progress.stepSampleDataForClassification();
        Instances data = new Instances("colors", attributes, capacity);
        // build the data set from randomly sampled data
        Random random = new Random(0xffaa123678234232l);
        int width = previewThumb.getWidth();
        int height = previewThumb.getHeight();
        for (int i = 0; i < capacity; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            Instance pixel = getThumbPixelInstanceAt(x, y);
            data.add(pixel);
        }
        progress.stepClusteringData();
        // compute k-means
        kmeans.buildClusterer(data);
        Instances centroidInstances = kmeans.getClusterCentroids();
        centroids = new int[NUMBER_OF_COLORS][3];
        centroidColors = new int[NUMBER_OF_COLORS];
        for (int i = 0; i < NUMBER_OF_COLORS; i++) {
            Instance centroid = centroidInstances.size() > i ?
                    centroidInstances.get(i) : centroidInstances.firstInstance();
            int[] color = getColorOf(centroid);
            centroids[i] = color;
            centroidColors[i] = NOT_TRANSPARENT | color[0] << 16 | color[1] << 8 | color[2];
        }

        progress.stepCreateClusterImage();
        // build a colored bitmap from the classification
        // see https://stackoverflow.com/a/10180908/1320237
        classifiedColors = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] resultColors = new int[width * height];
        int resultColorsIndex = 0;
        classifiedPixels = new int[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int classification = classifyThumbPixel(x, y);
                classifiedPixels[x][y] = classification;
                int color = centroidColors[classification];
                resultColors[resultColorsIndex] = color;
                resultColorsIndex++;
            }
        }
        progress.stepShowClusterImage();
        classifiedColors.setPixels(resultColors, 0, width, 0, 0, width, height);
    }

    private int classifyThumbPixel(int x, int y) {
        int color = previewThumb.getPixel(x, y);
        int r = (color >> 16) & 0xff;
        int g = (color >>  8) & 0xff;
        int b = (color      ) & 0xff;
        int minDistance = 0xffff;
        int minCentroid = -1;
        for (int i = 0; i < NUMBER_OF_COLORS; i++) {
            int[] centroid = centroids[i];
            int distanceToCentroid = // manhattan distance
                    Math.abs(r - centroid[0]) +
                    Math.abs(g - centroid[1]) +
                    Math.abs(b - centroid[2]);
            if (minDistance > distanceToCentroid) {
                minCentroid = i;
                minDistance = distanceToCentroid;
            }
        }
        if (minCentroid == -1) {
            throw new AssertionError("A centroid should have been chosen.");
        }
        return minCentroid;
    }

    private int[] getColorOf(Instance centroid) {
        int[] color = new int[3];
        for (int a = 0; a < centroid.numAttributes(); a++) {
            Attribute attribute = centroid.attribute(a);
            int value = (int)Math.round(centroid.value(a)) & 0xff;
            if (attribute.equals(red)) {
                color[0] = value;
            } else if (attribute.equals(green)) {
                color[1] = value;
            } else if (attribute.equals(blue)) {
                color[2] = value;
            } else {
                throw new AssertionError("expected the attribute to equal a color");
            }
        }
        return color;
    }

    private Instance getThumbPixelInstanceAt(int x, int y) {
        // get color as int, see https://stackoverflow.com/a/40498362/1320237
        int color = previewThumb.getPixel(x, y);
        int red = (color >> 16) & 0xff;
        int green = (color >> 8) & 0xff;
        int blue = color & 0xff;
        // use HSV to remove the brightness of the color
        // see https://en.wikipedia.org/wiki/HSL_and_HSV
        /*float[] hsv = new float[3];
        Color.RGBToHSV(red, green, blue, hsv);
        hsv[2] = hsv[2] > 0.5f ? 1 : 0; // value
        color = Color.HSVToColor(hsv);
        red = (color >> 16) & 0xff;
        green = (color >> 8) & 0xff;
        blue = color & 0xff;*/
        // for instances
        // see https://weka.sourceforge.io/doc.dev/weka/core/DenseInstance.html
        Instance pixel = new DenseInstance(3);
        pixel.setValue(this.red, red);
        pixel.setValue(this.green, green);
        pixel.setValue(this.blue, blue);
        return pixel;
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
