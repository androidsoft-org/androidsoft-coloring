package org.androidsoft.coloring.util.images;

import org.androidsoft.coloring.util.images.GalleryImageDB;
import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static junit.framework.TestCase.assertEquals;

public class DateTimeFormatTest {

    @Test
    public void testDate1() throws ParseException {
        Calendar calendar = parse("2020-03-10T22:16:41+01:00");
        // see https://docs.oracle.com/javase/8/docs/api/java/util/Date.html
        assertEquals(2020,    calendar.get(Calendar.YEAR));
        assertEquals(2,       calendar.get(Calendar.MONTH));
        assertEquals(10,      calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(22,      calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(16,      calendar.get(Calendar.MINUTE));
        assertEquals(41,      calendar.get(Calendar.SECOND));
    }

    @Test
    public void testDate3() throws ParseException {
        Calendar calendar = parse("1998-01-23T10:10:00-01:00");
        // see https://docs.oracle.com/javase/8/docs/api/java/util/Date.html
        assertEquals(1998,    calendar.get(Calendar.YEAR));
        assertEquals(0,       calendar.get(Calendar.MONTH));
        assertEquals(23,      calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(10,      calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(10,      calendar.get(Calendar.MINUTE));
        assertEquals(00,      calendar.get(Calendar.SECOND));
    }

    private Calendar parse(String dateString) throws ParseException {
        Date date = GalleryImageDB.parseTimeStamp(dateString);
        Calendar calendar = Calendar.getInstance();
        // see https://mkyong.com/java/java-date-and-calendar-examples/
        calendar.setTime(date);
        return calendar;
    }
}
