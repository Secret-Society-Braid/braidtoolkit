package braid.society.secret.braidtoolkit.versions;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
@Builder(setterPrefix = "set")
public class VersionObj implements Comparable<VersionObj> {

  private final int major;
  private final int minor;
  private final int patch;

  public static VersionObj parse(int major, int minor, int patch) {
    return new VersionObj(major, minor, patch);
  }

  public static VersionObj parse(String version) {
    String[] parts = version.replace("v", "").split("\\.");
    if (parts.length != 3) {
      throw new IllegalArgumentException("Invalid version string: " + version);
    }
    return parse(
        Integer.parseInt(parts[0]),
        Integer.parseInt(parts[1]),
        Integer.parseInt(parts[2])
    );
  }

  public static VersionObj parse(Version versionAnnotation) {
    return parse(
        versionAnnotation.major(),
        versionAnnotation.minor(),
        versionAnnotation.patch()
    );
  }

  public String asStringNotation() {
    return String.format("%d.%d.%d", major, minor, patch);
  }

  @Override
  public int compareTo(VersionObj other) {
    if (major != other.major) {
      return Integer.compare(major, other.major);
    }
    if (minor != other.minor) {
      return Integer.compare(minor, other.minor);
    }
    return Integer.compare(patch, other.patch);
  }

}
