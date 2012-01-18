package recng.db.nio;

import java.nio.ByteBuffer;

import recng.db.AbstractBinKVStore;
import recng.db.ByteSequence;
import recng.db.KVStore;

public class ByteBufferKVStore extends AbstractBinKVStore implements
    KVStore<String, byte[]> {

    @Override
    protected ByteSequence createStorage() {
        int s = getShardCount();
        int e = Math.min(4 + s, 11);
        // Capacity ranges from 16M to 2G
        int capacity = (int) Math.pow(2, e) * 1024 * 1024;
        return new Sequence(capacity);
    }

    private static class Sequence implements ByteSequence {

        private final ByteBuffer buffer;

        private Sequence(int capacity) {
            this.buffer = ByteBuffer.allocate(capacity);
        }

        @Override
        public int append(byte[] data) {
            int position = buffer.position();
            buffer.put(data);
            return position;
        }

        @Override
        public byte[] read(int offset, int length) {
            byte[] data = new byte[length];
            int position = buffer.position();
            try {
                buffer.position(offset);
                buffer.get(data);
            } finally {
                buffer.position(position);
            }
            return data;
        }

        @Override
        public int lenght() {
            return buffer.limit() - buffer.remaining();
        }

        @Override
        public int capacity() {
            return buffer.limit();
        }
    }
}
