package edu.bu.met.cs665.pool;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class HttpClientPool {
  private static final Logger logger = LogManager.getLogger(HttpClientPool.class);
  private final BlockingQueue<CloseableHttpClient> pool;
  private final int maxPoolSize;
  private final int timeout;

  /**
   * Creates a new HttpClientPool with the specified parameters.
   *
   * @param poolSize Maximum number of clients in the pool
   * @param timeout Timeout in seconds for acquiring a client
   */
  public HttpClientPool(int poolSize, int timeout) {
    this.maxPoolSize = poolSize;
    this.timeout = timeout;
    this.pool = new ArrayBlockingQueue<>(poolSize);

    this.initializePool();

    logger.info("HTTP Client Pool initialized with size: {}", poolSize);
  }

  /**
   * Initializes the pool with HTTP clients.
   */
  private void initializePool() {
    for (int i = 0; i < maxPoolSize; i++) {
      try {
        CloseableHttpClient client = HttpClients.createDefault();
        pool.offer(client);
      } catch (Exception e) {
        logger.error("Error creating HTTP client for pool", e);
      }
    }
  }

  /**
   * Acquires an HTTP client from the pool.
   * If no client is available, waits up to the timeout period.
   *
   * @return A CloseableHttpClient from the pool
   * @throws InterruptedException if interrupted while waiting
   * @throws IllegalStateException if no client available within timeout
   */
  public CloseableHttpClient acquire() throws InterruptedException {
    logger.debug("Acquiring HTTP client from pool. Pool size: {}", pool.size());

    CloseableHttpClient client = pool.poll(timeout, TimeUnit.SECONDS);

    if (client == null) {
      logger.error("Timeout acquiring HTTP client from pool");
      throw new IllegalStateException(
          "No HTTP client available within timeout period"
      );
    }

    logger.debug("HTTP client acquired. Remaining in pool: {}", pool.size());
    return client;
  }

  /**
   * Releases an HTTP client back to the pool.
   *
   * @param client The client to release
   */
  public void release(CloseableHttpClient client) {
    if (client != null) {
      boolean returned = pool.offer(client);
      if (returned) {
        logger.debug("HTTP client returned to pool. Pool size: {}", pool.size());
      } else {
        logger.warn("Failed to return client to pool (pool full)");
        try {
          client.close();
        } catch (IOException e) {
          logger.error("Error closing HTTP client", e);
        }
      }
    }
  }

  /**
   * Shuts down the pool and closes all clients.
   */
  public void shutdown() {
    logger.info("Shutting down HTTP Client Pool");

    while (!pool.isEmpty()) {
      try {
        CloseableHttpClient client = pool.poll();
        if (client != null) {
          client.close();
        }
      } catch (IOException e) {
        logger.error("Error closing HTTP client during shutdown", e);
      }
    }

    logger.info("HTTP Client Pool shutdown complete");
  }

  /**
   * Gets the current size of the pool.
   *
   * @return Number of available clients in pool
   */
  public int getAvailableCount() {
    return pool.size();
  }

  /**
   * Gets the maximum pool size.
   *
   * @return Maximum pool size
   */
  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  /**
   * Gets the number of clients currently in use.
   *
   * @return Number of clients in use
   */
  public int getInUseCount() {
    return maxPoolSize - pool.size();
  }
}
