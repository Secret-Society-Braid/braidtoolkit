package collabo.cheeredadventure.braidtoolkit.ratelimit;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Balances the rate of concurrent executions of tasks based on a token bucket algorithm and an hourly request counter.(fixed window counter).
 * <p>
 * This class is <strong>NOT</strong> respecting its task results; it only limits the number of tasks executed per hour and the rate of concurrent executions.
 * <p>
 * This class was designed with the collaboration of the community project: Cheered Adventure.
 * It is a different community project, but doing the same efforts to achieve the same goal.
 * Big thanks to the community for their contributions and support.
 *
 * @author Ranfa
 * @author All contributors of the community project: Cheered Adventure
 * @see TokenBucket
 * @see HourlyRequestCounter
 */
@Slf4j
public class RateLimit {

  @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
  @Getter(AccessLevel.PACKAGE)
  private static class CompletableFutureWrapper<V> {
    final Callable<V> task;
    final CompletableFuture<V> future;
  }

  private final TokenBucket tokenBucket;
  private final HourlyRequestCounter requestCounter;

  private final Queue<CompletableFutureWrapper<?>> delayedQueue = new ConcurrentLinkedQueue<>();
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final ExecutorService taskExecutor;

  private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

  /**
   * Constructs a new {@link RateLimit} instance with the detailed parameters.
   * @param tokenBucketCapacity the maximum number of tokens that allow requests to be processed
   * @param tokenBucketRefillRate the number of tokens to refill per period
   * @param tokenBucketRefillPeriodMillis the period in milliseconds to refill the token bucket
   * @param maxRequestsPer the maximum number of requests allowed per hour
   * @param taskExecutorPoolSize the size of the thread pool for executing tasks
   */
  public RateLimit(long tokenBucketCapacity, long tokenBucketRefillRate, long tokenBucketRefillPeriodMillis, long maxRequestsPer, int taskExecutorPoolSize) {
    this(
      new TokenBucket(tokenBucketCapacity, tokenBucketRefillRate, tokenBucketRefillPeriodMillis),
      new HourlyRequestCounter(maxRequestsPer),
      taskExecutorPoolSize
    );
  }

  /**
   * Constructs a new {@link RateLimit} instance by principle of dependency injection.
   * @param tokenBucket the customized {@link TokenBucket} that allows requests to be processed
   * @param requestCounter the customized {@link HourlyRequestCounter} that limits the number of requests per hour
   * @param taskExecutorPoolSize the size of the thread pool for executing tasks
   */
  public RateLimit(TokenBucket tokenBucket, HourlyRequestCounter requestCounter, int taskExecutorPoolSize) {
    this.tokenBucket = tokenBucket;
    this.requestCounter = requestCounter;
    this.taskExecutor = Executors.newFixedThreadPool(taskExecutorPoolSize);

    this.scheduler.scheduleAtFixedRate(this::processDelayedQueue, 0, tokenBucket.getRefillPeriodMillis() / 2, TimeUnit.MILLISECONDS);
  }

  /**
   * Submits a task for execution that needs to be rate-limited.
   * <p>
   * Tasks will be automatically delayed or rejected based on the rate limit configuration.
   * @param task the task to be executed, which needs to be rate-limited.
   * @return a {@link CompletableFuture} that will be completed with the result of the task or an exception if the limit is reached.
   * @param <V> the type of the result returned by the task
   */
  public <V> CompletableFuture<V> submit(Callable<V> task) {
    CompletableFuture<V> future = new CompletableFuture<>();
    CompletableFutureWrapper<V> wrapper = new CompletableFutureWrapper<>(task, future);

    if(isShuttingDown.get()) {
      future.completeExceptionally(
        new RejectedExecutionException("RateLimit is shutting down; no new tasks are accepted.")
      );
      return future;
    }

    // 1. Check request limits per hour
    if(!requestCounter.checkAndIncrement()) {
      future.completeExceptionally(
        new RateLimitExceededException(String.format("Hourly rate limit exceeded. current count: %d", requestCounter.getCurrentCount()))
      );
      return future;
    }

    // 2. Check token availability
    if(tokenBucket.tryConsume(1)) {
      // execute task immediately if tokens are available
      executeTask(wrapper);
    } else {
      // otherwise, add to delayed queue
      if(isShuttingDown.get()) {
        future.completeExceptionally(
          new RejectedExecutionException("RateLimit is shutting down; task rejected during delay queue submission.")
        );
        return future;
      }
      delayedQueue.offer(wrapper);
      log.debug("task delayed. Queue size: {} Current tokens: {}", delayedQueue.size(), tokenBucket.getTokens());
    }
    return future;
  }

  /**
   * Try to execute the task polled from the delayed queue.
   * <p>
   * This method will be called periodically to process tasks by schedulers.
   */
  private void processDelayedQueue() {
    if(isShuttingDown.get()) {
      log.debug("RateLimit is shutting down; no further processing of delayed tasks.");
      return;
    }

    CompletableFutureWrapper<?> wrapper = delayedQueue.poll();
    if(wrapper != null) {
      // Check whether future is already completed.
      if(wrapper.getFuture().isDone()) {
        return;
      }

      try {
        if(tokenBucket.tryConsume(1)) {
          executeTask(wrapper);
        } else {
          // re-add to the queue if no tokens are available
          delayedQueue.offer(wrapper);
        }
      } catch (Exception e) {
        // unexpected exception during task execution
        wrapper.getFuture().completeExceptionally(e);
        log.error("Unexpected exception during delayed task execution", e);
      }
    }
  }

  /**
   * Actual execution of the task, which runs parallelly in the task executor.
   *
   * @param wrapper the task wrapper containing the task and its future
   * @param <V> the type of the result returned by the task
   */
  private <V> void executeTask(CompletableFutureWrapper<V> wrapper) {
    if(isShuttingDown.get()) {
      wrapper.getFuture().completeExceptionally(
        new RejectedExecutionException("RateLimit is shutting down; task rejected just before its execution.")
      );
      return;
    }
    if(wrapper.getFuture().isDone()) {
      // if future is already completed, do not execute the task
      return;
    }

    // submit the task to the executor service
    taskExecutor.submit(() -> {
      try {
        V result = wrapper.getTask().call();
        wrapper.getFuture().complete(result);
      } catch (Exception e) {
        wrapper.getFuture().completeExceptionally(e);
      }
    });
  }

  /**
   * Shuts down the rate limit service, releasing all resources and stopping the scheduler.
   * <p>
   * All pending tasks will be cancelled; they will be rejected.
   */
  public void shutdown() {
    isShuttingDown.set(true);

    scheduler.shutdown();
    try {
      if(!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      log.error("Unexpected exception during shutting down the scheduler", e);
      Thread.currentThread().interrupt();
    } finally {
      scheduler.shutdownNow();
    }

    taskExecutor.shutdown();
    try {
      if(!taskExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
        taskExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
      log.error("Unexpected exception during shutting down the task executor", e);
      Thread.currentThread().interrupt();
    } finally {
      taskExecutor.shutdownNow();
    }

    // cancel all delayed tasks
    int cancelledCount = 0;
    CompletableFutureWrapper<?> remainingWrapper;
    while((remainingWrapper = delayedQueue.poll()) != null) {
      if(!remainingWrapper.getFuture().isDone()) {
        // notify cancellation with completing exceptionally
        remainingWrapper.getFuture().completeExceptionally(
          new RejectedExecutionException("Task was cancelled due to RateLimit shutdown."));
        log.warn("Task in delayed queue was cancelled due to shutdown: {}", remainingWrapper.getTask().toString());
        cancelledCount++;
      }
    }
    log.info("RateLimit shutdown completed. Cancelled {} tasks from delayed queue.", cancelledCount);
  }
}
