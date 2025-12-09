package edu.bu.met.cs665.service;

import edu.bu.met.cs665.filter.FilterChain;
import edu.bu.met.cs665.model.GatewayRequest;
import edu.bu.met.cs665.model.GatewayResponse;
import edu.bu.met.cs665.pool.HttpClientPool;
import edu.bu.met.cs665.route.Route;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;


@Service
public class GatewayService {
  private static final Logger logger = LogManager.getLogger(GatewayService.class);
  private final List<Route> routes;
  private final HttpClientPool clientPool;

  /**
   * Creates a new GatewayService.
   */
  public GatewayService() {
    this.routes = new ArrayList<>();
    this.clientPool = new HttpClientPool(10, 5); // 10 clients, 5 second timeout
    logger.info("GatewayService initialized");
  }

  /**
   * Registers a route with the gateway.
   *
   * @param route The route to register
   */
  public void registerRoute(Route route) {
    routes.add(route);
    logger.info("Route registered: {}", route);
  }

  /**
   * Processes a request through the gateway.
   *
   * @param request The gateway request
   * @return The gateway response
   */
  public GatewayResponse processRequest(GatewayRequest request) {
    logger.info("Processing request: {} {}", request.getMethod(), request.getPath());

    GatewayResponse response = new GatewayResponse();

    try {
      Route route = findRoute(request.getPath());
      if (route == null) {
        logger.warn("No route found for path: {}", request.getPath());
        response.setStatusCode(404);
        response.setBody("No route found");
        return response;
      }

      FilterChain filterChain = new FilterChain(route.getFilters());
      boolean continueProcessing = filterChain.executePreFilters(request);

      if (!continueProcessing) {
        logger.warn("Request blocked by filters");
        response.setStatusCode(403);
        response.setBody("Request blocked by filters");
        return response;
      }

      response = this.forwardRequest(request, route);

      filterChain.executePostFilters(request, response);

    } catch (Exception e) {
      logger.error("Error processing request", e);
      response.setStatusCode(500);
      response.setBody("Internal gateway error: " + e.getMessage());
    }

    return response;
  }

  /**
   * Finds a route that matches the given path.
   *
   * @param path The request path
   * @return Matching route or null
   */
  private Route findRoute(String path) {
    for (Route route : routes) {
      if (route.matches(path)) {
        return route;
      }
    }
    return null;
  }

  /**
   * Forwards the request to the backend service.
   * Uses the Object Pool pattern to get an HTTP client.
   *
   * @param request The gateway request
   * @param route The matched route
   * @return The gateway response
   */
  private GatewayResponse forwardRequest(GatewayRequest request, Route route) {
    GatewayResponse response = new GatewayResponse();
    CloseableHttpClient httpClient = null;

    try {
      httpClient = this.clientPool.acquire();
      logger.info("Using pooled HTTP client. In use: {}/{}",
          this.clientPool.getInUseCount(), this.clientPool.getMaxPoolSize());

      String targetUrl = route.getTargetUrl();
      logger.info("Forwarding to: {}", targetUrl);

      HttpUriRequest httpRequest = this.createHttpRequest(request, targetUrl);

      HttpResponse httpResponse = httpClient.execute(httpRequest);

      response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
      String responseBody = EntityUtils.toString(httpResponse.getEntity());
      response.setBody(responseBody);

      logger.info("Backend response received. Status: {}",
          response.getStatusCode());

    } catch (InterruptedException e) {
      logger.error("Interrupted while acquiring HTTP client", e);
      response.setStatusCode(503);
      response.setBody("Service temporarily unavailable");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      logger.error("Error forwarding request", e);
      response.setStatusCode(502);
      response.setBody("Bad gateway: " + e.getMessage());
    } finally {
      if (httpClient != null) {
        this.clientPool.release(httpClient);
        logger.debug("HTTP client released. Available: {}/{}",
            this.clientPool.getAvailableCount(), this.clientPool.getMaxPoolSize());
      }
    }

    return response;
  }

  /**
   * Creates an HTTP request based on the gateway request.
   *
   * @param request The gateway request
   * @param targetUrl The target URL
   * @return HttpUriRequest
   * @throws IOException if there's an error creating the request
   */
  private HttpUriRequest createHttpRequest(GatewayRequest request, String targetUrl)
      throws IOException {
    HttpUriRequest httpRequest;

    if ("POST".equalsIgnoreCase(request.getMethod())) {
      HttpPost post = new HttpPost(targetUrl);
      if (request.getBody() != null) {
        post.setEntity(new StringEntity(request.getBody()));
      }
      httpRequest = post;
    } else {
      httpRequest = new HttpGet(targetUrl);
    }

    // Add standard headers to avoid being blocked by CDNs/WAFs
    httpRequest.addHeader("User-Agent", 
        "Mozilla/5.0 (compatible; API-Gateway/1.0)");
    httpRequest.addHeader("Accept", 
        "application/json, text/plain, */*");
    httpRequest.addHeader("Accept-Language", "en-US,en;q=0.9");

    // Add request headers (but don't override standard headers)
    for (java.util.Map.Entry<String, String> header : request.getHeaders().entrySet()) {
      String headerName = header.getKey();
      // Skip headers that might cause issues or are gateway-specific
      if (!headerName.equalsIgnoreCase("Host") 
          && !headerName.equalsIgnoreCase("Content-Length")
          && !headerName.startsWith("X-Gateway-")) {
        httpRequest.addHeader(headerName, header.getValue());
      }
    }

    return httpRequest;
  }

  /**
   * Gets the current routes.
   *
   * @return List of routes
   */
  public List<Route> getRoutes() {
    return new ArrayList<>(routes);
  }

  /**
   * Gets the HTTP client pool.
   *
   * @return The client pool
   */
  public HttpClientPool getClientPool() {
    return clientPool;
  }

  /**
   * Shuts down the gateway service.
   */
  public void shutdown() {
    logger.info("Shutting down GatewayService");
    this.clientPool.shutdown();
  }
}
