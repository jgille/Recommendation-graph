package tests.misc;

import rectest.cache.*;
import rectest.common.BinPropertyContainer;
import rectest.common.FieldMetadata;
import rectest.common.FieldMetadataImpl;
import rectest.common.FieldSet;
import rectest.common.FieldSetImpl;
import rectest.common.Marshallers;
import rectest.common.WeightedPropertyContainer;
import rectest.index.*;

import java.io.*;
import java.util.*;

public class ProductIdTester {

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

    public static void main(String[] args) throws IOException {
        new ProductIdTester().cache(args[0]);
    }

    private void cache(String fileName) throws IOException {
        Weigher<Key<String>, WeightedPropertyContainer<String>> weigher =
            new Weigher<Key<String>, WeightedPropertyContainer<String>>() {
            public int weigh(int overhead, Key<String> key,
            				 WeightedPropertyContainer<String> props) {
                return overhead + 40 + props.getWeight();
            }
        };
        Cache<Key<String>, WeightedPropertyContainer<String>> cache =
            new CacheBuilder<Key<String>, WeightedPropertyContainer<String>>().
            maxWeight(Runtime.getRuntime().maxMemory() / 5).weigher(weigher).build();

        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);
            String line = null;
            int count = 0;
            while ((line = br.readLine()) != null && count++ < 10000000) {
                String[] values = line.split("\";");
                if(values.length == 0)
                    continue;
                WeightedPropertyContainer<String> properties = getPropertyContainer(FIELDS);
                Key<String> key = null;
                for(int ordinal = 0; ordinal < FIELDS.size(); ordinal++) {
                    if(values.length <= ordinal)
                        break;
                    String value = values[ordinal];
                    if(value.startsWith("\""))
                        value = new String(value.substring(1, value.length()));
                    FieldMetadata<?> fm = FIELDS.getFieldMetadataByOrdinal(ordinal);
                    if(ordinal == 0)
                        key = StringKeys.parseKey(value);
                    if (ordinal < 3)
                        properties.setProperty(fm.getFieldName(),
                                               fm.getMarshaller().parse(value));

                }
                if (key != null)
                    cache.cache(key, properties);
                if ((count % 100000) == 0) {
                    System.out.println(String.format("Done for %s rows", count));
                    System.out.println(cache);
                }
            }
        } finally {
            if (br != null)
                br.close();
            if (fr != null)
                fr.close();
        }
        System.out.println(String.format("Cached %s products", cache.size()));
        while(true) {
            try {
                Thread.sleep(10000);
                System.out.println(cache);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

    private WeightedPropertyContainer<String> getPropertyContainer(FieldSet fields) {
        return BinPropertyContainer.build(fields, true);
    }
}
