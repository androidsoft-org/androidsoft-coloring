package eu.quelltext.coloring;

import org.junit.Test;

import eu.quelltext.clustering.ConnectedComponents;

import static junit.framework.TestCase.assertEquals;

public class ConnectedComponentTest {

    @Test
    public void testOneComponent() {
        assertComponentComputed(
                new int[]{
                        1, 1, 1,
                        1, 1, 1,
                        1, 1, 1,
                },
                new int[]{
                        0, 0, 0,
                        0, 0, 0,
                        0, 0, 0,
                },
                3, 3
        );
    }

    @Test
    public void testTwoComponents() {
        assertComponentComputed(
                new int[]{
                        1, 1, 1,
                        1, 1, 1,
                        5, 5, 5,
                },
                new int[]{
                        0, 0, 0,
                        0, 0, 0,
                        1, 1, 1,
                },
                3, 3
        );
    }
    @Test
    public void testThreeComponents() {
        assertComponentComputed(
                new int[]{
                        1, 1, 1,
                        1, 5, 5,
                        5, 5, 1,
                },
                new int[]{
                        0, 0, 0,
                        0, 1, 1,
                        1, 1, 2,
                },
                3, 3
        );
    }

    private void assertComponentComputed(int[] classifiedArray, int[] expectedArray, int width, int height) {
        ConnectedComponents connectedComponents = new ConnectedComponents(classifiedArray, width, height);
        ConnectedComponents.Result result = connectedComponents.compute();
        int[] resultArray = result.computeArray();
        assertEquals("result should be of length " + expectedArray.length + " and not " + resultArray.length,
                expectedArray.length, resultArray.length);
        String message = "";
        int i = 0;
        for (int y = 0; y < height; y++) {
            String line1 = "";
            String line2 = "";
            for (int x = 0; x < width; x++) {
                line1 += expectedArray[i] + ",\t";
                line2 += resultArray[i] + ",\t";
                i++;
            }
            message += "\n" + line1 + "==\t" + line2;
        }
        for (i = 0; i < expectedArray.length; i++) {
            assertEquals(message, expectedArray[i], resultArray[i]);
        }
    }
}
