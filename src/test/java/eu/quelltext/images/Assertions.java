package eu.quelltext.images;

import static junit.framework.TestCase.assertEquals;

public class Assertions {
    public static void assert2DArrayEquals(int[] expectedArray, int[] array, int width, int height) {
        assertEquals("result should be of length " + expectedArray.length + " and not " + array.length,
                expectedArray.length, array.length);
        String message = "";
        int i = 0;
        for (int y = 0; y < height; y++) {
            String line1 = "";
            String line2 = "";
            for (int x = 0; x < width; x++) {
                line1 += expectedArray[i] + ",\t";
                line2 += array[i] + ",\t";
                i++;
            }
            message += "\n" + line1 + "==\t" + line2;
        }
        for (i = 0; i < expectedArray.length; i++) {
            assertEquals(message, expectedArray[i], array[i]);
        }
    }
}
