package eu.quelltext.images;

import java.util.Arrays;

public class ClusteredColors {
    private final int[] classifiedColors;
    private final int[] centroidColors;

    public ClusteredColors(int[] classifiedColors, int[] centroidColors) {
        this.classifiedColors = classifiedColors;
        this.centroidColors = centroidColors;
    }

    /* Return a list of colors
     *
     */
    public int[] getClassifiedColors() {
        ArrayMapper.Result mapping = ArrayMapper.mapFrom(classifiedColors, centroidColors);
        if (mapping.getValuesInOrder().length != centroidColors.length) {
            throw new AssertionError("Not all colors were in the centroids.");
        }
        return mapping.getArray();
    }

    /* Return a list of integers from 0 to n where n is the number of clusters - 1
     *
     */
    public int[] getArrayWithClusterIds() {
        return classifiedColors;
    }
}
