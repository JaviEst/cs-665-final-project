package edu.bu.met.cs665.filter;

import edu.bu.met.cs665.model.GatewayRequest;
import edu.bu.met.cs665.model.GatewayResponse;


public interface Filter {
  
  /**
   * Processes the request before it's forwarded to the backend.
   *
   * @param request The gateway request
   * @return true if processing should continue, false to stop the chain
   */
  boolean preProcess(GatewayRequest request);

  /**
   * Processes the response after receiving it from the backend.
   *
   * @param request The gateway request
   * @param response The gateway response
   */
  void postProcess(GatewayRequest request, GatewayResponse response);

  /**
   * Returns the name of this filter for logging and debugging.
   *
   * @return filter name
   */
  String getName();
}
