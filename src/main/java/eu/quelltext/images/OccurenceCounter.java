package eu.quelltext.images;

import android.os.Build;
import android.util.ArrayMap;

import java.util.HashMap;
import java.util.Map;

public class OccurenceCounter {
    private final Map<Integer, Integer> occurences;

    public OccurenceCounter() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
             occurences = new ArrayMap<>();
         } else {
             occurences = new HashMap<>();
         }
     }

    public void increase(int label) {
        int count;
        Integer previousCount = occurences.get(label);
        if (previousCount == null) {
            count = 1;
        } else {
            count = previousCount + 1;
        }
        occurences.put(label, count);
    }

    public int max() {
        int maxLabel = -1;
        int maxCount = -1;
        for(Map.Entry<Integer, Integer> label : occurences.entrySet()) {
            if (label.getValue() > maxCount) {
                maxCount = label.getValue();
                maxLabel = label.getKey();
            }
        }
        return maxLabel;
    }
}
