package edu.bu.met.cs665.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import edu.bu.met.cs665.filter.impl.AuthenticationFilter;
import edu.bu.met.cs665.filter.impl.LoggingFilter;
import edu.bu.met.cs665.filter.impl.RateLimitFilter;
import edu.bu.met.cs665.model.GatewayRequest;
import edu.bu.met.cs665.model.GatewayResponse;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;


public class FilterChainTest {

  private GatewayRequest request;
  private GatewayResponse response;

  /**
   * Sets up test fixtures.
   */
  @Before
  public void setUp() {
    request = new GatewayRequest();
    request.setMethod("GET");
    request.setPath("/api/test");
    request.setClientId("test-client");

    response = new GatewayResponse();
  }

  /**
   * Tests that all filters execute when all return true.
   */
  @Test
  public void testAllFiltersPassPreProcessing() {
    List<Filter> filters = Arrays.asList(
        new LoggingFilter(),
        new LoggingFilter()
    );

    FilterChain chain = new FilterChain(filters);
    boolean result = chain.executePreFilters(request);

    assertTrue("All filters should pass", result);
  }

  /**
   * Tests that chain stops when a filter returns false.
   * This is the core of the Chain of Responsibility pattern.
   */
  @Test
  public void testChainStopsWhenFilterFails() {
    // Authentication filter will fail without proper header
    List<Filter> filters = Arrays.asList(
        new LoggingFilter(),
        new AuthenticationFilter(), // This will fail
        new LoggingFilter() // This should not execute
    );

    FilterChain chain = new FilterChain(filters);
    boolean result = chain.executePreFilters(request);

    assertFalse("Chain should stop at authentication filter", result);
  }

  /**
   * Tests successful authentication in the chain.
   */
  @Test
  public void testChainPassesWithValidAuthentication() {
    request.addHeader("Authorization", "Bearer token-123");

    List<Filter> filters = Arrays.asList(
        new LoggingFilter(),
        new AuthenticationFilter(),
        new LoggingFilter()
    );

    FilterChain chain = new FilterChain(filters);
    boolean result = chain.executePreFilters(request);

    assertTrue("Chain should pass with valid token", result);
  }

  /**
   * Tests post-filter execution.
   */
  @Test
  public void testPostFilterExecution() {
    List<Filter> filters = Arrays.asList(
        new LoggingFilter()
    );

    FilterChain chain = new FilterChain(filters);
    chain.executePostFilters(request, response);

    assertTrue("Processing time should be set", 
        response.getProcessingTime() >= 0);
  }

  /**
   * Tests rate limiting filter behavior.
   */
  @Test
  public void testRateLimitFilter() {
    RateLimitFilter rateLimitFilter = new RateLimitFilter(2);
    
    // First request should pass
    assertTrue("First request should pass", 
        rateLimitFilter.preProcess(request));
    
    // Second request should pass
    assertTrue("Second request should pass", 
        rateLimitFilter.preProcess(request));
    
    // Third request should fail (limit is 2)
    assertFalse("Third request should fail", 
        rateLimitFilter.preProcess(request));
  }

  /**
   * Tests rate limit filter with different clients.
   */
  @Test
  public void testRateLimitPerClient() {
    RateLimitFilter rateLimitFilter = new RateLimitFilter(2);
    
    // Client 1
    request.setClientId("client-1");
    assertTrue("Client 1 first request should pass", 
        rateLimitFilter.preProcess(request));
    assertTrue("Client 1 second request should pass", 
        rateLimitFilter.preProcess(request));
    
    // Client 2 should have separate limit
    request.setClientId("client-2");
    assertTrue("Client 2 first request should pass", 
        rateLimitFilter.preProcess(request));
    assertTrue("Client 2 second request should pass", 
        rateLimitFilter.preProcess(request));
  }

  /**
   * Tests authentication filter with invalid token.
   */
  @Test
  public void testAuthenticationFilterInvalidToken() {
    AuthenticationFilter authFilter = new AuthenticationFilter();
    request.addHeader("Authorization", "Bearer invalid-token");

    assertFalse("Invalid token should fail", 
        authFilter.preProcess(request));
  }

  /**
   * Tests authentication filter with valid token.
   */
  @Test
  public void testAuthenticationFilterValidToken() {
    AuthenticationFilter authFilter = new AuthenticationFilter();
    request.addHeader("Authorization", "Bearer token-123");

    assertTrue("Valid token should pass", 
        authFilter.preProcess(request));
  }

  /**
   * Tests authentication filter adds security headers to response.
   */
  @Test
  public void testAuthenticationFilterAddsSecurityHeaders() {
    AuthenticationFilter authFilter = new AuthenticationFilter();
    authFilter.postProcess(request, response);

    assertEquals("X-Content-Type-Options should be set", 
        "nosniff", response.getHeaders().get("X-Content-Type-Options"));
    assertEquals("X-Frame-Options should be set", 
        "DENY", response.getHeaders().get("X-Frame-Options"));
  }

  /**
   * Tests filter chain size.
   */
  @Test
  public void testFilterChainSize() {
    List<Filter> filters = Arrays.asList(
        new LoggingFilter(),
        new AuthenticationFilter(),
        new RateLimitFilter(10)
    );

    FilterChain chain = new FilterChain(filters);
    assertEquals("Chain should have 3 filters", 3, chain.size());
  }
}
