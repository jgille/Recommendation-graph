package recng.filter;

public class AbstractIntervalBound<T extends Comparable<T>> {

    private boolean inclusive = true;
    private final T bound;

    public AbstractIntervalBound(T bound) {
        this.bound = bound;
    }

    public boolean isInclusive() {
        return inclusive;
    }

    public void setInclusive(boolean inclusive) {
        this.inclusive = inclusive;
    }

    public T getBound() {
        return bound;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bound == null) ? 0 : bound.hashCode());
        result = prime * result + (inclusive ? 1231 : 1237);
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
        AbstractIntervalBound<T> other = (AbstractIntervalBound<T>) obj;
        if (bound == null) {
            if (other.bound != null) {
                return false;
            }
        } else if (!bound.equals(other.bound)) {
            return false;
        }
        if (inclusive != other.inclusive) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AbstractIntervalBound [inclusive=" + inclusive + ", bound=" + bound + "]";
    }

}
