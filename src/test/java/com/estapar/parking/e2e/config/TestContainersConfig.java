package com.estapar.parking.e2e.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

public abstract class TestContainersConfig {

  private static final String TEST_DATASOURCE_URL = System.getenv("TEST_DATASOURCE_URL");
  private static final boolean USE_EXTERNAL_DATABASE = 
      TEST_DATASOURCE_URL != null && !TEST_DATASOURCE_URL.isEmpty();

  static MySQLContainer<?> mysql;

  static {
    if (!USE_EXTERNAL_DATABASE) {
      mysql = new MySQLContainer<>("mysql:8.0")
          .withDatabaseName("parking_test")
          .withUsername("test")
          .withPassword("test")
          .withReuse(true);
      try {
        mysql.start();
      } catch (Exception e) {
      }
    }
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    if (USE_EXTERNAL_DATABASE) {
      registry.add("spring.datasource.url", () -> TEST_DATASOURCE_URL);
      registry.add("spring.datasource.username", () -> 
          System.getenv().getOrDefault("TEST_DATASOURCE_USERNAME", "root"));
      registry.add("spring.datasource.password", () -> 
          System.getenv().getOrDefault("TEST_DATASOURCE_PASSWORD", "root"));
    } else if (mysql != null) {
      try {
        if (!mysql.isRunning()) {
          mysql.start();
        }
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
      } catch (Exception e) {
        registry.add("spring.datasource.url", () -> 
            "jdbc:mysql://localhost:3306/parking_test?createDatabaseIfNotExist=true&serverTimezone=UTC");
        registry.add("spring.datasource.username", () -> "root");
        registry.add("spring.datasource.password", () -> "root");
      }
    } else {
      registry.add("spring.datasource.url", () -> 
          "jdbc:mysql://localhost:3306/parking_test?createDatabaseIfNotExist=true&serverTimezone=UTC");
      registry.add("spring.datasource.username", () -> "root");
      registry.add("spring.datasource.password", () -> "root");
    }
  }
}
