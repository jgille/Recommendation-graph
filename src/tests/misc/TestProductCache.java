package tests.misc;

import rectest.common.FieldMetadata;
import rectest.common.FieldMetadataImpl;
import rectest.common.FieldSet;
import rectest.common.FieldSetImpl;
import rectest.common.Marshallers;
import rectest.common.PropertyContainer;

import java.io.*;
import java.util.*;

public abstract class TestProductCache {
    private static final FieldMetadata<String> PRODUCT_ID =
        new FieldMetadataImpl<String>("ProductId",
                                      Marshallers.STRING_MARSHALLER,
                                      FieldMetadata.Type.STRING);
    private static final FieldMetadata<String> NAME =
        new FieldMetadataImpl<String>("Name",
                                      Marshallers.STRING_MARSHALLER,
                                      FieldMetadata.Type.STRING);
    private static final FieldMetadata<Float> PRICE =
        new FieldMetadataImpl<Float>("Price",
                                     Marshallers.FLOAT_MARSHALLER,
                                     FieldMetadata.Type.FLOAT);
    private static final FieldMetadata<String> ISBN =
        new FieldMetadataImpl<String>("ISBN",
                                      Marshallers.STRING_MARSHALLER,
                                      FieldMetadata.Type.STRING);
    private static final FieldMetadata<Date> RELEASE_DATE =
        new FieldMetadataImpl<Date>("ReleaseDate",
                                    Marshallers.DATE_MARSHALLER,
                                    FieldMetadata.Type.DATE);
    private static final FieldMetadata<String> LIBRARY_CATEGORY =
        new FieldMetadataImpl<String>("LibraryCategory",
                                      Marshallers.STRING_MARSHALLER,
                                      FieldMetadata.Type.STRING);
    private static final FieldMetadata<String> AVAILABILITY_CODE =
        new FieldMetadataImpl<String>("AvailabilityCode",
                                      Marshallers.STRING_MARSHALLER,
                                      FieldMetadata.Type.STRING);

    private static FieldSet FIELDS =
        new FieldSetImpl.Builder().
        add(PRODUCT_ID).add(NAME).add(PRICE).add(ISBN).add(RELEASE_DATE).
        add(LIBRARY_CATEGORY).add(AVAILABILITY_CODE).build();


    protected void populateCache(String productFile) throws IOException {

        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(productFile);
            br = new BufferedReader(fr);
            String line = null;
            int count = 0;
            while ((line = br.readLine()) != null && count++ < 1000000) {
                PropertyContainer<String> properties =
                    getPropertyContainer(FIELDS);
                String[] values = line.split("\";");
                for(int ordinal = 0; ordinal < FIELDS.size(); ordinal++) {
                    if(values.length <= ordinal)
                        break;
                    String value = values[ordinal];
                    if(value.startsWith("\""))
                        value = value.substring(1, value.length());
                    FieldMetadata<?> fm = FIELDS.getFieldMetadataByOrdinal(ordinal);
                    if(ordinal == 0) {
                        continue;
                    }
                    properties.setProperty(fm.getFieldName(),
                                           fm.getMarshaller().parse(value));
                }
            }
        } finally {
            if (br != null)
                br.close();
            if (fr != null)
                fr.close();
        }
        System.out.println("Done");
        while(true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

    private static interface TrieNode {
        TrieNode getParent();
        void addSuffix(String suffix);
        List<TrieNode> getChildren();
        TrieNode getChild(char c);
        char getChar();
        void setIsNode();
        boolean isNode();
    }

    private static class TrieNodeImpl implements TrieNode {
        private boolean isNode = false;
        private final char c;
        protected TrieNode[] children = null;
        private final TrieNode parent;

        private static final int MAX_NO_CHILDREN = getIndex('z') + 1;

        public TrieNodeImpl(TrieNode parent, char c) {
            this.parent = parent;
            this.c = c;
        }

        public void setIsNode() {
            this.isNode = true;
        }

        public boolean isNode() {
            return isNode;
        }

        public TrieNode getParent() {
            return parent;
        }

        public void addSuffix(String s) {
            if(s.isEmpty())
                return;
            char c = s.charAt(0);
            int index = getIndex(c);
            if (index < 0 || index >= MAX_NO_CHILDREN) {
                String msg = String.format("Charachter: \'%s\' is not allowed!", c);
                System.out.println(msg);
                return;
            }
            if (children == null)
                children = new TrieNode[index + 1];
            else if (children.length <= index) // Extend dimension if neccessary
                children = Arrays.copyOf(children, index + 1);
            TrieNode child = children[index];
            if (child == null) {
                child = new TrieNodeImpl(this, c);
                children[index] = child;
            }
            if (s.length() > 1)
                child.addSuffix(s.substring(1));
            else
                child.setIsNode();
        }

        private static int getIndex(char c) {
            return c - '-';
        }

        public char getChar () {
            return c;
        }

        public TrieNode getChild(char c) {
            if (children == null)
                return null;
            int index = getIndex (c);
            if (index < 0 || index >= children.length)
                return null;
            return children[index];
        }

        public List<TrieNode> getChildren() {
            List<TrieNode> res = new ArrayList<TrieNode>();
            if(children == null)
                return res;
            for (TrieNode child : children)
                if(child != null)
                    res.add(child);
            return res;
        }
    }

    protected abstract PropertyContainer<String> getPropertyContainer(FieldSet fields);
}
