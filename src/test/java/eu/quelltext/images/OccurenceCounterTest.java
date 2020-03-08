package eu.quelltext.images;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class OccurenceCounterTest {

    private OccurenceCounter counter;

    @Before
    public void setUpCounter() {
        counter = new OccurenceCounter();
    }

    @Test
    public void testEmpty() {
        assertEquals(-1, counter.max());
    }

    @Test
    public void testOne() {
        counter.increase(1);
        assertEquals(1, counter.max());
    }

    @Test
    public void testMany() {
        counter.increase(1);
        counter.increase(2);
        counter.increase(3);
        counter.increase(3);
        counter.increase(3);
        counter.increase(1);
        counter.increase(2);
        counter.increase(222);
        counter.increase(10000);
        assertEquals(3, counter.max());
    }
}
