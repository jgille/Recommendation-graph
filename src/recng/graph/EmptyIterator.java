package recng.graph;

import java.util.Iterator;

/**
 * An ever empty iterator.
 * 
 * @author jon
 * 
 * @param <E>
 */
class EmptyIterator<E> implements Iterator<E> {
    public boolean hasNext() {
        return false;
    }

    public E next() {
        return null;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}