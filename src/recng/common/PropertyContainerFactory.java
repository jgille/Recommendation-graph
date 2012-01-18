package recng.common;

public interface PropertyContainerFactory {

    PropertyContainer create();

    PropertyContainer create(TableMetadata metadata);

}
