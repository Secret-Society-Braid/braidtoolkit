package braid.society.secret.braidtoolkit.versions;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class VersionTest {

  @Version(major = 1, minor = 0, patch = 0)
  private static class TestClass1 {

  }

  @Version(major = 2, minor = 5, patch = 3)
  private static class TestClass2 {

  }

  @Test
  void testMajorVersionIsOne() {
    Version version = TestClass1.class.getAnnotation(Version.class);

    assertThat(version.major()).isEqualTo(1);
    assertThat(version.minor()).isEqualTo(0);
    assertThat(version.patch()).isEqualTo(0);
  }

  @Test
  void testMajorVersionIsTwo() {
    Version version = TestClass2.class.getAnnotation(Version.class);

    assertThat(version.major()).isEqualTo(2);
    assertThat(version.minor()).isEqualTo(5);
    assertThat(version.patch()).isEqualTo(3);
  }
}