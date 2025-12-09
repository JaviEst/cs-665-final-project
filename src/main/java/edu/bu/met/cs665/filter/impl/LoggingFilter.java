package edu.bu.met.cs665.filter.impl;

import edu.bu.met.cs665.filter.Filter;
import edu.bu.met.cs665.model.GatewayRequest;
import edu.bu.met.cs665.model.GatewayResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class LoggingFilter implements Filter {
  private static final Logger logger = LogManager.getLogger(LoggingFilter.class);

  @Override
  public boolean preProcess(GatewayRequest request) {
    logger.info("=== Request Logging ===");
    logger.info("Method: {}", request.getMethod());
    logger.info("Path: {}", request.getPath());
    logger.info("Client ID: {}", request.getClientId());
    logger.info("Headers: {}", request.getHeaders());
    logger.info("Timestamp: {}", request.getTimestamp());
    return true;
  }

  @Override
  public void postProcess(GatewayRequest request, GatewayResponse response) {
    logger.info("=== Response Logging ===");
    logger.info("Status Code: {}", response.getStatusCode());
    logger.info("Processing Time: {} ms", response.getProcessingTime());
    logger.info("Response Headers: {}", response.getHeaders());
  }

  /**
   * Returns the name of this filter for logging and debugging.
   *
   * @return filter name
   */
  @Override
  public String getName() {
    return "LoggingFilter";
  }
}
