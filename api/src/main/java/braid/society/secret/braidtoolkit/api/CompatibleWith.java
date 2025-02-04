package braid.society.secret.braidtoolkit.api;

/**
 * Interface for converting one object to another.
 * <p>
 * This interface is intended to tell IDEs explicitly that a class can be converted to another class,
 * which means that IDE's code completion can be used to find the convert method.
 * @param <T> The type of object to convert to.
 */
public interface CompatibleWith<T> {

  /**
   * Convert the object to another object.
   * @return The converted object that based on base object data.
   */
  T convert();
}
