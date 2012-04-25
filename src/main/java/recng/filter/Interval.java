package recng.filter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

public class Interval<T extends Comparable<T>> {

    private final LowerIntervalBound<T> lowerBound;
    private final UpperIntervalBound<T> upperBound;

    public Interval(LowerIntervalBound<T> lowerBound, UpperIntervalBound<T> upperBound) {
        Assert.notNull(lowerBound);
        Assert.notNull(upperBound);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public LowerIntervalBound<T> getLowerBound() {
        return lowerBound;
    }

    public UpperIntervalBound<T> getUpperBound() {
        return upperBound;
    }

    public boolean contains(T value) {
        return lowerBound.accepts(value) && upperBound.accepts(value);
    }

    public Intervals<T> invert() {
        List<Interval<T>> inverted = new ArrayList<Interval<T>>(2);

        if (lowerBound.getBound() != null) {
            LowerIntervalBound<T> l0 = new LowerIntervalBound<T>(null);
            UpperIntervalBound<T> u0 = lowerBound.invert();
            Interval<T> i0 = new Interval<T>(l0, u0);
            inverted.add(i0);
        }

        if (upperBound.getBound() != null) {
            LowerIntervalBound<T> l1 = upperBound.invert();
            UpperIntervalBound<T> u1 = new UpperIntervalBound<T>(null);
            Interval<T> i1 = new Interval<T>(l1, u1);
            inverted.add(i1);
        }

        return new Intervals<T>(inverted);
    }

    public Interval<T> intersectWith(Interval<T> otherInterval) {
        Assert.notNull(otherInterval);
        LowerIntervalBound<T> newLowerBound =
            LowerIntervalBound.max(lowerBound, otherInterval.getLowerBound());
        UpperIntervalBound<T> newUpperBound =
            UpperIntervalBound.min(upperBound, otherInterval.getUpperBound());
        return new Interval<T>(newLowerBound, newUpperBound);
    }

    public boolean isEmpty() {
        T lower = lowerBound.getBound();
        T upper = upperBound.getBound();

        if (lower == null || upper == null) { // null is MIN/MAX
            return false;
        }

        int comp = lower.compareTo(upper);

        if (comp == 0) {
            return !(lowerBound.isInclusive() && upperBound.isInclusive());
        }

        return comp > 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((lowerBound == null) ? 0 : lowerBound.hashCode());
        result = prime * result + ((upperBound == null) ? 0 : upperBound.hashCode());
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Interval<T> other = (Interval<T>) obj;
        if (lowerBound == null) {
            if (other.lowerBound != null) {
                return false;
            }
        } else if (!lowerBound.equals(other.lowerBound)) {
            return false;
        }
        if (upperBound == null) {
            if (other.upperBound != null) {
                return false;
            }
        } else if (!upperBound.equals(other.upperBound)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Interval [lowerBound=" + lowerBound + ", upperBound=" + upperBound + "]";
    }
}
