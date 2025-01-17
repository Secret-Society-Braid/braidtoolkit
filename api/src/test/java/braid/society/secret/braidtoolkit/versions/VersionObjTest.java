package braid.society.secret.braidtoolkit.versions;


import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class VersionObjTest {

  @Test
  void parseFromStringNotation() {
    VersionObj noPrefix = VersionObj.parse("1.2.3");
    VersionObj withPrefix = VersionObj.parse("v1.2.3");
    VersionObj latter = VersionObj.parse("v1.2.4");
    VersionObj older = VersionObj.parse("v1.2.2");

    assertThat(noPrefix.getMajor()).isEqualTo(1);
    assertThat(noPrefix.getMinor()).isEqualTo(2);
    assertThat(noPrefix.getPatch()).isEqualTo(3);

    assertThat(withPrefix.getMajor()).isEqualTo(1);
    assertThat(withPrefix.getMinor()).isEqualTo(2);
    assertThat(withPrefix.getPatch()).isEqualTo(3);

    // This is valid since we implement Comparable interface.
    assertThat(noPrefix).isEqualTo(withPrefix);

    assertThat(withPrefix).isNotEqualTo(latter);
    assertThat(withPrefix).isNotEqualTo(older);

    assertThat(withPrefix).isGreaterThan(older);
    assertThat(withPrefix).isLessThan(latter);
  }

  @Test
  void parseInvalidStringNotation() {
    IllegalArgumentException lessEx = assertThrows(IllegalArgumentException.class, () -> {
      VersionObj.parse("1.2");
    });
    IllegalArgumentException moreEx = assertThrows(IllegalArgumentException.class, () -> {
      VersionObj.parse("v1.2.3.4");
    });

    assertThat(lessEx).hasMessageThat().contains("Invalid version string: 1.2");
    assertThat(moreEx).hasMessageThat().contains("Invalid version string: v1.2.3.4");
  }

  @Test
  void parseFromAnnotation() {
    VersionObj version = VersionObj.parse(TestClass.class.getAnnotation(Version.class));
    VersionObj older = VersionObj.parse(OlderClass.class.getAnnotation(Version.class));
    VersionObj latter = VersionObj.parse(LatterClass.class.getAnnotation(Version.class));

    assertThat(version).isGreaterThan(older);
    assertThat(version).isLessThan(latter);
  }

  @Test
  void stringNotationTest() {
    VersionObj fromNums = VersionObj.parse(1, 2, 3);
    VersionObj fromString = VersionObj.parse("1.2.3");
    VersionObj fromStringWithPrefix = VersionObj.parse("v1.2.3");
    VersionObj fromAnnotation = VersionObj.parse(TestClass.class.getAnnotation(Version.class));

    final String expected = "1.2.3";
    assertThat(fromNums.asStringNotation()).isEqualTo(expected);
    assertThat(fromString.asStringNotation()).isEqualTo(expected);
    assertThat(fromStringWithPrefix.asStringNotation()).isEqualTo(expected);
    assertThat(fromAnnotation.asStringNotation()).isEqualTo(expected);
  }

  @Test
  void compareAllDifferentParts() {
    VersionObj version1 = VersionObj.parse(1, 2, 3);
    VersionObj version2 = VersionObj.parse(2, 1, 3);
    VersionObj version3 = VersionObj.parse(1, 3, 2);
    VersionObj version4 = VersionObj.parse(1, 2, 4);

    assertThat(version1).isLessThan(version2);
    assertThat(version1).isLessThan(version3);
    assertThat(version1).isLessThan(version4);

    assertThat(version2).isGreaterThan(version1);
    assertThat(version2).isGreaterThan(version3);
    assertThat(version2).isGreaterThan(version4);

    assertThat(version3).isGreaterThan(version1);
    assertThat(version3).isLessThan(version2);
    assertThat(version3).isGreaterThan(version4);

    assertThat(version4).isGreaterThan(version1);
    assertThat(version4).isLessThan(version2);
    assertThat(version4).isLessThan(version3);
  }

  @Version(minor = 2, patch = 2)
  private static class OlderClass {

  }

  @Version(minor = 2, patch = 4)
  private static class LatterClass {

  }

  @Version(minor = 2, patch = 3)
  private static class TestClass {

  }
}
