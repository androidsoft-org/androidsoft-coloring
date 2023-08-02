package org.androidsoft.coloring.util.cache;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.androidsoft.coloring.util.cache.FileCacheWithId.makeIdReadyForDirectory;


/* This class tests the conversion of ids to a path in the file system.
 */
public class IdConversionTest {

    @Test
    public void testConvertSimpleString() {
        assertEquals("hello", makeIdReadyForDirectory("hello"));
        assertEquals("123.png", makeIdReadyForDirectory("123.png"));
    }

    @Test
    public void testSeveralSlashes() {
        assertEquals("a/b", makeIdReadyForDirectory("a//b"));
        assertEquals("a/b/c/123.png", makeIdReadyForDirectory("a//b//c/123.png"));
        assertEquals("a/b", makeIdReadyForDirectory("a/////////////b"));
    }

    @Test
    public void testSlashAtStart() {
        assertEquals("a/b", makeIdReadyForDirectory("/a/b"));
    }

    /* HTTPS and HTTP should be the same.
     *
     */
    @Test
    public void testUrlLoosesScheme() {
        assertEquals("192.168.42.1/asd.1.2.3/sss", makeIdReadyForDirectory("http://192.168.42.1/asd.1.2.3/sss"));
        assertEquals("192.168.42.1/asd.1.2.3/sss", makeIdReadyForDirectory("https://192.168.42.1/asd.1.2.3/sss"));
        assertEquals("192.168.42.1/asd.1.2.3/sss", makeIdReadyForDirectory("HTTPs://192.168.42.1/asd.1.2.3/sss"));
        assertEquals("http:/192.168.42.1/asd.1.2.3/sss", makeIdReadyForDirectory("http:/192.168.42.1/asd.1.2.3/sss"));
    }

    @Test
    public void testUrlHandlesPort() {
        /* Test because : might be a special character */
        assertEquals("192.168.42.1:8000/asd.1.2.3/sss", makeIdReadyForDirectory("http://192.168.42.1:8000/asd.1.2.3/sss"));
    }


    @Test
    public void testSlashAtEndMapsToIndex() {
        assertEquals("A/B/C/index", makeIdReadyForDirectory("A/B/C/"));
    }

}
