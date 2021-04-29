package eu.quelltext.images;

import org.junit.Test;

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

    @Test
    public void testBig() {
        assertComponentComputed(
                new int[]{
                        1, 1, 1, 1, 2, 2,
                        1, 0, 0, 1, 0, 1,
                        0, 0, 1, 0, 0, 1,
                },
                new int[]{
                        0, 0, 0, 0, 1, 1,
                        0, 2, 2, 0, 3, 4,
                        2, 2, 5, 3, 3, 4,
                },
                6, 3
        );
    }

    private void assertComponentComputed(int[] classifiedArray, int[] expectedArray, int width, int height) {
        ConnectedComponents connectedComponents = new ConnectedComponents(classifiedArray, width, height);
        ConnectedComponents.Result result = connectedComponents.compute();
        int[] resultArray = result.computeArray();
        Assertions.assert2DArrayEquals(expectedArray, resultArray, width, height);

    }

}
