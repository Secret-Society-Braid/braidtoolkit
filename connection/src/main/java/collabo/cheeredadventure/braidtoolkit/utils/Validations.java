package collabo.cheeredadventure.braidtoolkit.utils;

import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Validations {

  public static boolean checkPositive(long value) {
    return checkOrElseThrow((v) -> v > 0, value, () -> String.format("Value must be positive, but was: %d", value));
  }

  public static <T> boolean checkOrElseThrow(Predicate<T> predicate, T input, Supplier<String> messageSupplier) {
    if (!predicate.test(input)) {
      throw new IllegalArgumentException(messageSupplier.get());
    }
    return true;
  }

}
