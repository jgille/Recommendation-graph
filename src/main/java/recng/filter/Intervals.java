package recng.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;

public class Intervals<T extends Comparable<T>> {

    private final List<Interval<T>> intervals;

    public Intervals() {
        this(new ArrayList<Interval<T>>());
    }

    public Intervals(List<Interval<T>> intervals) {
        Assert.notNull(intervals);
        this.intervals = new ArrayList<Interval<T>>(intervals);
    }

    public List<Interval<T>> getIntervals() {
        return Collections.unmodifiableList(intervals);
    }

    public void addInterval(Interval<T> interval) {
        intervals.add(interval);
    }

    public boolean contains(T value) {
        Assert.notNull(value);
        for (Interval<T> interval : intervals) {
            if (interval.contains(value)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        for (Interval<T> interval : intervals) {
            if (!interval.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public Intervals<T> intersectWith(Intervals<T> other) {
        List<Interval<T>> otherIntervals = other.getIntervals();
        List<Interval<T>> intersections = new ArrayList<Interval<T>>();

        for (Interval<T> interval : intervals) {
            for (Interval<T> otherInterval : otherIntervals) {
                Interval<T> intersection = interval.intersectWith(otherInterval);
                if (!intersection.isEmpty()) {
                    intersections.add(intersection);
                }
            }
        }
        Intervals<T> res = new Intervals<T>(intersections);
        return res;
    }

    @Override
    public String toString() {
        return "Intervals [intervals=" + intervals + "]";
    }

}
