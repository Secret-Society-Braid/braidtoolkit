package collabo.cheeredadventure.braidtoolkit.ratelimit;

public class RateLimitExceededException extends RuntimeException {

  public RateLimitExceededException(String message) {
    super(message);
  }
}
