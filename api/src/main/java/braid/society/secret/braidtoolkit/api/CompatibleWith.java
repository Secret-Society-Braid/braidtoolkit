package braid.society.secret.braidtoolkit.api;

/**
 * Interface for converting one object to another.
 * <p>
 * This can achieve when we want to convert each other between two concrete classes for example.
 * @param <T> The type of object to convert to.
 */
public interface CompatibleWith<T> {

  /**
   * Convert the object to another object.
   * @return The converted object that based on base object data.
   */
  T convert();
}
