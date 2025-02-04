package braid.society.secret.braidtoolkit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The elements marked with this element are considered work-in-progress and may be changed their behavior in future versions.
 * <p>
 * Basically, you may use the functionality marked with this annotation, but be aware to check their documentation and changelogs for updates.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface Experimental {
}
