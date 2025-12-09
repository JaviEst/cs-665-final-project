package edu.bu.met.cs665.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class HttpClientPoolTest {

  private HttpClientPool pool;

  /**
   * Sets up test fixtures.
   */
  @Before
  public void setUp() {
    pool = new HttpClientPool(5, 2); // 5 clients, 2 second timeout
  }

  /**
   * Cleans up after tests.
   */
  @After
  public void tearDown() {
    if (pool != null) {
      pool.shutdown();
    }
  }

  /**
   * Tests pool initialization.
   */
  @Test
  public void testPoolInitialization() {
    assertEquals("Pool should have 5 available clients", 
        5, pool.getAvailableCount());
    assertEquals("Pool max size should be 5", 
        5, pool.getMaxPoolSize());
    assertEquals("No clients should be in use", 
        0, pool.getInUseCount());
  }

  /**
   * Tests acquiring a client from the pool.
   */
  @Test
  public void testAcquireClient() throws InterruptedException {
    CloseableHttpClient client = pool.acquire();

    assertNotNull("Client should not be null", client);
    assertEquals("Available count should decrease", 
        4, pool.getAvailableCount());
    assertEquals("In-use count should increase", 
        1, pool.getInUseCount());
  }

  /**
   * Tests releasing a client back to the pool.
   */
  @Test
  public void testReleaseClient() throws InterruptedException {
    CloseableHttpClient client = pool.acquire();
    assertEquals("Should have 4 available", 4, pool.getAvailableCount());

    pool.release(client);
    assertEquals("Should have 5 available after release", 
        5, pool.getAvailableCount());
    assertEquals("Should have 0 in use after release", 
        0, pool.getInUseCount());
  }

  /**
   * Tests acquiring multiple clients.
   */
  @Test
  public void testAcquireMultipleClients() throws InterruptedException {
    CloseableHttpClient client1 = pool.acquire();
    CloseableHttpClient client2 = pool.acquire();
    CloseableHttpClient client3 = pool.acquire();

    assertNotNull("Client 1 should not be null", client1);
    assertNotNull("Client 2 should not be null", client2);
    assertNotNull("Client 3 should not be null", client3);

    assertEquals("Available count should be 2", 
        2, pool.getAvailableCount());
    assertEquals("In-use count should be 3", 
        3, pool.getInUseCount());
  }

  /**
   * Tests releasing multiple clients.
   */
  @Test
  public void testReleaseMultipleClients() throws InterruptedException {
    CloseableHttpClient client1 = pool.acquire();
    CloseableHttpClient client2 = pool.acquire();
    CloseableHttpClient client3 = pool.acquire();

    pool.release(client1);
    pool.release(client2);
    pool.release(client3);

    assertEquals("All clients should be available", 
        5, pool.getAvailableCount());
    assertEquals("No clients should be in use", 
        0, pool.getInUseCount());
  }

  /**
   * Tests pool exhaustion behavior.
   */
  @Test(expected = IllegalStateException.class)
  public void testPoolExhaustion() throws InterruptedException {
    // Acquire all clients
    for (int i = 0; i < 5; i++) {
      pool.acquire();
    }

    assertEquals("Pool should be exhausted", 0, pool.getAvailableCount());
    assertEquals("All clients should be in use", 5, pool.getInUseCount());

    // This should throw IllegalStateException (timeout)
    pool.acquire();
  }

  /**
   * Tests pool reuse pattern - acquire, release, re-acquire.
   */
  @Test
  public void testPoolReuse() throws InterruptedException {
    CloseableHttpClient client1 = pool.acquire();
    pool.release(client1);

    CloseableHttpClient client2 = pool.acquire();
    assertNotNull("Should be able to re-acquire client", client2);
  }

  /**
   * Tests that pool maintains correct counts during complex operations.
   */
  @Test
  public void testComplexPoolOperations() throws InterruptedException {
    CloseableHttpClient c1 = pool.acquire();
    CloseableHttpClient c2 = pool.acquire();
    
    assertEquals("Should have 3 available", 3, pool.getAvailableCount());
    assertEquals("Should have 2 in use", 2, pool.getInUseCount());

    pool.release(c1);
    assertEquals("Should have 4 available", 4, pool.getAvailableCount());
    assertEquals("Should have 1 in use", 1, pool.getInUseCount());

    CloseableHttpClient c3 = pool.acquire();
    assertEquals("Should have 3 available", 3, pool.getAvailableCount());
    assertEquals("Should have 2 in use", 2, pool.getInUseCount());

    pool.release(c2);
    pool.release(c3);
    assertEquals("Should have 5 available", 5, pool.getAvailableCount());
    assertEquals("Should have 0 in use", 0, pool.getInUseCount());
  }

  /**
   * Tests pool shutdown.
   */
  @Test
  public void testPoolShutdown() throws InterruptedException {
    CloseableHttpClient client = pool.acquire();
    pool.release(client);

    pool.shutdown();

    // After shutdown, pool should be empty
    assertTrue("Pool should be empty after shutdown", 
        pool.getAvailableCount() == 0);
  }

  /**
   * Tests releasing null client (should not crash).
   */
  @Test
  public void testReleaseNullClient() {
    pool.release(null);
    assertEquals("Pool size should remain unchanged", 
        5, pool.getAvailableCount());
  }
}
