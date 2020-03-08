package eu.quelltext.images;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class Util {
    public static int[] flatten(int[][] array) {
        // from https://stackoverflow.com/a/2569314
        int size = 0;
        for (int i = 0; i < array.length; i++) {
            size += array[i].length;
        }
        int[] newArray = new int[size];
        int newIndex = 0;
        for (int i = 0; i < array.length; i++) {
            System.arraycopy(array[i], 0, newArray, newIndex, array[i].length);
            newIndex += array[i].length;
        }
        return newArray;
    }

    public static int[][] unflatten(int[] array, int numberOfSubarrays) {
        int subArrayLength = array.length / numberOfSubarrays;
        int[][] newArray  = new int[numberOfSubarrays][subArrayLength];
        for (int i = 0; i < numberOfSubarrays; i++) {
            System.arraycopy(array, i * subArrayLength, newArray[i], 0, subArrayLength);
        }
        return newArray;
    }

    public static int max(int[] array, int defaultValue) {
        if (array.length == 0) {
            return defaultValue;
        }
        int biggestValue = array[0];
        for (int j = 1; j < array.length; j++) {
            if (array[j] > biggestValue) {
                biggestValue = array[j];
            }
        }
        return biggestValue;
    }

    public static int max(Collection<Integer> collection, int defaultValue) {
        if (collection.size() == 0) {
            return defaultValue;
        }
        Iterator<Integer> iterator = collection.iterator();
        int biggestValue = iterator.next();
        for (Iterator<Integer> it = iterator; it.hasNext(); ) {
            int value = it.next();
            if (value > biggestValue) {
                biggestValue = value;
            }
        }
        return biggestValue;
    }
}
