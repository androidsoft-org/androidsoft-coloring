package eu.quelltext.images;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Measurement {
    private final Map<Integer, Map<Integer, Integer>> neighborCount = new HashMap<>();
    private final Map<Integer, ArrayList<XY>> positions = new HashMap<>();
    private final Map<Integer, Integer> equalityLookup = new HashMap<>();
    private final int width;
    private final int height;

    public Measurement(int[] classified, int width, int height) {
        this.width = width;
        this.height = height;
        setup(classified);
    }

    private void setup(int[] classified) {
        int i = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++, i++) {
                int label = classified[i];
                equalityLookup.put(label, label);
                if (!positions.containsKey(label)) {
                    positions.put(label, new ArrayList<XY>());
                    neighborCount.put(label, new HashMap<Integer, Integer>());
                }
                positions.get(label).add(new XY(x, y));
                Map<Integer, Integer> thisCount = null;
                if (x > 0) {
                    int leftLabel = classified[i - 1];
                    if (leftLabel != label) {
                        thisCount = neighborCount.get(label);
                        Map<Integer, Integer> leftCount = neighborCount.get(leftLabel);
                        increaseByOne(thisCount, leftLabel);
                        increaseByOne(leftCount, label);
                    }
                }
                if (y > 0) {
                    int topLabel = classified[i - width];
                    if (topLabel != label) {
                        if (thisCount == null) {
                            thisCount = neighborCount.get(label);
                        }
                        Map<Integer, Integer> topCount = neighborCount.get(topLabel);
                        increaseByOne(thisCount, topLabel);
                        increaseByOne(topCount, label);
                    }
                }
            }
        }
    }

    private void increaseByOne(Map<Integer, Integer> counters, int label) {
        if (counters.containsKey(label)) {
            counters.put(label, counters.get(label) + 1);
        } else {
            counters.put(label, 1);
        }
    }


    public int getNumberOfComponents() {
        return positions.size();
    }

    public void mergeSmallestAreaIntoItsBiggestNeighbor() {
        if (positions.size() <= 1) {
            return;
        }
        int smallestLabel = getSmallestLabel();
        int biggestNeighborSize = 0;
        int biggestNeighborLabel = -1;
        for (Map.Entry<Integer, Integer> neigbor : neighborCount.get(smallestLabel).entrySet()) {
            if (biggestNeighborSize < neigbor.getValue()) {
                biggestNeighborLabel = neigbor.getKey();
                biggestNeighborSize = neigbor.getValue();
            }
        }
        if (smallestLabel == biggestNeighborLabel) {
            throw new AssertionError("A label can not be neighbor of itself.");
        }
        if (biggestNeighborLabel == -1) {
            throw new AssertionError("The label " + smallestLabel + " has no neighbors.");
        }
        ArrayList<XY> biggestNeighborPositions = null;
        while(biggestNeighborPositions == null) {
            biggestNeighborLabel = equalityLookup.get(biggestNeighborLabel);
            biggestNeighborPositions = positions.get(biggestNeighborLabel);
        }
        biggestNeighborPositions.addAll(positions.get(smallestLabel));
        positions.remove(smallestLabel);
        Map<Integer, Integer> biggestNeighborsNeighbors = neighborCount.get(biggestNeighborLabel);
        for (Map.Entry<Integer, Integer> neighbor : neighborCount.get(smallestLabel).entrySet()) {
            Integer neighborLabel = neighbor.getKey();
            if (biggestNeighborsNeighbors.containsKey(neighborLabel)) {
                biggestNeighborsNeighbors.put(neighborLabel,
                        biggestNeighborsNeighbors.get(neighborLabel) + neighbor.getValue());
            } else {
                biggestNeighborsNeighbors.put(neighborLabel, neighbor.getValue());
            }
        }
        neighborCount.remove(smallestLabel);
        biggestNeighborsNeighbors.remove(smallestLabel);
        biggestNeighborsNeighbors.remove(biggestNeighborLabel);
        equalityLookup.put(smallestLabel, biggestNeighborLabel);
    }

    private int getSmallestLabel() {
        int smallestLabel = -1;
        int smallestLabelSize = width * height + 1;
        for (Map.Entry<Integer, ArrayList<XY>> label: positions.entrySet()) {
            if (smallestLabelSize > label.getValue().size()) {
                smallestLabel = label.getKey();
                smallestLabelSize = label.getValue().size();
            }
        }
        return smallestLabel;
    }

    public int[] computeArea() {
        int[] area = new int[width * height];
        for (Map.Entry<Integer, ArrayList<XY>> labelPositions : positions.entrySet()) {
            int label = labelPositions.getKey();
            for (XY position : labelPositions.getValue()) {
                area[position.x + position.y * width] = label;
            }
        }
        return area;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSmallestComponentSize() {
        int smallestLabel = getSmallestLabel();
        return positions.get(smallestLabel).size();
    }

    private static class XY {

        private final int x;
        private final int y;

        private XY(int x, int y) {
            this.x = x;
            this.y = y;
        }
        @Override
        public boolean equals(@Nullable Object obj) {
            if (XY.class.isInstance(obj)) {
                XY other = (XY)obj;
                return other.x == x && other.y == y;
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return x ^ (y << 16);
        }
    }
}
