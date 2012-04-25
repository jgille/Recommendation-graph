package recng.filter;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;

import org.hamcrest.Matchers;
import org.junit.Test;

import recng.filter.Interval;
import recng.filter.Intervals;
import recng.filter.LowerIntervalBound;
import recng.filter.UpperIntervalBound;

public class IntervalsTest {

    @Test
    public void testSingleIntervalContains() {
        Interval<Integer> i1 = new Interval<Integer>(new LowerIntervalBound<Integer>(1),
                                                     new UpperIntervalBound<Integer>(5));
        Intervals<Integer> intervals = new Intervals<Integer>(Collections.singletonList(i1));

        assertThat(intervals.contains(0), Matchers.is(false));
        assertThat(intervals.contains(3), Matchers.is(true));
        assertThat(intervals.contains(6), Matchers.is(false));
    }

    @Test
    public void testSingleIntervalsIntersect() {
        Interval<Integer> i1 = new Interval<Integer>(new LowerIntervalBound<Integer>(1),
                                                     new UpperIntervalBound<Integer>(5));
        Intervals<Integer> intervals1 = new Intervals<Integer>(Collections.singletonList(i1));

        Interval<Integer> i2 = new Interval<Integer>(new LowerIntervalBound<Integer>(3),
                                                     new UpperIntervalBound<Integer>(6));
        Intervals<Integer> intervals2 = new Intervals<Integer>(Collections.singletonList(i2));

        Intervals<Integer> intervals = intervals1.intersectWith(intervals2);

        assertThat(intervals.contains(0), Matchers.is(false));
        assertThat(intervals.contains(2), Matchers.is(false));
        assertThat(intervals.contains(3), Matchers.is(true));
        assertThat(intervals.contains(5), Matchers.is(true));
        assertThat(intervals.contains(6), Matchers.is(false));
    }

    @Test
    public void testDualIntervalsContains() {
        Interval<Integer> i1 = new Interval<Integer>(new LowerIntervalBound<Integer>(1),
                                                     new UpperIntervalBound<Integer>(5));

        Interval<Integer> i2 = new Interval<Integer>(new LowerIntervalBound<Integer>(8),
                                                     new UpperIntervalBound<Integer>(10));

        Intervals<Integer> intervals = new Intervals<Integer>(Collections.singletonList(i1));
        intervals.addInterval(i2);

        assertThat(intervals.contains(0), Matchers.is(false));
        assertThat(intervals.contains(3), Matchers.is(true));
        assertThat(intervals.contains(6), Matchers.is(false));
        assertThat(intervals.contains(9), Matchers.is(true));
        assertThat(intervals.contains(11), Matchers.is(false));
    }

    @Test
    public void testDualIntervalsIntersect() {
        Interval<Integer> i1 = new Interval<Integer>(new LowerIntervalBound<Integer>(1),
                                                     new UpperIntervalBound<Integer>(5));

        Interval<Integer> i2 = new Interval<Integer>(new LowerIntervalBound<Integer>(8),
                                                     new UpperIntervalBound<Integer>(10));

        Intervals<Integer> intervals1 = new Intervals<Integer>(Collections.singletonList(i1));
        intervals1.addInterval(i2);

        Interval<Integer> i3 = new Interval<Integer>(new LowerIntervalBound<Integer>(4),
                                                     new UpperIntervalBound<Integer>(6));

        Interval<Integer> i4 = new Interval<Integer>(new LowerIntervalBound<Integer>(9),
                                                     new UpperIntervalBound<Integer>(20));

        Intervals<Integer> intervals2 = new Intervals<Integer>(Collections.singletonList(i3));
        intervals2.addInterval(i4);

        Intervals<Integer> intervals = intervals1.intersectWith(intervals2);

        for (int i = 0; i <= 3; i++) {
            assertThat(intervals.contains(i), Matchers.is(false));
        }
        for (int i = 4; i <= 5; i++) {
            assertThat(intervals.contains(i), Matchers.is(true));
        }
        for (int i = 6; i <= 8; i++) {
            assertThat(intervals.contains(i), Matchers.is(false));
        }
        for (int i = 9; i <= 10; i++) {
            assertThat(intervals.contains(i), Matchers.is(true));
        }
        for (int i = 11; i <= 21; i++) {
            assertThat(intervals.contains(i), Matchers.is(false));
        }
    }

}
