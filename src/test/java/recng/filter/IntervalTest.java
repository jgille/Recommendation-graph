package recng.filter;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

import recng.filter.Interval;
import recng.filter.LowerIntervalBound;
import recng.filter.UpperIntervalBound;

public class IntervalTest {

    @Test
    public void testInclusiveIntervalContains() {
        LowerIntervalBound<Integer> lower = new LowerIntervalBound<Integer>(1);
        lower.setInclusive(true);
        UpperIntervalBound<Integer> upper = new UpperIntervalBound<Integer>(3);
        upper.setInclusive(true);

        Interval<Integer> interval = new Interval<Integer>(lower, upper);

        assertThat(interval.contains(0), Matchers.is(false));
        assertThat(interval.contains(1), Matchers.is(true));
        assertThat(interval.contains(2), Matchers.is(true));
        assertThat(interval.contains(3), Matchers.is(true));
        assertThat(interval.contains(4), Matchers.is(false));
    }

    @Test
    public void testExclusiveIntervalContains() {
        LowerIntervalBound<Integer> lower = new LowerIntervalBound<Integer>(1);
        lower.setInclusive(false);
        UpperIntervalBound<Integer> upper = new UpperIntervalBound<Integer>(3);
        upper.setInclusive(false);

        Interval<Integer> interval = new Interval<Integer>(lower, upper);

        assertThat(interval.contains(0), Matchers.is(false));
        assertThat(interval.contains(1), Matchers.is(false));
        assertThat(interval.contains(2), Matchers.is(true));
        assertThat(interval.contains(3), Matchers.is(false));
        assertThat(interval.contains(4), Matchers.is(false));
    }

    @Test
    public void testNullLowerBoundIntervalContains() {
        LowerIntervalBound<Integer> lower = new LowerIntervalBound<Integer>(null);
        UpperIntervalBound<Integer> upper = new UpperIntervalBound<Integer>(3);

        Interval<Integer> interval = new Interval<Integer>(lower, upper);

        assertThat(interval.contains(Integer.MIN_VALUE), Matchers.is(true));
        assertThat(interval.contains(0), Matchers.is(true));
        assertThat(interval.contains(4), Matchers.is(false));
    }

    @Test
    public void testNullUpperBoundIntervalContains() {
        LowerIntervalBound<Integer> lower = new LowerIntervalBound<Integer>(1);
        UpperIntervalBound<Integer> upper = new UpperIntervalBound<Integer>(null);

        Interval<Integer> interval = new Interval<Integer>(lower, upper);

        assertThat(interval.contains(0), Matchers.is(false));
        assertThat(interval.contains(1), Matchers.is(true));
        assertThat(interval.contains(Integer.MAX_VALUE), Matchers.is(true));
    }

    @Test
    public void testInvertInclusiveInterval() {
        LowerIntervalBound<Integer> lower = new LowerIntervalBound<Integer>(1);
        lower.setInclusive(true);
        UpperIntervalBound<Integer> upper = new UpperIntervalBound<Integer>(3);
        upper.setInclusive(true);

        Interval<Integer> interval = new Interval<Integer>(lower, upper);
        List<Interval<Integer>> inverted = interval.invert().getIntervals();

        assertThat(inverted.size(), Matchers.is(2));
        Interval<Integer> i0 = inverted.get(0);
        Interval<Integer> i1 = inverted.get(1);

        assertThat(i0.contains(Integer.MIN_VALUE), Matchers.is(true));
        assertThat(i0.contains(0), Matchers.is(true));
        assertThat(i0.contains(1), Matchers.is(false));
        assertThat(i0.contains(Integer.MAX_VALUE), Matchers.is(false));

        assertThat(i1.contains(Integer.MIN_VALUE), Matchers.is(false));
        assertThat(i1.contains(3), Matchers.is(false));
        assertThat(i1.contains(4), Matchers.is(true));
        assertThat(i1.contains(Integer.MAX_VALUE), Matchers.is(true));

    }

    @Test
    public void testInvertExclusiveInterval() {
        LowerIntervalBound<Integer> lower = new LowerIntervalBound<Integer>(1);
        lower.setInclusive(false);
        UpperIntervalBound<Integer> upper = new UpperIntervalBound<Integer>(3);
        upper.setInclusive(false);

        Interval<Integer> interval = new Interval<Integer>(lower, upper);
        List<Interval<Integer>> inverted = interval.invert().getIntervals();

        assertThat(inverted.size(), Matchers.is(2));
        Interval<Integer> i0 = inverted.get(0);
        Interval<Integer> i1 = inverted.get(1);

        assertThat(i0.contains(Integer.MIN_VALUE), Matchers.is(true));
        assertThat(i0.contains(1), Matchers.is(true));
        assertThat(i0.contains(2), Matchers.is(false));
        assertThat(i0.contains(Integer.MAX_VALUE), Matchers.is(false));

        assertThat(i1.contains(Integer.MIN_VALUE), Matchers.is(false));
        assertThat(i1.contains(2), Matchers.is(false));
        assertThat(i1.contains(3), Matchers.is(true));
        assertThat(i1.contains(Integer.MAX_VALUE), Matchers.is(true));
    }

    @Test
    public void testInclusiveIntersectWith() {
        LowerIntervalBound<Integer> lower0 = new LowerIntervalBound<Integer>(0);
        UpperIntervalBound<Integer> upper0 = new UpperIntervalBound<Integer>(5);
        Interval<Integer> interval0 = new Interval<Integer>(lower0, upper0);

        LowerIntervalBound<Integer> lower1 = new LowerIntervalBound<Integer>(1);
        UpperIntervalBound<Integer> upper1 = new UpperIntervalBound<Integer>(3);
        Interval<Integer> interval1 = new Interval<Integer>(lower1, upper1);

        Interval<Integer> interval = interval0.intersectWith(interval1);
        assertThat(interval.contains(0), Matchers.is(false));
        assertThat(interval.contains(1), Matchers.is(true));
        assertThat(interval.contains(2), Matchers.is(true));
        assertThat(interval.contains(3), Matchers.is(true));
        assertThat(interval.contains(4), Matchers.is(false));
    }

    @Test
    public void testExclusiveIntersectWith() {
        LowerIntervalBound<Integer> lower0 = new LowerIntervalBound<Integer>(0);
        lower0.setInclusive(false);
        UpperIntervalBound<Integer> upper0 = new UpperIntervalBound<Integer>(5);
        upper0.setInclusive(false);
        Interval<Integer> interval0 = new Interval<Integer>(lower0, upper0);

        LowerIntervalBound<Integer> lower1 = new LowerIntervalBound<Integer>(1);
        lower1.setInclusive(false);
        UpperIntervalBound<Integer> upper1 = new UpperIntervalBound<Integer>(3);
        upper1.setInclusive(false);
        Interval<Integer> interval1 = new Interval<Integer>(lower1, upper1);

        Interval<Integer> interval = interval0.intersectWith(interval1);
        assertThat(interval.contains(1), Matchers.is(false));
        assertThat(interval.contains(2), Matchers.is(true));
        assertThat(interval.contains(3), Matchers.is(false));
    }

    @Test
    public void testExclusiveAndInclusiveIntersectWith() {
        LowerIntervalBound<Integer> lower0 = new LowerIntervalBound<Integer>(0);
        lower0.setInclusive(false);
        UpperIntervalBound<Integer> upper0 = new UpperIntervalBound<Integer>(5);
        Interval<Integer> interval0 = new Interval<Integer>(lower0, upper0);

        LowerIntervalBound<Integer> lower1 = new LowerIntervalBound<Integer>(0);
        UpperIntervalBound<Integer> upper1 = new UpperIntervalBound<Integer>(5);
        upper1.setInclusive(false);
        Interval<Integer> interval1 = new Interval<Integer>(lower1, upper1);

        Interval<Integer> interval = interval0.intersectWith(interval1);
        assertThat(interval.contains(0), Matchers.is(false));
        assertThat(interval.contains(1), Matchers.is(true));
        assertThat(interval.contains(4), Matchers.is(true));
        assertThat(interval.contains(5), Matchers.is(false));
    }

    @Test
    public void testIsEmpty() {
        LowerIntervalBound<Integer> lower = new LowerIntervalBound<Integer>(1);
        UpperIntervalBound<Integer> upper = new UpperIntervalBound<Integer>(0);
        Interval<Integer> interval = new Interval<Integer>(lower, upper);
        assertThat(interval.isEmpty(), Matchers.is(true));
    }

    @Test
    public void testIsNotEmpty() {
        LowerIntervalBound<Integer> lower = new LowerIntervalBound<Integer>(0);
        UpperIntervalBound<Integer> upper = new UpperIntervalBound<Integer>(1);
        Interval<Integer> interval = new Interval<Integer>(lower, upper);
        assertThat(interval.isEmpty(), Matchers.is(false));
    }

    @Test
    public void testInclusiveExclusiveIsEmpty() {
        LowerIntervalBound<Integer> lower = new LowerIntervalBound<Integer>(1);
        lower.setInclusive(false);
        UpperIntervalBound<Integer> upper = new UpperIntervalBound<Integer>(1);
        Interval<Integer> interval = new Interval<Integer>(lower, upper);
        assertThat(interval.isEmpty(), Matchers.is(true));
    }

    @Test
    public void testExclusiveExclusiveIsEmpty() {
        LowerIntervalBound<Integer> lower = new LowerIntervalBound<Integer>(1);
        lower.setInclusive(false);
        UpperIntervalBound<Integer> upper = new UpperIntervalBound<Integer>(1);
        upper.setInclusive(false);
        Interval<Integer> interval = new Interval<Integer>(lower, upper);
        assertThat(interval.isEmpty(), Matchers.is(true));
    }
}
