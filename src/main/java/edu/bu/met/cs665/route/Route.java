package edu.bu.met.cs665.route;

import edu.bu.met.cs665.filter.Filter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Route {
  private final String pathPattern;
  private final String targetUrl;
  private final List<Filter> filters;
  private final Map<String, String> headers;
  private final int timeout;
  private final int rateLimit;
  private final boolean transformRequest;
  private final boolean transformResponse;

  /**
   * Private constructor - only accessible through Builder.
   *
   * @param builder The builder instance
   */
  private Route(Builder builder) {
    this.pathPattern = builder.pathPattern;
    this.targetUrl = builder.targetUrl;
    this.filters = builder.filters;
    this.headers = builder.headers;
    this.timeout = builder.timeout;
    this.rateLimit = builder.rateLimit;
    this.transformRequest = builder.transformRequest;
    this.transformResponse = builder.transformResponse;
  }

  /**
   * Creates a new Builder instance.
   *
   * @param pathPattern The URL path pattern to match
   * @return A new Builder instance
   */
  public static Builder builder(String pathPattern) {
    return new Builder(pathPattern);
  }

  /**
   * Returns the path pattern for this route.
   * 
   * @return The URL path pattern
   */
  public String getPathPattern() {
    return pathPattern;
  }

  /**
   * Returns the target URL for this route.
   * 
   * @return The backend service URL
   */
  public String getTargetUrl() {
    return targetUrl;
  }

  /**
   * Returns the list of filters for this route.
   * 
   * @return List of filters
   */
  public List<Filter> getFilters() {
    return new ArrayList<>(filters);
  }

  /**
   * Returns the headers for this route.
   * 
   * @return Map of headers
   */
  public Map<String, String> getHeaders() {
    return new HashMap<>(headers);
  }

  /**
   * Returns the timeout for this route.
   * 
   * @return Timeout in milliseconds
   */
  public int getTimeout() {
    return timeout;
  }

  /**
   * Returns the rate limit for this route.
   * 
   * @return Rate limit (requests per minute)
   */
  public int getRateLimit() {
    return rateLimit;
  }

  /**
   * Indicates if request transformation is enabled.
   * 
   * @return true if request transformation is enabled, false otherwise
   */
  public boolean isTransformRequest() {
    return transformRequest;
  }

  /**
   * Indicates if response transformation is enabled.
   * 
   * @return true if response transformation is enabled, false otherwise
   */
  public boolean isTransformResponse() {
    return transformResponse;
  }


  public static class Builder {
    private final String pathPattern;

    private String targetUrl = "";
    private List<Filter> filters = new ArrayList<>();
    private Map<String, String> headers = new HashMap<>();
    private int timeout = 5000; // 5 seconds default
    private int rateLimit = 100; // requests per minute
    private boolean transformRequest = false;
    private boolean transformResponse = false;

    /**
     * Constructor with required parameter.
     *
     * @param pathPattern The URL path pattern
     */
    private Builder(String pathPattern) {
      this.pathPattern = pathPattern;
    }

    /**
     * Sets the target URL where requests should be forwarded.
     *
     * @param targetUrl The backend service URL
     * @return this Builder instance for method chaining
     */
    public Builder targetUrl(String targetUrl) {
      this.targetUrl = targetUrl;
      return this;
    }

    /**
     * Adds a filter to the route's filter chain.
     *
     * @param filter The filter to add
     * @return this Builder instance for method chaining
     */
    public Builder addFilter(Filter filter) {
      this.filters.add(filter);
      return this;
    }

    /**
     * Sets all filters for the route.
     *
     * @param filters List of filters
     * @return this Builder instance for method chaining
     */
    public Builder filters(List<Filter> filters) {
      this.filters = new ArrayList<>(filters);
      return this;
    }

    /**
     * Adds a header to be included in all requests.
     *
     * @param key Header name
     * @param value Header value
     * @return this Builder instance for method chaining
     */
    public Builder addHeader(String key, String value) {
      this.headers.put(key, value);
      return this;
    }

    /**
     * Sets all headers for the route.
     *
     * @param headers Map of headers
     * @return this Builder instance for method chaining
     */
    public Builder headers(Map<String, String> headers) {
      this.headers = new HashMap<>(headers);
      return this;
    }

    /**
     * Sets the request timeout in milliseconds.
     *
     * @param timeout Timeout in milliseconds
     * @return this Builder instance for method chaining
     */
    public Builder timeout(int timeout) {
      this.timeout = timeout;
      return this;
    }

    /**
     * Sets the rate limit (requests per minute).
     *
     * @param rateLimit Maximum requests per minute
     * @return this Builder instance for method chaining
     */
    public Builder rateLimit(int rateLimit) {
      this.rateLimit = rateLimit;
      return this;
    }

    /**
     * Enables request transformation.
     *
     * @param transformRequest Whether to transform requests
     * @return this Builder instance for method chaining
     */
    public Builder transformRequest(boolean transformRequest) {
      this.transformRequest = transformRequest;
      return this;
    }

    /**
     * Enables response transformation.
     *
     * @param transformResponse Whether to transform responses
     * @return this Builder instance for method chaining
     */
    public Builder transformResponse(boolean transformResponse) {
      this.transformResponse = transformResponse;
      return this;
    }

    /**
     * Builds and returns the Route instance.
     * This is the final step in the fluent interface.
     *
     * @return A new Route instance with all configured properties
     * @throws IllegalStateException if required fields are not set
     */
    public Route build() {
      if (targetUrl == null || targetUrl.isEmpty()) {
        throw new IllegalStateException("targetUrl must be set");
      }
      return new Route(this);
    }
  }

  /**
   * Checks if the given path matches this route's pattern.
   *
   * @param path The request path
   * @return true if the path matches, false otherwise
   */
  public boolean matches(String path) {
    // Simple pattern matching - could be enhanced with wildcards
    return path.startsWith(pathPattern);
  }

  @Override
  public String toString() {
    return "Route{"
        + "pathPattern='" + pathPattern + '\''
        + ", targetUrl='" + targetUrl + '\''
        + ", filters=" + filters.size()
        + ", rateLimit=" + rateLimit
        + ", timeout=" + timeout
        + '}';
  }
}
