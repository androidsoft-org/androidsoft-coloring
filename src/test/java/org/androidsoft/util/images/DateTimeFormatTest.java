package org.androidsoft.util.images;

import org.androidsoft.coloring.util.images.GalleryImageDB;
import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static junit.framework.TestCase.assertEquals;

public class DateTimeFormatTest {

    @Test
    public void testDateTimeFormat() throws ParseException {
        Calendar calendar = GalleryImageDB.parse("2020-03-10T22:16:41+01:00");
        // see https://docs.oracle.com/javase/8/docs/api/java/util/Date.html
        assertEquals(2020,    calendar.get(Calendar.YEAR));
        assertEquals(2,       calendar.get(Calendar.MONTH));
        assertEquals(10,      calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(22,      calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(16,      calendar.get(Calendar.MINUTE));
        assertEquals(41,      calendar.get(Calendar.SECOND));
        assertEquals(3600000, calendar.get(Calendar.ZONE_OFFSET));
    }
}
