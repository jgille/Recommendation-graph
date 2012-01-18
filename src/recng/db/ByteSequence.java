package recng.db;

public interface ByteSequence {

    /**
     * Appends data to the sequence.
     *
     * @return The offset in the sequence at which the data was written.
     */
    int append(byte[] data);

    /**
     * Reads data from the sequence.
     */
    byte[] read(int offset, int length);

    /**
     * Gets the length of this sequence.
     */
    int lenght();

    /**
     * Gets the capacity of this sequence.
     */
    int capacity();
}
