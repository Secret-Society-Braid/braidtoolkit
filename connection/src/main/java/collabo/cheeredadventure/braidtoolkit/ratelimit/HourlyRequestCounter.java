package collabo.cheeredadventure.braidtoolkit.ratelimit;

import collabo.cheeredadventure.braidtoolkit.utils.Validations;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.Queue;

public class HourlyRequestCounter {

  private final long maxRequestsPer;
  private final Queue<Instant> timestamps;
  private static final ChronoUnit RECURRING_UNIT = ChronoUnit.HOURS;

  public HourlyRequestCounter(final long maxRequestsPer) {
    Validations.checkPositive(maxRequestsPer);
    this.maxRequestsPer = maxRequestsPer;
    this.timestamps = new LinkedList<>();
  }

  synchronized boolean checkAndIncrement() {
    Instant now = Instant.now();
    pollingQueue(now);
    if(timestamps.size() < maxRequestsPer) {
      timestamps.offer(now);
      return true;
    }
    return false;
  }

  synchronized long getCurrentCount() {
    Instant now = Instant.now();
    pollingQueue(now);
    return timestamps.size();
  }

  private void pollingQueue(Instant now) {
    while(!timestamps.isEmpty() && timestamps.peek().isBefore(now.minus(1, RECURRING_UNIT))) {
      timestamps.poll();
    }
  }
}
