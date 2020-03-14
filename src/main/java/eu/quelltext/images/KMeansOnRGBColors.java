package eu.quelltext.images;

import java.util.ArrayList;
import java.util.Random;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.ManhattanDistance;

public class KMeansOnRGBColors {

    private static final int NOT_TRANSPARENT = 0xff000000;

    private final int[] colors;
    private final int numberOfColors;
    private final ArrayList<Attribute> attributes;
    private final int capacity;
    private final SimpleKMeans kmeans;
    
    private Attribute red = new Attribute("red");
    private Attribute green = new Attribute("green");
    private Attribute blue = new Attribute("blue");

    // step 1 attributes
    private boolean step01Taken = false;
    private Instances data;

    // step 2 attributes
    private boolean step02Taken = false;
    private int[][] centroids;
    private int[] centroidColors;

    public KMeansOnRGBColors(int[] colors, int numberOfColors) throws Exception {
        this.colors = colors;
        this.numberOfColors = numberOfColors;
        // for an example run, see
        // see https://www.programcreek.com/2014/02/k-means-clustering-in-java/
        // for SimpleKMeans
        // see https://weka.sourceforge.io/doc.dev/weka/clusterers/SimpleKMeans.html
        kmeans = new SimpleKMeans();
        // see https://weka.sourceforge.io/doc.dev/weka/core/DistanceFunction.html
        kmeans.setDistanceFunction(new ManhattanDistance()); // manhattan should speed things up
        kmeans.setPreserveInstancesOrder(false);
        kmeans.setFastDistanceCalc(true);
        kmeans.setNumClusters(numberOfColors);
        // computing the number of colors to get from the image
        // see https://medium.com/@equipintelligence/java-algorithms-the-k-nearest-neighbor-classifier-4faca7ad26b2
        //     in the Section "Determining the value of K"
        // formula: K = sqrt ( number of samples in dataset ) / 2
        capacity = 4 * numberOfColors * numberOfColors;
        // for attributes
        // see https://weka.sourceforge.io/doc.dev/weka/core/Attribute.html
        attributes = new ArrayList<>(3);
        attributes.add(red);
        attributes.add(green);
        attributes.add(blue);
    }

    /* This samples random data points in a reproducible way.
     *
     */
    public void step01RandomSampling() {
        data = new Instances("colors", attributes, capacity);
        // build the data set from randomly sampled data
        Random random = new Random(0xffaa123678234232l);
        for (int i = 0; i < capacity; i++) {
            int index = random.nextInt(colors.length);
            Instance pixel = getPixelInstanceAt(index);
            data.add(pixel);
        }
        step01Taken = true;
    }

    private Instance getPixelInstanceAt(int index) {
        // get color as int, see https://stackoverflow.com/a/40498362/1320237
        int color = colors[index];
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

    public void step02clustering() throws Exception {
        if (!step01Taken) {
            throw new AssertionError("Step 1 needs to be taken before step 2.");
        }
        // compute k-means
        kmeans.buildClusterer(data);
        Instances centroidInstances = kmeans.getClusterCentroids();
        centroids = new int[numberOfColors][3];
        centroidColors = new int[numberOfColors];
        for (int i = 0; i < numberOfColors; i++) {
            Instance centroid = centroidInstances.size() > i ?
                    centroidInstances.get(i) : centroidInstances.firstInstance();
            int[] color = getColorOf(centroid);
            centroids[i] = color;
            centroidColors[i] = NOT_TRANSPARENT | color[0] << 16 | color[1] << 8 | color[2];
        }
        step02Taken = true;
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

    public ClusteredColors step03ClassifyData() {
        if (!step02Taken) {
            throw new AssertionError("Step 2 needs to be taken before step 3.");
        }
        int[] classifiedColors = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            classifiedColors[i] = classifyPixel(i);
        }
        return new ClusteredColors(classifiedColors, centroidColors);
    }

    private int classifyPixel(int index) {
        int color = colors[index];
        int r = (color >> 16) & 0xff;
        int g = (color >>  8) & 0xff;
        int b = (color      ) & 0xff;
        int minDistance = 0xffff;
        int minCentroid = -1;
        for (int i = 0; i < numberOfColors; i++) {
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

}
