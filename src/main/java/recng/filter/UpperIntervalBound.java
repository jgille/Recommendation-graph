package recng.filter;

import org.springframework.util.Assert;

public class UpperIntervalBound<T extends Comparable<T>> extends AbstractIntervalBound<T> implements
    Comparable<UpperIntervalBound<T>> {

    public UpperIntervalBound(T bound) {
        super(bound);
    }

    @Override
    public int compareTo(UpperIntervalBound<T> other) {
        Assert.notNull(other);
        T b0 = getBound();
        T b1 = other.getBound();

        if (b0 == null && b1 == null) {
            return 0;
        }

        if (b0 == null) {
            return 1; // null is treated as MAX value
        }

        if (b1 == null) {
            return -1; // null is treated as MAX value
        }

        int comp = b0.compareTo(b1);

        if (comp != 0) {
         return comp;
        }

        if (isInclusive() && other.isInclusive()) {
            return 0;
        }

        if (isInclusive()) {
            return 1;
        }

        return -1;
    }

    public boolean accepts(T value) {
        Assert.notNull(value);
        T upperBound = getBound();
        if (upperBound == null) {
            return true; // null is treated as MAX value
        }

        int comp = upperBound.compareTo(value);

        if (comp > 0) {
            return true;
        }

        if (comp < 0) {
            return false;
        }

        return isInclusive();
    }

    public LowerIntervalBound<T> invert() {
        LowerIntervalBound<T> invertedBound = new LowerIntervalBound<T>(getBound());
        invertedBound.setInclusive(!isInclusive());
        return invertedBound;
    }

    public static <T extends Comparable<T>> UpperIntervalBound<T> min(UpperIntervalBound<T> b0,
                                                                      UpperIntervalBound<T> b1) {
        Assert.notNull(b0);
        Assert.notNull(b1);
        int comp = b0.compareTo(b1);
        if (comp <= 0) {
            return b0;
        }
        return b1;
    }

    @Override
    public String toString() {
        return "UpperIntervalBound [isInclusive()=" + isInclusive() + ", getBound()=" + getBound()
            + "]";
    }

}
