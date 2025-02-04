package braid.society.secret.braidtoolkit.api;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

public class CompatibleWithTest {

  @Getter
  @RequiredArgsConstructor
  static class ConcreteClass implements CompatibleWith<MockDocument> {
    private final long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String password;

    @Override
    public MockDocument convert() {
      String fullname = firstName + " " + lastName;
      String mailPasswordHashed = Hashing.sha256().hashString(email + password, StandardCharsets.UTF_8).toString();
      return new MockDocument(String.valueOf(id), fullname, email, mailPasswordHashed);
    }
  }

  @Getter
  @RequiredArgsConstructor
  static class MockDocument {
    private final String id;
    private final String name;
    private final String email;
    private final String hash;
  }

  @Test
  void testConvert() {
    String email = "johndoe@example.com";
    String password = "password";
    CompatibleWith<MockDocument> concrete = new ConcreteClass(1L, "John", "Doe", email, password);
    MockDocument document = concrete.convert();

    assertThat(document.getId()).isEqualTo("1");
    assertThat(document.getName()).isEqualTo("John Doe");
    String expectedHash = Hashing.sha256().hashString(email + password, StandardCharsets.UTF_8).toString();
    assertThat(document.getHash()).isEqualTo(expectedHash);
  }
}
