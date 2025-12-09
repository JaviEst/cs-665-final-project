package edu.bu.met.cs665.filter;

import edu.bu.met.cs665.model.GatewayRequest;
import edu.bu.met.cs665.model.GatewayResponse;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class FilterChain {
  private static final Logger logger = LogManager.getLogger(FilterChain.class);
  private final List<Filter> filters;

  /**
   * Constructs a FilterChain with the given filters.
   *
   * @param filters List of filters to execute
   */
  public FilterChain(List<Filter> filters) {
    this.filters = filters;
  }

  /**
   * Executes all filters in the pre-processing phase.
   * If any filter returns false, the chain stops.
   *
   * @param request The gateway request
   * @return true if all filters passed, false if any filter stopped the chain
   */
  public boolean executePreFilters(GatewayRequest request) {
    logger.info("Executing pre-filters for path: {}", request.getPath());

    for (Filter filter : filters) {
      logger.debug("Executing pre-filter: {}", filter.getName());
      boolean continueChain = filter.preProcess(request);

      if (!continueChain) {
        logger.warn("Filter {} stopped the chain", filter.getName());
        return false;
      }
    }

    logger.info("All pre-filters passed");
    return true;
  }

  /**
   * Executes all filters in the post-processing phase.
   * Post filters always execute all filters (no early termination).
   *
   * @param request The gateway request
   * @param response The gateway response
   */
  public void executePostFilters(GatewayRequest request, GatewayResponse response) {
    logger.info("Executing post-filters for path: {}", request.getPath());

    // Execute in reverse order for post-processing
    for (int i = filters.size() - 1; i >= 0; i--) {
      Filter filter = filters.get(i);
      logger.debug("Executing post-filter: {}", filter.getName());
      filter.postProcess(request, response);
    }

    logger.info("All post-filters executed");
  }

  /**
   * Gets the number of filters in this chain.
   *
   * @return filter count
   */
  public int size() {
    return filters.size();
  }
}
