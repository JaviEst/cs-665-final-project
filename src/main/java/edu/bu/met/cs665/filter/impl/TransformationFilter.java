package edu.bu.met.cs665.filter.impl;

import edu.bu.met.cs665.filter.Filter;
import edu.bu.met.cs665.model.GatewayRequest;
import edu.bu.met.cs665.model.GatewayResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class TransformationFilter implements Filter {
  private static final Logger logger = LogManager.getLogger(TransformationFilter.class);

  /**
   * Processes the request before it's forwarded to the backend.
   *
   * @param request The gateway request
   * @return true if processing should continue, false to stop the chain
   */
  @Override
  public boolean preProcess(GatewayRequest request) {
    request.addHeader("X-Gateway-Version", "1.0");
    request.addHeader("X-Gateway-Timestamp", String.valueOf(System.currentTimeMillis()));

    if (request.getBody() != null && !request.getBody().isEmpty()) {
      logger.info("Request body size before transformation: {} bytes",
          request.getBody().length());
    }

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
    response.addHeader("X-Gateway-Processed", "true");
    response.addHeader("X-Gateway-Route", request.getPath());

    long processingTime = System.currentTimeMillis() - request.getTimestamp();
    response.setProcessingTime(processingTime);

    logger.info("Response transformation complete. Processing time: {} ms",
        processingTime);
  }

  /**
   * Returns the name of this filter for logging and debugging.
   *
   * @return filter name
   */
  @Override
  public String getName() {
    return "TransformationFilter";
  }
}
