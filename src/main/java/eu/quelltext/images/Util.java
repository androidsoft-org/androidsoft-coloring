package eu.quelltext.images;

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
}
