package edu.bu.met.cs665.filter.impl;

import edu.bu.met.cs665.filter.Filter;
import edu.bu.met.cs665.model.GatewayRequest;
import edu.bu.met.cs665.model.GatewayResponse;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class RateLimitFilter implements Filter {
  private static final Logger logger = LogManager.getLogger(RateLimitFilter.class);
  private final int maxRequestsPerMinute;
  private final Map<String, Queue<Long>> clientRequestTimes;

  /**
   * Creates a new RateLimitFilter.
   *
   * @param maxRequestsPerMinute Maximum requests allowed per minute
   */
  public RateLimitFilter(int maxRequestsPerMinute) {
    this.maxRequestsPerMinute = maxRequestsPerMinute;
    this.clientRequestTimes = new HashMap<>();
  }

  /**
   * Processes the request before it's forwarded to the backend.
   *
   * @param request The gateway request
   * @return true if processing should continue, false to stop the chain
   */
  @Override
  public boolean preProcess(GatewayRequest request) {
    String clientId = request.getClientId();
    if (clientId == null || clientId.isEmpty()) {
      clientId = "default";
    }

    long currentTime = System.currentTimeMillis();
    Queue<Long> requestTimes = clientRequestTimes.computeIfAbsent(
        clientId, k -> new LinkedList<>()
    );

    long oneMinuteAgo = currentTime - 60000;
    while (!requestTimes.isEmpty() && requestTimes.peek() < oneMinuteAgo) {
      requestTimes.poll();
    }

    if (requestTimes.size() >= maxRequestsPerMinute) {
      logger.warn("Rate limit exceeded for client: {}. Limit: {}/min",
          clientId, maxRequestsPerMinute);
      return false;
    }

    requestTimes.offer(currentTime);
    logger.info("Rate limit check passed for client: {}. Current: {}/{}",
        clientId, requestTimes.size(), maxRequestsPerMinute);

    return true;
  }

  /**
   * Processes the response after receiving it from the backend.
   *
   * @param request The gateway request
   * @param response The gateway response
   */
  @Override
  public void postProcess(GatewayRequest request, GatewayResponse response) {
    String clientId = request.getClientId() != null 
        ? request.getClientId() : "default";
    Queue<Long> requestTimes = clientRequestTimes.get(clientId);

    if (requestTimes != null) {
      int remaining = maxRequestsPerMinute - requestTimes.size();
      response.addHeader("X-RateLimit-Limit", String.valueOf(maxRequestsPerMinute));
      response.addHeader("X-RateLimit-Remaining", String.valueOf(remaining));
    }
  }

  /**
   * Returns the name of this filter for logging and debugging.
   *
   * @return filter name
   */
  @Override
  public String getName() {
    return "RateLimitFilter";
  }

  /**
   * Gets the current request count for a client.
   *
   * @param clientId The client identifier
   * @return Current request count
   */
  public int getCurrentCount(String clientId) {
    Queue<Long> requestTimes = clientRequestTimes.get(clientId);
    return requestTimes != null ? requestTimes.size() : 0;
  }

  /**
   * Resets the rate limit for a specific client.
   *
   * @param clientId The client identifier
   */
  public void reset(String clientId) {
    clientRequestTimes.remove(clientId);
  }
}
