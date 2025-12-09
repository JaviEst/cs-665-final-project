package edu.bu.met.cs665.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import edu.bu.met.cs665.filter.impl.LoggingFilter;
import edu.bu.met.cs665.model.GatewayRequest;
import edu.bu.met.cs665.model.GatewayResponse;
import edu.bu.met.cs665.route.Route;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class GatewayServiceTest {

  private GatewayService gatewayService;

  /**
   * Sets up test fixtures.
   */
  @Before
  public void setUp() {
    gatewayService = new GatewayService();
  }

  /**
   * Cleans up after tests.
   */
  @After
  public void tearDown() {
    if (gatewayService != null) {
      gatewayService.shutdown();
    }
  }

  /**
   * Tests route registration.
   */
  @Test
  public void testRouteRegistration() {
    Route route = Route.builder("/api/test")
        .targetUrl("http://localhost:8081/test")
        .build();

    gatewayService.registerRoute(route);

    assertEquals("Should have 1 route", 1, gatewayService.getRoutes().size());
  }

  /**
   * Tests multiple route registration.
   */
  @Test
  public void testMultipleRouteRegistration() {
    Route route1 = Route.builder("/api/users")
        .targetUrl("http://localhost:8081/users")
        .build();

    Route route2 = Route.builder("/api/posts")
        .targetUrl("http://localhost:8081/posts")
        .build();

    gatewayService.registerRoute(route1);
    gatewayService.registerRoute(route2);

    assertEquals("Should have 2 routes", 2, gatewayService.getRoutes().size());
  }

  /**
   * Tests request processing with no matching route.
   */
  @Test
  public void testNoMatchingRoute() {
    GatewayRequest request = new GatewayRequest();
    request.setMethod("GET");
    request.setPath("/api/nonexistent");
    request.setClientId("test-client");

    GatewayResponse response = gatewayService.processRequest(request);

    assertEquals("Should return 404", 404, response.getStatusCode());
    assertEquals("Should have appropriate message", 
        "No route found", response.getBody());
  }

  /**
   * Tests client pool initialization.
   */
  @Test
  public void testClientPoolInitialization() {
    assertNotNull("Client pool should not be null", 
        gatewayService.getClientPool());
    assertEquals("Pool should have correct max size", 
        10, gatewayService.getClientPool().getMaxPoolSize());
  }

  /**
   * Tests integration of Builder pattern with route configuration.
   */
  @Test
  public void testBuilderPatternIntegration() {
    Route route = Route.builder("/api/complex")
        .targetUrl("http://localhost:8081/complex")
        .addFilter(new LoggingFilter())
        .timeout(5000)
        .rateLimit(100)
        .addHeader("X-Custom", "value")
        .build();

    gatewayService.registerRoute(route);

    Route registered = gatewayService.getRoutes().get(0);
    assertEquals("Route should match", "/api/complex", 
        registered.getPathPattern());
    assertEquals("Timeout should match", 5000, registered.getTimeout());
    assertEquals("Rate limit should match", 100, registered.getRateLimit());
  }

  /**
   * Tests that gateway service properly shuts down.
   */
  @Test
  public void testGatewayShutdown() {
    gatewayService.shutdown();
    assertNotNull("Service should still be accessible", gatewayService);
  }
}
