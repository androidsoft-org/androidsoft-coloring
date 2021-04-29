package eu.quelltext.images;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static eu.quelltext.images.Assertions.assert2DArrayEquals;

@RunWith(Theories.class)
public class RunningMaximumKernelTest {

    interface FilterCreator {
        MaximumShiftFilter create(int[] sourceArray, int width, int height);
    }

    // data points, see https://stackoverflow.com/a/752578
    public static @DataPoints FilterCreator[] creators = {
            new FilterCreator() {
                @Override
                public MaximumShiftFilter create(int[] array, int width, int height) {
                    return new SimpleMaximumShiftFilter(array, width, height);
                }
            },
            new FilterCreator() {
                @Override
                public MaximumShiftFilter create(int[] array, int width, int height) {
                    return new FastMaximumShiftFilter(array, width, height);
                }
            }
    };

    @Theory
    public void testOnlyOneArea(FilterCreator creator) {
        assertFilteredEquals(new int[]{
                1, 1, 1, 1, 1,
                1, 1, 1, 1, 1,
                1, 1, 1, 1, 1,
        }, new int[]{
                1, 1, 1, 1, 1,
                1, 1, 1, 1, 1,
                1, 1, 1, 1, 1,
        },  5, 3, 1, creator);
    }

    @Theory
    public void testSomeNoise(FilterCreator creator) {
        assertFilteredEquals(new int[]{
                1, 0, 1, 2, 2, 1,
                1, 1, 0, 1, 1, 1,
                3, 1, 1, 3, 0, 2,
        }, new int[]{
                1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1,
        },  6, 3, 1, creator);
    }

    @Theory
    public void testDifferentClasses(FilterCreator creator) {
        assertFilteredEquals(new int[]{
                3, 3, 0, 3, 2, 2,
                3, 1, 1, 1, 4, 2,
                3, 5, 1, 3, 0, 0,
        }, new int[]{
                3, 3, 1, 1, 2, 2,
                3, 3, 1, 1, 2, 2,
                3, 1, 1, 1, 0, 0,
        },  6, 3, 1, creator);
    }

    @Theory
    public void testDifferentRadius(FilterCreator creator) {
        assertFilteredEquals(new int[]{
                3, 3, 3, 3, 1, 1,
                3, 3, 1, 1, 1, 1,
                3, 3, 1, 1, 1, 1,
        }, new int[]{
                3, 3, 3, 1, 1, 1,
                3, 3, 3, 1, 1, 1,
                3, 3, 3, 1, 1, 1,
        },  6, 3, 2, creator);
    }

    private void assertFilteredEquals(int[] sourceArray, int[] expectedArray,
                                      int width, int height, int radius, FilterCreator creator) {
        MaximumShiftFilter maximumShiftFilter = creator.create(sourceArray, width, height);
        int[] computedArray = maximumShiftFilter.compute(radius);
        assert2DArrayEquals(expectedArray, computedArray, width, height);
    }
}
