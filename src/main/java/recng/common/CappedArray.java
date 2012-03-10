package recng.common;

import java.util.ArrayList;
import java.util.List;

/**
 * A capped array, i.e. a fixed size array. When the size limit is reached, the
 * oldest element is kicked out to make room for the new element.
 *
 * This class is thread safe.
 * 
 * @author jon
 * 
 * @param <E>
 *            The generic type of the elements in this array.
 */
public class CappedArray<E> {

    private final int maxSize;
    private int head;
    private int tail;
    private final List<E> elements;
    private int size = 0;

    public CappedArray(int maxSize) {
        this.maxSize = maxSize;
        this.head = 0;
        this.tail = 0;
        this.elements = new ArrayList<E>(maxSize);
        for (int i = 0; i < maxSize; i++)
            elements.add(null);
    }

    /**
     * Returns a list containing all the elements of the capped array.
     */
    public List<E> asList() {
        List<E> copy;
        int headSnapshot, tailSnapshot;
        synchronized (this) {
            copy = new ArrayList<E>(elements);
            headSnapshot = head;
            tailSnapshot = tail;
        }
        List<E> res = new ArrayList<E>(copy.size());
        int endIndex = copy.size() - 1;
        if (headSnapshot > 0) {
            int to = Math.max(endIndex, tailSnapshot) + 1;
            res.addAll(copy.subList(headSnapshot, to));
            res.addAll(copy.subList(0, headSnapshot));
        } else {
            res.addAll(copy.subList(0, tailSnapshot));
        }
        return res;
    }

    /**
     * Applies the procedure for all elements in this array.
     */
    public void forEach(Procedure<E> proc) {
        for (E element : asList())
            proc.apply(element);
    }

    /**
     * Gets the current size of this array.
     */
    public synchronized int size() {
        return size;
    }

    /**
     * Pushes an element to the array.
     */
    public synchronized void push(E element) {
        if (element == null)
            throw new IllegalArgumentException("Null elements not allowed.");
        if (size == maxSize) {
            head++;
        } else {
            size++;
        }
        if (head == maxSize) {
            head = 0;
            tail = maxSize - 1;
        } else if (tail == maxSize) {
            head = 1;
            tail = 0;
        }
        elements.set(tail, element);
        tail++;
    }
}
