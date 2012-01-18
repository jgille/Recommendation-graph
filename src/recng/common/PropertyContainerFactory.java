package recng.common;

/**
 * A factory used to create (empty) {@link PropertyContainer}s.
 * 
 * @author jon
 * 
 */
public interface PropertyContainerFactory {

    PropertyContainer create();

    PropertyContainer create(TableMetadata metadata);

}
