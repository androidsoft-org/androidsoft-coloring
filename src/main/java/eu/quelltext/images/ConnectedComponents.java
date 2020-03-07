package eu.quelltext.images;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ConnectedComponents {
    private final int[] classified;
    private final int width;
    private final int height;

    public ConnectedComponents(int[] classified, int width, int height) {
        this.classified = classified;
        this.width = width;
        this.height = height;
    }

    public Result compute() {
        int[] area = new int[width * height];
        List<Set<Integer>> labels = new ArrayList<>();

        // first pass: label
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int thisValue = classified[x + y * width];
                if (x > 0 && classified[x-1 + y * width] == thisValue) {
                    if (y > 0 && classified[x + (y-1) * width] == thisValue) {
                        /* pixel is equal to left and top */
                        int leftLabel = area[x-1 + y * width];
                        int topLabel = area[x + (y-1) * width];
                        area[x + y * width] = leftLabel;
                        if (leftLabel == topLabel) {
                            /* both labels are equivalent */
                        } else {
                            /* record equivalence */
                            Set<Integer> leftSet = labels.get(leftLabel);
                            leftSet.addAll(labels.get(topLabel));
                            labels.set(topLabel, leftSet);
                        }
                    } else {
                        /* pixel is equal to left only */
                        area[x + y * width] = area[x-1 + y * width];
                    }
                } else if (y > 0 && classified[x + (y-1) * width] == thisValue) {
                    /* pixel is equal to top only */
                    area[x + y * width] = area[x + (y-1) * width];
                } else {
                    /* pixel is different from top and left */
                    /* create new label */
                    int newLabel = labels.size();
                    CopyOnWriteArraySet<Integer> set = new CopyOnWriteArraySet<Integer>();
                    set.add(newLabel);
                    labels.add(set);
                    area[x + y * width] = newLabel;
                }
            }
        }
        // second pass components
        int[] minLabels = new int[labels.size()]; /* this is the lowest recorded equivalent label */
        for (int i = 0; i < labels.size(); i++) {
            int minLabel = i;
            for (int label : labels.get(i)) {
                if (label < minLabel) {
                    minLabel = label;
                }
            }
            minLabels[i] = minLabel;
        }
        return new Result(area, width, height, minLabels);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public static class Result {
        private boolean isFinal;
        private int[] area;
        private final int width;
        private final int height;
        private int[] minLabels;

        public Result(int[] area, int width, int height, int[] minLabels) {
            this.area = area;
            this.width = width;
            this.height = height;
            this.minLabels = minLabels;
            isFinal = false;
        }

        public int[] computeArray() {
            if (isFinal) {
                return area;
            }
            int[] result = new int[width * height];
            Map<Integer, Integer> finalLabels = new HashMap<>();
            for (int i = 0; i <  result.length; i++) {
                int label = minLabels[area[i]];
                int finalLabel;
                if (!finalLabels.containsKey(label)) {
                    finalLabel = finalLabels.size();
                    finalLabels.put(label, finalLabel);
                } else {
                    finalLabel = finalLabels.get(label);
                }
                result[i] = finalLabel;
            }
            // record result - it will not change
            minLabels = null;
            this.area = result;
            isFinal = true;
            return result;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public Measurement computeMeasurement() {
            return new Measurement(computeArray(), width, height);
        }
    }
}
