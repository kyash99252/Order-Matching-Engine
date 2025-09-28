package com.example.exchange;

import com.example.exchange.api.dto.OrderRequest;
import com.example.exchange.domain.Order;
import com.example.exchange.domain.OrderType;
import com.example.exchange.domain.Side;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer; // ✅ use this
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationIT {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    // ✅ Replace RedisContainer with GenericContainer
    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @Test
    void whenPlaceLimitBuyOrder_thenOrderIsCreated() {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setSymbol("BTC/USD");
        orderRequest.setSide(Side.BUY);
        orderRequest.setOrderType(OrderType.LIMIT);
        orderRequest.setQuantity(new BigDecimal("0.5"));
        orderRequest.setPrice(new BigDecimal("50000.00"));

        ResponseEntity<Order> response = restTemplate.postForEntity("/api/v1/orders", orderRequest, Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Order createdOrder = response.getBody();
        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getId()).isNotNull();
        assertThat(createdOrder.getSymbol()).isEqualTo("BTC/USD");
        assertThat(createdOrder.getSide()).isEqualTo(Side.BUY);
        assertThat(createdOrder.getQuantity()).isEqualByComparingTo("0.5");
    }
}