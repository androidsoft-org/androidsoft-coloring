package org.androidsoft.coloring.util.imports;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;

import eu.quelltext.images.ArrayMapper;
import eu.quelltext.images.ClusteredColors;
import eu.quelltext.images.ConnectedComponents;
import eu.quelltext.images.FastMaximumShiftFilter;
import eu.quelltext.images.KMeansOnRGBColors;
import eu.quelltext.images.LineasAroundAreas;
import eu.quelltext.images.Measurement;
import eu.quelltext.images.RandomColorGenerator;

public class ColoredImageImport extends UriImageImport {

    private static final int NUMBER_OF_COLORS = 9;
    private static final int LINE_WIDTH = 7;
    private static final int MAX_SHIFT_KERNEL_DIAMETER = 10;
    private static final int COLOR_LINE = Color.BLACK;
    private static final int COLOR_BACKGROUND = Color.WHITE;

    // at least LINE_WIDTH  wide between the lines so one can actually touch it
    private static final int MINIMUM_AREA = 2 * (LINE_WIDTH * 2 * LINE_WIDTH * 2);
    private ClusteredColors cluster;
    private int[] colors;

    public ColoredImageImport(Uri imageUri, LoadImageProgress progress, ImagePreview imagePreview) {
        super(imageUri, progress, imagePreview);
    }

    @Override
    protected void runWithBitmap(Bitmap image) {
        colors = getPixels(image);
        showImage(colors);
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
        super.runWithBitmap(getBitmap(colors));
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

}
