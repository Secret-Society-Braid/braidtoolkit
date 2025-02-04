package braid.society.secret.braidtoolkit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a {@link Deprecated} element as scheduled for removal in a future version.
 * <p>
 * You should not use the functionality marked with this annotation anymore in new code.
 *
 * @see Deprecated
 * @see DeprecatedSince
 * @see Alternative
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface ForRemoval {

  /**
   * The future version in which annotated element is scheduled for removal.
   * @return The version string in which the annotated element is scheduled for removal or "none" if not specified.
   */
  String deadline() default "none";
}
