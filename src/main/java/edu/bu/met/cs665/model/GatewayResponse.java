package edu.bu.met.cs665.model;

import java.util.HashMap;
import java.util.Map;


public class GatewayResponse {
  private int statusCode;
  private Map<String, String> headers;
  private String body;
  private long processingTime;

  /**
   * Constructs a new GatewayResponse.
   */
  public GatewayResponse() {
    this.headers = new HashMap<>();
    this.statusCode = 200;
  }

  /**
   * Returns the HTTP status code of the response.
   * 
   * @return HTTP status code as an int
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * Sets the HTTP status code of the response.
   * 
   * @param statusCode HTTP status code as an int
   */
  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  /**
   * Returns the response headers.
   * 
   * @return Map of response headers
   */
  public Map<String, String> getHeaders() {
    return headers;
  }

  /**
   * Sets the response headers.
   * 
   * @param headers Map of response headers
   */
  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  /**
   * Adds a single header to the response.
   * 
   * @param key Header name
   * @param value Header value
   */
  public void addHeader(String key, String value) {
    this.headers.put(key, value);
  }

  /**
   * Returns the response body.
   * 
   * @return response body as a String
   */
  public String getBody() {
    return body;
  }

  /**
   * Sets the response body.
   * 
   * @param body response body as a String
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * Returns the processing time of the response.
   * 
   * @return processing time in milliseconds
   */
  public long getProcessingTime() {
    return processingTime;
  }

  /**
   * Sets the processing time of the response.
   * 
   * @param processingTime processing time in milliseconds
   */
  public void setProcessingTime(long processingTime) {
    this.processingTime = processingTime;
  }
}
