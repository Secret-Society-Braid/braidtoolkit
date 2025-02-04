package braid.society.secret.braidtoolkit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark an element as deprecated since a specific version.
 * It must be used in combination with {@link Deprecated} to mark actually deprecated elements.
 *
 * @see Deprecated
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface DeprecatedSince {

  /**
   * @return The version since the element is marked as deprecated.
   */
  String value();
}
