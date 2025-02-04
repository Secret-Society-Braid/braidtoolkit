package braid.society.secret.braidtoolkit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An alternative functionality is provided by another element and should be used instead.
 * <p>
 * Alternative elements has usually no changes in their behavior, but changed their name or location, or may have more concise or efficient way to achieve the same goal.
 *
 * @see ForRemoval
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface Alternative {

  /**
   * The name of the alternative element.
   */
  String value();
}
