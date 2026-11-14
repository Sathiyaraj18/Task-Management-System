package com.example.taskmanagement;

import com.example.taskmanagement.util.DateUtils;

import org.junit.Test;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Unit tests for DateUtils functions.
 */
public class DateUtilsTest {

    @Test
    public void testFormatDisplayDate_validDate() {
        String formatted = DateUtils.formatDisplayDate("2026-11-14");
        assertEquals("14 Nov 2026", formatted);
    }

    @Test
    public void testFormatDisplayDate_emptyOrNull() {
        assertEquals("-", DateUtils.formatDisplayDate(null));
        assertEquals("-", DateUtils.formatDisplayDate(""));
    }

    @Test
    public void testDueDateBeforeStartDate_validComparison() {
        assertTrue(DateUtils.isDueDateBeforeStartDate("2026-11-20", "2026-11-10"));
        assertFalse(DateUtils.isDueDateBeforeStartDate("2026-11-10", "2026-11-20"));
        assertFalse(DateUtils.isDueDateBeforeStartDate("2026-11-15", "2026-11-15"));
    }

    @Test
    public void testIsPastDate_withPastAndFuture() {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.FORMAT_DATABASE, Locale.getDefault());

        Calendar pastCal = Calendar.getInstance();
        pastCal.add(Calendar.DAY_OF_YEAR, -5);
        assertTrue(DateUtils.isPastDate(sdf.format(pastCal.getTime())));

        Calendar futureCal = Calendar.getInstance();
        futureCal.add(Calendar.DAY_OF_YEAR, 5);
        assertFalse(DateUtils.isPastDate(sdf.format(futureCal.getTime())));
    }
}
