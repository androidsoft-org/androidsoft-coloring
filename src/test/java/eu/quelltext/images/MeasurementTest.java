package eu.quelltext.images;

import org.junit.Test;

import static eu.quelltext.images.Assertions.assert2DArrayEquals;
import static junit.framework.TestCase.assertEquals;

public class MeasurementTest {
    private Measurement measurement = null;

    @Test
    public void testNothingToMeasure() {
        createMeasurement(new int[]{}, 0, 0);
        assertHasComponents(0);
        measurement.mergeSmallestAreaIntoItsBiggestNeighbor();
        assertHasComponents(0);
    }

        @Test
    public void testEmptyArea() {
        createMeasurement(new int[]{
                0, 0, 0,
                0, 0, 0,
                0, 0, 0,
        }, 3, 3);
        assertHasComponents(1);
        measurement.mergeSmallestAreaIntoItsBiggestNeighbor();
        assertHasComponents(1);
        assertAreaEquals(new int[]{
                0, 0, 0,
                0, 0, 0,
                0, 0, 0,
        });
    }

    @Test
    public void testThreeComponentArea() {
        createMeasurement(new int[]{
                0, 1, 0,
                0, 1, 3,
                0, 3, 3,
        }, 3, 3);
        assertHasComponents(3);
        measurement.mergeSmallestAreaIntoItsBiggestNeighbor();
        assertHasComponents(2);
        assertAreaEquals(new int[]{
                0, 0, 0, // 1 is smallest and has 3x 0 as neighbor
                0, 0, 3,
                0, 3, 3,
        });
    }

    @Test
    public void testSixComponentArea() {
        createMeasurement(new int[]{
                0, 0, 0, 3, 3, 3, 3, 3, 5, 5, 6,
                0, 1, 0, 2, 2, 3, 4, 4, 5, 6, 6,
                0, 1, 0, 2, 2, 3, 4, 4, 5, 6, 6,
                0, 1, 0, 2, 2, 3, 4, 4, 5, 6, 6,
        }, 11, 4);
        assertHasComponents(7);

        measurement.mergeSmallestAreaIntoItsBiggestNeighbor();
        assertHasComponents(6);
        assertAreaEquals(new int[]{
                0, 0, 0, 3, 3, 3, 3, 3, 5, 5, 6,
                0, 0, 0, 2, 2, 3, 4, 4, 5, 6, 6,
                0, 0, 0, 2, 2, 3, 4, 4, 5, 6, 6,
                0, 0, 0, 2, 2, 3, 4, 4, 5, 6, 6,
        });

        measurement.mergeSmallestAreaIntoItsBiggestNeighbor();
        assertHasComponents(5);
        assertAreaEquals(new int[]{
                0, 0, 0, 3, 3, 3, 3, 3, 6, 6, 6,
                0, 0, 0, 2, 2, 3, 4, 4, 6, 6, 6,
                0, 0, 0, 2, 2, 3, 4, 4, 6, 6, 6,
                0, 0, 0, 2, 2, 3, 4, 4, 6, 6, 6,
        });

        measurement.mergeSmallestAreaIntoItsBiggestNeighbor();
        measurement.mergeSmallestAreaIntoItsBiggestNeighbor();
        assertHasComponents(3);
        assertAreaEquals(new int[]{
                0, 0, 0, 3, 3, 3, 3, 3, 6, 6, 6,
                0, 0, 0, 3, 3, 3, 3, 3, 6, 6, 6,
                0, 0, 0, 3, 3, 3, 3, 3, 6, 6, 6,
                0, 0, 0, 3, 3, 3, 3, 3, 6, 6, 6,
        });
    }

    @Test
    public void  testMergingIntoEachother() {
        createMeasurement(new int[]{
                1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4,
                1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4,
        }, 15, 2);
        measurement.mergeSmallestAreaIntoItsBiggestNeighbor();
        assertAreaEquals(new int[]{
                2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4,
                2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4,
        });
        measurement.mergeSmallestAreaIntoItsBiggestNeighbor();
        assertAreaEquals(new int[]{
                3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4,
                3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4,
        });
        measurement.mergeSmallestAreaIntoItsBiggestNeighbor();
        assertAreaEquals(new int[]{
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        });
    }

    @Test
    public void  testMergeIntoRemovedArea() {
        createMeasurement(new int[]{
                1, 1, 1, 1,
                1, 1, 1, 3,
                0, 0, 0, 3,
                3, 3, 3, 3,
        }, 4, 4);
        measurement.mergeSmallestAreaIntoItsBiggestNeighbor();
        assertAreaEquals(new int[]{
                1, 1, 1, 1,
                1, 1, 1, 3,
                3, 3, 3, 3,
                3, 3, 3, 3,
        });
        measurement.mergeSmallestAreaIntoItsBiggestNeighbor();
        assertHasComponents(1);
    }

    private void assertAreaEquals(int[] expectedArea) {
        assert2DArrayEquals(expectedArea, measurement.computeArea(),
                measurement.getWidth(), measurement.getHeight());
    }

    private void createMeasurement(int[] components, int width, int height) {
        measurement = new Measurement(components, width, height);
    }

    private void assertHasComponents(int expectedNumberOfComponents) {
        int numberOfComponents = measurement.getNumberOfComponents();
        assertEquals("expected " + expectedNumberOfComponents + " components instead of " + numberOfComponents,
                expectedNumberOfComponents, numberOfComponents);
    }
}
