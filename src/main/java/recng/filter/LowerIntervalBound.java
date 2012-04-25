package recng.filter;

import org.springframework.util.Assert;

public class LowerIntervalBound<T extends Comparable<T>> extends AbstractIntervalBound<T> implements
    Comparable<LowerIntervalBound<T>> {

    public LowerIntervalBound(T bound) {
        super(bound);
    }

    @Override
    public int compareTo(LowerIntervalBound<T> other) {
        Assert.notNull(other);
        T b0 = getBound();
        T b1 = other.getBound();

        if (b0 == null && b1 == null) {
            return 0;
        }

        if (b0 == null) {
            return -1; // null is treated as MIN value
        }

        if (b1 == null) {
            return 1; // null is treated as MIN value
        }

        int comp = b0.compareTo(b1);

        if (comp != 0) {
         return comp;
        }

        if (isInclusive() && other.isInclusive()) {
            return 0;
        }

        if (isInclusive()) {
            return -1;
        }

        return 1;
    }

    public boolean accepts(T value) {
        Assert.notNull(value);
        T lowerBound = getBound();
        if (lowerBound == null) {
            return true; // null is treated as MIN value
        }

        int comp = lowerBound.compareTo(value);

        if (comp > 0) {
            return false;
        }

        if (comp < 0) {
            return true;
        }

        return isInclusive();
    }

    public UpperIntervalBound<T> invert() {
        UpperIntervalBound<T> invertedBound = new UpperIntervalBound<T>(getBound());
        invertedBound.setInclusive(!isInclusive());
        return invertedBound;
    }

    public static <T extends Comparable<T>> LowerIntervalBound<T> max(LowerIntervalBound<T> b0,
                                                                     LowerIntervalBound<T> b1) {
        Assert.notNull(b0);
        Assert.notNull(b1);
        int comp = b0.compareTo(b1);
        if (comp >= 0) {
            return b0;
        }
        return b1;
    }

    @Override
    public String toString() {
        return "LowerIntervalBound [isInclusive()=" + isInclusive() + ", getBound()=" + getBound()
            + "]";
    }

}
