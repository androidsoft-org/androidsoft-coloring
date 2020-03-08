package eu.quelltext.images;

import org.androidsoft.coloring.ui.activity.PaintActivity;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/* This class maps integer arrays back and forth.
 * Suppose, you have an {array of colors} where the colors are huge numbers.
 * You can use ArrayMapper.mapTo({array of colors}) to get an array with lower numbers
 * from 0 to the the number of colors - 1.
 * {array of colors} --> mapTo() --> {indices of colors}, {colors} --> mapFrom() --> {array of colors}
 */
public class ArrayMapper {

    public interface Result {
        int[] getArray();
        int[] getValuesInOrder();
    }

    public static class MapToResult implements Result {

        private final int[] result;
        private final Map<Integer, Integer> mapping;

        public MapToResult(int[] result, Map<Integer, Integer> mapping) {
            this.result = result;
            this.mapping = mapping;
        }

        public int[] getArray() {
            return result;
        }

        public int[] getValuesInOrder() {
            int[] keys = new int[mapping.size()];
            //Arrays.fill(keys, Util.max(mapping.values(), -1) + 1);
            for (Map.Entry<Integer, Integer> entry: mapping.entrySet()) {
                keys[entry.getValue()] = entry.getKey();
            }
            return keys;
        }
    }

    public static Result mapTo(int[] data) {
        return mapTo(data, new int[0]);
    }

    public static Result mapTo(int[] data, int[] values) {
        Map<Integer, Integer> mapping = new ConcurrentSkipListMap<>();
        for (int i = 0; i < values.length; i++) {
            mapping.put(values[i], i);
        }
        int[] result = new int[data.length];
        for (int i = 0; i < result.length; i++) {
            Integer id = mapping.get(data[i]);
            if (id == null) {
                id = mapping.size();
                mapping.put(data[i], id);
            }
            result[i] = id;
        }
        return new MapToResult(result, mapping);
    }

    private static class MapFromResult implements Result {
        private final int[] result;
        private int[] keys;
        private Map<Integer, Integer> mapping;

        public MapFromResult(int[] result, int[] keys, Map<Integer, Integer> mapping) {
            this.result = result;
            this.keys = keys;
            this.mapping = mapping;
        }

        @Override
        public int[] getArray() {
            return result;
        }

        @Override
        public int[] getValuesInOrder() {
            if (mapping == null) {
                return keys;
            }
            int[] newKeys = new int[Util.max(mapping.keySet(), -1) + 1];
            Arrays.fill(newKeys, Util.max(mapping.values(), -1) + 1);
            System.arraycopy(keys, 0, newKeys, 0, keys.length);
            for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
                newKeys[entry.getKey()] = entry.getValue();
            }
            keys = newKeys;
            mapping = null; // no need to compute a second time
            return keys;
        }
    }

    public static Result mapFrom(int[] array) {
        return mapFrom(array, new int[0]);
    }

    public static Result mapFrom(int[] array, int[] keys) {
        Map<Integer, Integer> mapping = null;
        int nextUnknownValue = 0;
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            int key = array[i];
            if (key >= keys.length) {
                if (mapping == null) {
                    mapping = new ConcurrentSkipListMap<>();
                    nextUnknownValue = Util.max(keys, -1);
                }
                Integer value = mapping.get(key);
                if (value == null) {
                    nextUnknownValue++;
                    mapping.put(key, nextUnknownValue);
                    result[i] = nextUnknownValue;
                } else {
                    result[i] = value;
                }
            } else {
                result[i] = keys[key];
            }
        }
        return new MapFromResult(result, keys, mapping);
    }
}
