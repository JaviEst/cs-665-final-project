package edu.bu.met.cs665.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import edu.bu.met.cs665.filter.impl.AuthenticationFilter;
import edu.bu.met.cs665.filter.impl.LoggingFilter;
import org.junit.Test;


public class RouteBuilderTest {

  /**
   * Tests basic route building with required parameters.
   */
  @Test
  public void testBasicRouteCreation() {
    Route route = Route.builder("/api/test")
        .targetUrl("http://localhost:8081/service")
        .build();

    assertNotNull("Route should not be null", route);
    assertEquals("Path pattern should match", "/api/test", route.getPathPattern());
    assertEquals("Target URL should match", 
        "http://localhost:8081/service", route.getTargetUrl());
  }

  /**
   * Tests fluent interface with multiple optional parameters.
   * This demonstrates the power of the Builder pattern.
   */
  @Test
  public void testFluentInterfaceBuilder() {
    Route route = Route.builder("/api/users")
        .targetUrl("http://localhost:8081/users")
        .timeout(3000)
        .rateLimit(50)
        .transformRequest(true)
        .transformResponse(true)
        .addHeader("X-Custom-Header", "value")
        .build();

    assertNotNull("Route should not be null", route);
    assertEquals("Timeout should be set", 3000, route.getTimeout());
    assertEquals("Rate limit should be set", 50, route.getRateLimit());
    assertTrue("Request transformation should be enabled", 
        route.isTransformRequest());
    assertTrue("Response transformation should be enabled", 
        route.isTransformResponse());
    assertEquals("Custom header should be set", 
        "value", route.getHeaders().get("X-Custom-Header"));
  }

  /**
   * Tests adding filters to route configuration.
   */
  @Test
  public void testAddingFilters() {
    Route route = Route.builder("/api/secure")
        .targetUrl("http://localhost:8081/secure")
        .addFilter(new LoggingFilter())
        .addFilter(new AuthenticationFilter())
        .build();

    assertNotNull("Route should not be null", route);
    assertEquals("Should have 2 filters", 2, route.getFilters().size());
  }

  /**
   * Tests that builder throws exception when required fields are missing.
   */
  @Test(expected = IllegalStateException.class)
  public void testBuilderValidation() {
    // Should throw exception because targetUrl is not set
    Route.builder("/api/test").build();
  }

  /**
   * Tests route path matching.
   */
  @Test
  public void testRouteMatching() {
    Route route = Route.builder("/api/users")
        .targetUrl("http://localhost:8081/users")
        .build();

    assertTrue("Should match exact path", route.matches("/api/users"));
    assertTrue("Should match subpaths", route.matches("/api/users/123"));
  }

  /**
   * Tests multiple header additions.
   */
  @Test
  public void testMultipleHeaders() {
    Route route = Route.builder("/api/test")
        .targetUrl("http://localhost:8081/test")
        .addHeader("X-Header-1", "value1")
        .addHeader("X-Header-2", "value2")
        .addHeader("X-Header-3", "value3")
        .build();

    assertEquals("Should have 3 headers", 3, route.getHeaders().size());
    assertEquals("Header 1 should match", "value1", 
        route.getHeaders().get("X-Header-1"));
    assertEquals("Header 2 should match", "value2", 
        route.getHeaders().get("X-Header-2"));
    assertEquals("Header 3 should match", "value3", 
        route.getHeaders().get("X-Header-3"));
  }

  /**
   * Tests default values in builder.
   */
  @Test
  public void testDefaultValues() {
    Route route = Route.builder("/api/default")
        .targetUrl("http://localhost:8081/default")
        .build();

    assertEquals("Default timeout should be 5000ms", 5000, route.getTimeout());
    assertEquals("Default rate limit should be 100", 100, route.getRateLimit());
  }
}
