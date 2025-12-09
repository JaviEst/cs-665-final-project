package edu.bu.met.cs665.filter.impl;

import edu.bu.met.cs665.filter.Filter;
import edu.bu.met.cs665.model.GatewayRequest;
import edu.bu.met.cs665.model.GatewayResponse;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class AuthenticationFilter implements Filter {
  private static final Logger logger = LogManager.getLogger(AuthenticationFilter.class);
  private final Set<String> validTokens;

  /**
   * Creates a new AuthenticationFilter with a set of valid tokens.
   */
  public AuthenticationFilter() {
    this.validTokens = new HashSet<>();
    // Pre-populate with some valid tokens for demonstration
    validTokens.add("token-123");
    validTokens.add("token-456");
    validTokens.add("token-789");
  }

  /**
   * Adds a valid token to the authentication system.
   *
   * @param token The token to add
   */
  public void addValidToken(String token) {
    validTokens.add(token);
  }

  /**
   * Processes the request before it's forwarded to the backend.
   *
   * @param request The gateway request
   * @return true if processing should continue, false to stop the chain
   */
  @Override
  public boolean preProcess(GatewayRequest request) {
    // Spring Boot converts headers to lowercase, so check both cases
    String authHeader = request.getHeaders().get("Authorization");
    if (authHeader == null) {
      authHeader = request.getHeaders().get("authorization");
    }

    if (authHeader == null || authHeader.isEmpty()) {
      logger.warn("Missing Authorization header for request: {}", request.getPath());
      return false;
    }

    // Extract token from "Bearer token-123" format
    String token = authHeader.replace("Bearer ", "").replace("bearer ", "");

    if (!validTokens.contains(token)) {
      logger.warn("Invalid token: {} for request: {}", token, request.getPath());
      return false;
    }

    logger.info("Authentication successful for token: {}", token);
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
    response.addHeader("X-Content-Type-Options", "nosniff");
    response.addHeader("X-Frame-Options", "DENY");
  }

  /**
   * Returns the name of this filter for logging and debugging.
   *
   * @return filter name
   */
  @Override
  public String getName() {
    return "AuthenticationFilter";
  }
}
