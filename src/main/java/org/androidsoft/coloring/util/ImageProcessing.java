package org.androidsoft.coloring.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;

import org.androidsoft.coloring.ui.activity.ImageImportActivity;
import org.androidsoft.coloring.ui.widget.LoadImageProgress;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.PerformanceStats;

// see https://developer.android.com/reference/java/lang/Thread
public class ImageProcessing implements Runnable {

    private static final int NUMBER_OF_COLORS = 10;
    private Attribute red;
    private Attribute green;
    private Attribute blue;
    private Bitmap previewThumb;
    private Bitmap classifiedColors;

    public interface ImagePreview {
        void setImage(Bitmap image);
        double getWidth();
        double getHeight();
        InputStream openInputStream(Uri uri) throws FileNotFoundException;
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

        progress.stepDone();
    }

    private void classifyColors() throws Exception {
        progress.stepPreparingClustering();
        // for an example run, see
        // see https://www.programcreek.com/2014/02/k-means-clustering-in-java/
        // for SimpleKMeans
        // see https://weka.sourceforge.io/doc.dev/weka/clusterers/SimpleKMeans.html
        SimpleKMeans kmeans = new SimpleKMeans();
        // see https://weka.sourceforge.io/doc.dev/weka/core/DistanceFunction.html
        kmeans.setDistanceFunction(new EuclideanDistance());
        kmeans.setPreserveInstancesOrder(false);
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
        Instances centroids = kmeans.getClusterCentroids();
        int[] centroidColors = new int[NUMBER_OF_COLORS];
        for (int i = 0; i < NUMBER_OF_COLORS; i++) {
            Instance centroid = centroids.get(i);
            int color = 0;
            for (int a = 0; a < centroid.numAttributes(); a++) {
                Attribute attribute = centroid.attribute(a);
                int value = (int)Math.round(centroid.value(a)) & 0xff;
                if (attribute.equals(red)) {
                    color |= value << 16;
                } else if (attribute.equals(green)) {
                    color |= value << 8;
                } else if (attribute.equals(blue)) {
                    color |= value;
                } else {
                    throw new AssertionError("expected the attribute to equal a color");
                }
            }
            centroidColors[i] = color;
        }
        progress.stepCreateClusterPreview();
        // build a colored bitmap from the classification
        classifiedColors = previewThumb.copy(previewThumb.getConfig(), true);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Instance pixel = getThumbPixelInstanceAt(x, y);
                int classification = kmeans.clusterInstance(pixel);
                int color = centroidColors[classification];
                classifiedColors.setPixel(x, y, color);
            }
        }
    }

    private Instance getThumbPixelInstanceAt(int x, int y) {
        // get color as int, see https://stackoverflow.com/a/40498362/1320237
        int color = previewThumb.getPixel(x, y);
        // for instances
        // see https://weka.sourceforge.io/doc.dev/weka/core/DenseInstance.html
        Instance pixel = new DenseInstance(3);
        pixel.setValue(red, (color >> 16) & 0xff);
        pixel.setValue(green, (color >> 8) & 0xff);
        pixel.setValue(blue, color & 0xff);
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
            ratio = maxWidth / (double)onlyBoundsOptions.outWidth;
        } else {
            ratio = maxHeight / (double)onlyBoundsOptions.outHeight;
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
