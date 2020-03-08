package eu.quelltext.images;

import android.os.Build;
import android.util.ArrayMap;

import java.util.HashMap;
import java.util.Map;

public class OccurrenceCounter {
    private final Map<Integer, Integer> occurrences;

    public OccurrenceCounter() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
             occurrences = new ArrayMap<>();
         } else {
             occurrences = new HashMap<>();
         }
     }

    public void increase(int label) {
        increaseBy(label, 1);
        System.out.println("\tcounter" + "+" + label);
    }

    public void decrease(int label) {
        increaseBy(label, -1);
        System.out.println("\tcounter" + "-" + label);
    }

    private void increaseBy(int label, int difference) {
        int count;
        Integer previousCount = occurrences.get(label);
        if (previousCount == null) {
            count = difference;
        } else {
            count = previousCount + difference;
        }
        if (count < 0) {
            throw new AssertionError("Label " + label + " can not occur " + count + " times.");
        }
        occurrences.put(label, count);
    }

    public int max() {
        int maxLabel = -1;
        int maxCount = -1;
        for(Map.Entry<Integer, Integer> label : occurrences.entrySet()) {
            if (label.getValue() > maxCount) {
                maxCount = label.getValue();
                maxLabel = label.getKey();
            }
        }
        return maxLabel;
    }

    public OccurrenceCounter copy() {
        OccurrenceCounter copy = new OccurrenceCounter();
        copy.initializeFrom(occurrences);
        return copy;
    }

    protected void initializeFrom(Map<Integer, Integer> occurrences) {
        this.occurrences.putAll(occurrences);
    }
}
