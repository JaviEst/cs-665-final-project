package edu.bu.met.cs665;

import edu.bu.met.cs665.filter.impl.AuthenticationFilter;
import edu.bu.met.cs665.filter.impl.LoggingFilter;
import edu.bu.met.cs665.filter.impl.RateLimitFilter;
import edu.bu.met.cs665.filter.impl.TransformationFilter;
import edu.bu.met.cs665.route.Route;
import edu.bu.met.cs665.service.GatewayService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class ApiGatewayApplication {
  private static final Logger logger = LogManager.getLogger(ApiGatewayApplication.class);

  /**
   * Main entry point for the application.
   *
   * @param args Command line arguments
   */
  public static void main(String[] args) {
    logger.info("Starting API Gateway Application");
    SpringApplication.run(ApiGatewayApplication.class, args);
  }

  /**
   * Configures initial routes on application startup.
   *
   * @param gatewayService The gateway service
   * @return CommandLineRunner
   */
  @Bean
  public CommandLineRunner configureRoutes(GatewayService gatewayService) {
    return args -> {
      logger.info("Configuring routes...");

      Route publicRoute = Route.builder("/api/public")
          .targetUrl("https://jsonplaceholder.typicode.com/posts/1")
          .addFilter(new LoggingFilter())
          .addFilter(new TransformationFilter())
          .timeout(5000)
          .rateLimit(100)
          .build();
      gatewayService.registerRoute(publicRoute);

      Route secureRoute = Route.builder("/api/secure")
          .targetUrl("https://jsonplaceholder.typicode.com/users/1")
          .addFilter(new LoggingFilter())
          .addFilter(new AuthenticationFilter())
          .addFilter(new RateLimitFilter(10)) // 10 requests per minute
          .addFilter(new TransformationFilter())
          .timeout(3000)
          .build();
      gatewayService.registerRoute(secureRoute);

      Route highVolumeRoute = Route.builder("/api/data")
          .targetUrl("https://jsonplaceholder.typicode.com/posts")
          .addFilter(new LoggingFilter())
          .addFilter(new AuthenticationFilter())
          .addFilter(new RateLimitFilter(50)) // 50 requests per minute
          .addFilter(new TransformationFilter())
          .timeout(10000)
          .rateLimit(50)
          .build();
      gatewayService.registerRoute(highVolumeRoute);

      logger.info("Routes configured successfully. Total routes: {}",
          gatewayService.getRoutes().size());
    };
  }
}
