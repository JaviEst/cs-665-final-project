package edu.bu.met.cs665.controller;

import edu.bu.met.cs665.model.GatewayRequest;
import edu.bu.met.cs665.model.GatewayResponse;
import edu.bu.met.cs665.service.GatewayService;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class GatewayController {
  private static final Logger logger = LogManager.getLogger(GatewayController.class);

  @Autowired
  private GatewayService gatewayService;

  /**
   * Handles GET requests through the gateway.
   *
   * @param servletRequest The HTTP servlet request
   * @param headers Request headers
   * @param params Query parameters
   * @return Response from backend service
   */
  @GetMapping("/api/**")
  public ResponseEntity<String> handleGet(
      HttpServletRequest servletRequest,
      @RequestHeader Map<String, String> headers,
      @RequestParam Map<String, String> params) {

    String path = servletRequest.getRequestURI();
    logger.info("Received GET request for path: {}", path);

    GatewayRequest request = new GatewayRequest();
    request.setMethod("GET");
    request.setPath(path);
    request.setHeaders(headers);
    request.setQueryParams(params);
    request.setClientId(headers.getOrDefault("X-Client-Id", "unknown"));

    GatewayResponse response = gatewayService.processRequest(request);

    return ResponseEntity
        .status(HttpStatus.valueOf(response.getStatusCode()))
        .body(response.getBody());
  }

  /**
   * Handles POST requests through the gateway.
   *
   * @param servletRequest The HTTP servlet request
   * @param headers Request headers
   * @param body Request body
   * @return Response from backend service
   */
  @PostMapping("/api/**")
  public ResponseEntity<String> handlePost(
      HttpServletRequest servletRequest,
      @RequestHeader Map<String, String> headers,
      @RequestBody(required = false) String body) {

    String path = servletRequest.getRequestURI();
    logger.info("Received POST request for path: {}", path);

    GatewayRequest request = new GatewayRequest();
    request.setMethod("POST");
    request.setPath(path);
    request.setHeaders(headers);
    request.setBody(body);
    request.setClientId(headers.getOrDefault("X-Client-Id", "unknown"));

    GatewayResponse response = gatewayService.processRequest(request);

    return ResponseEntity
        .status(HttpStatus.valueOf(response.getStatusCode()))
        .body(response.getBody());
  }

  /**
   * Health check endpoint.
   *
   * @return Health status
   */
  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("API Gateway is running");
  }

  /**
   * Gets gateway statistics.
   *
   * @return Statistics
   */
  @GetMapping("/stats")
  public ResponseEntity<String> stats() {
    int routes = gatewayService.getRoutes().size();
    int availableClients = gatewayService.getClientPool().getAvailableCount();
    int maxClients = gatewayService.getClientPool().getMaxPoolSize();
    int inUse = gatewayService.getClientPool().getInUseCount();

    String stats = String.format(
        "Gateway Statistics:\n"
            + "Routes configured: %d\n"
            + "HTTP clients available: %d/%d\n"
            + "HTTP clients in use: %d",
        routes, availableClients, maxClients, inUse
    );

    return ResponseEntity.ok(stats);
  }

}
