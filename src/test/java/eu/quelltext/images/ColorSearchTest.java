package eu.quelltext.images;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ColorSearchTest {

    @Test
    public void testSize0() {
        ColorSearch search = new ColorSearch(new int[0], 0, 0);
        search.startSearch(0, 0, ColorComparator.unequal(1), 2);
        assertEquals(false, search.wasSuccessful());
    }

    @Test
    public void testSize3Find1() {
        ColorSearch search = new ColorSearch(new int[]{
                1,1,1,
                1,1,1,
                1,1,2,
        }, 3, 3);
        search.startSearch(1, 1, ColorComparator.unequal(1), 1);
        assertEquals(true, search.wasSuccessful());
        assertEquals(2, search.getX());
        assertEquals(2, search.getY());
    }

    @Test
    public void testSize3Fail0() {
        ColorSearch search = new ColorSearch(new int[]{
                1,1,1,
                1,1,1,
                1,1,2,
        }, 3, 3);
        search.startSearch(1, 1, ColorComparator.unequal(1), 0);
        assertEquals(false, search.wasSuccessful());
    }



    @Test
    public void testInvalidParameters() {
        ColorSearch search = new ColorSearch(new int[]{
                1,1,1,
                1,1,1,
                1,1,2,
        }, 3, 3);
        search.startSearch(-1, 1, ColorComparator.unequal(1), 0);
        assertEquals(false, search.wasSuccessful());
        search.startSearch(4, 1, ColorComparator.unequal(1), 0);
        assertEquals(false, search.wasSuccessful());
        search.startSearch(1, -1, ColorComparator.unequal(1), 0);
        assertEquals(false, search.wasSuccessful());
        search.startSearch(1, 4, ColorComparator.unequal(1), 0);
        assertEquals(false, search.wasSuccessful());
    }

    @Test
    public void testSize3Fail1() {
        ColorSearch search = new ColorSearch(new int[]{
                1,1,1,
                1,1,1,
                1,1,2,
        }, 3, 3);
        search.startSearch(0, 1, ColorComparator.unequal(1), 1);
        assertEquals(false, search.wasSuccessful());
    }

    @Test
    public void testSize3Find2() {
        ColorSearch search = new ColorSearch(new int[]{
                1,1,1,
                1,1,1,
                1,1,2,
        }, 3, 3);
        search.startSearch(0, 1, ColorComparator.unequal(1), 2);
        assertEquals(true, search.wasSuccessful());
        assertEquals(2, search.getX());
        assertEquals(2, search.getY());
    }

    @Test
    public void testSize5Find2() {
        ColorSearch search = new ColorSearch(new int[]{
                1,1,1,1,1,
                1,0,3,3,3,
                1,1,1,1,1,
                1,1,1,1,0,
                1,1,1,1,1,
        }, 5, 5);
        search.startSearch(0, 4, ColorComparator.unequal(1, 3), 3);
        assertEquals(true, search.wasSuccessful());
        assertEquals(1, search.getX());
        assertEquals(1, search.getY());
    }

}
