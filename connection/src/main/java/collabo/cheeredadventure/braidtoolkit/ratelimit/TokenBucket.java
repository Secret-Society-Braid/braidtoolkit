package collabo.cheeredadventure.braidtoolkit.ratelimit;

import collabo.cheeredadventure.braidtoolkit.utils.Validations;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of a standard token bucket rate limiting algorithm.
 * This class allows you to create a token bucket with a specified capacity, and completely hides you from the details of the algorithm, which completes usability of token bucket.
 *
 * @author Ranfa
 * @author All contributors of the community project: Cheered Adventure
 */
@Slf4j
public class TokenBucket {

  private final long capacity;
  private final long refillRate;
  @Getter(AccessLevel.PACKAGE)
  private final long refillPeriodMillis;

  private final AtomicLong tokens;
  private volatile long lastRefillTimestamp;

  /**
   * Constructs a new {@link TokenBucket} instance with the specified parameters.
   *
   * @param capacity the maximum number of tokens in the bucket
   * @param refillRate the number of tokens to add to the bucket per refill period
   * @param refillPeriodMillis the period in milliseconds for refilling tokens
   */
  public TokenBucket(long capacity, long refillRate, long refillPeriodMillis) {
    Validations.checkPositive(capacity);
    Validations.checkPositive(refillRate);
    Validations.checkPositive(refillPeriodMillis);

    this.capacity = capacity;
    this.refillRate = refillRate;
    this.refillPeriodMillis = refillPeriodMillis;
    this.tokens = new AtomicLong(capacity);
    this.lastRefillTimestamp = System.currentTimeMillis();
  }

  /**
   * Constructs a new {@link TokenBucket} instance with the specified parameters but using more flexible time unit for the refill period.
   *
   * @param capacity the maximum number of tokens in the bucket
   * @param refillRate the number of tokens to add to the bucket per refill period
   * @param refillPeriod the period for refilling tokens
   * @param refillPeriodUnit the time unit of the refill period
   */
  public TokenBucket(long capacity, long refillRate, long refillPeriod, TimeUnit refillPeriodUnit) {
    this(capacity, refillRate, refillPeriodUnit.toMillis(refillPeriod));
  }

  boolean tryConsume(final long tokensToConsume) {
    Validations.checkPositive(tokensToConsume);
    refillTokens();

    while(true) {
      long currentTokens = tokens.get();
      if(currentTokens >= tokensToConsume) {
        if(tokens.compareAndSet(currentTokens, currentTokens - tokensToConsume)) {
          return true;
        }
      } else {
        log.debug("Not enough tokens available. Current: {}, Required: {}", currentTokens, tokensToConsume);
        return false;
      }
    }
  }

  private void refillTokens() {
    long now = Instant.now().toEpochMilli();
    long elapsed = now - lastRefillTimestamp;
    if(elapsed < refillPeriodMillis) {
      return; // Not enough time has passed to refill
    }
    long refillCount = elapsed / refillPeriodMillis;
    long tokensToAdd = refillCount * refillRate;
    if (tokensToAdd <= 0) {
      return;
    }
    long currentTokens = tokens.get();
    long newTokens = Math.min(currentTokens + tokensToAdd, capacity);
    if(tokens.compareAndSet(currentTokens, newTokens)) {
      lastRefillTimestamp = now;
    }
  }

  long getTokens() {
    refillTokens();
    return tokens.get();
  }
}
