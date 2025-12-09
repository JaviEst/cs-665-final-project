package edu.bu.met.cs665.model;

import java.util.HashMap;
import java.util.Map;


public class GatewayRequest {
  private String method;
  private String path;
  private Map<String, String> headers;
  private Map<String, String> queryParams;
  private String body;
  private String clientId;
  private long timestamp;

  /**
   * Constructs a new GatewayRequest.
   */
  public GatewayRequest() {
    this.headers = new HashMap<>();
    this.queryParams = new HashMap<>();
    this.timestamp = System.currentTimeMillis();
  }

  /**
   * Returns the HTTP method of the request.
   * 
   * @return HTTP method as a String
   */
  public String getMethod() {
    return method;
  }

  /**
   * Sets the HTTP method of the request.
   * 
   * @param method HTTP method as a String
   */
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * Returns the request path.
   * 
   * @return request path as a String
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets the request path.
   * 
   * @param path request path as a String
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Returns the request headers.
   * 
   * @return Map of request headers
   */
  public Map<String, String> getHeaders() {
    return headers;
  }

  /**
   * Sets the request headers.
   * 
   * @param headers Map of request headers
   */
  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  /**
   * Adds a single header to the request.
   * 
   * @param key Header name
   * @param value Header value
   */
  public void addHeader(String key, String value) {
    this.headers.put(key, value);
  }

  /**
   * Returns the query parameters.
   * 
   * @return Map of query parameters
   */
  public Map<String, String> getQueryParams() {
    return queryParams;
  }

  /**
   * Sets the query parameters.
   * 
   * @param queryParams Map of query parameters
   */
  public void setQueryParams(Map<String, String> queryParams) {
    this.queryParams = queryParams;
  }

  /**
   * Returns the request body.
   * 
   * @return request body as a String
   */
  public String getBody() {
    return body;
  }

  /**
   * Sets the request body.
   * 
   * @param body request body as a String
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * Returns the client ID associated with the request.
   * 
   * @return client ID as a String
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * Sets the client ID associated with the request.
   * 
   * @param clientId client ID as a String
   */
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  /**
   * Returns the timestamp when the request was created.
   * 
   * @return timestamp as a long
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Sets the timestamp when the request was created.
   * 
   * @param timestamp timestamp as a long
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
