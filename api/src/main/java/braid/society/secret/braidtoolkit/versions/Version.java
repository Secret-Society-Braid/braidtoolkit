package braid.society.secret.braidtoolkit.versions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Version {
  int major() default 1;
  int minor() default 0;
  int patch() default 0;
}
