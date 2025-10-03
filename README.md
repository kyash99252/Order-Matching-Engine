# Order Matching Engine Overview

This project is a high-performance, real-time order matching engine for financial exchanges, built with Java, Spring Boot, Redis, and PostgreSQL. It supports RESTful APIs and WebSocket communication for live trading, order book management, and trade notifications. The architecture emphasizes separation of concerns, thread safety, and scalability, making it suitable for production-grade trading platforms.

## Features

-   **Order Matching Engine**: Core matching logic is encapsulated in a thread-safe, stateless engine (`MatchingEngine`), ensuring sequential processing per symbol for data integrity.
    -   Supports market and limit orders, with FIFO time priority at each price level.
    -   Efficient in-memory order book management using sorted data structures for fast matching.
-   **Order Book Management**: Each trading symbol has its own `OrderBook`, maintaining sorted bids and asks.
    -   Orders are matched, partially filled, or added to the book as appropriate.
    -   Order book state is cached in Redis for fast retrieval and scalability.
-   **Persistence Layer**: Orders and trades are persisted in PostgreSQL using Spring Data JPA repositories.
    -   Asynchronous persistence via a dedicated service ensures minimal API latency and high throughput.
-   **API Layer**: RESTful endpoints for placing orders and retrieving order books.
    -   Input validation using DTOs and `@Valid` annotations for robust, fail-fast error handling.
    -   Thin controllers delegate all business logic to service interfaces for maintainability.
-   **WebSocket Integration**: Real-time trade and order book updates are pushed to clients via STOMP over WebSocket.
    -   Configurable message broker and endpoint for scalable client communication.
-   **Performance Tuning**: Connection pools, thread pools, and async task executors are tuned for high concurrency.
    -   Compression and batching enabled for efficient network and database usage.

## Architecture

-   **Spring Boot**: Main application framework, providing dependency injection, configuration, and REST API support.
-   **Redis**: Used for caching order book snapshots, reducing database load and improving response times.
-   **PostgreSQL**: Stores all orders and trades for auditability and durability.
-   **WebSocket (STOMP)**: Enables real-time notifications for trades and order book changes.
-   **Domain-Driven Design (DDD)**: Entities (`Order`, `Trade`) and repositories are modeled for clarity and extensibility.

## Folder Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/example/exchange/
│   │       ├── ExchangeApplication.java
│   │       ├── api/
│   │       │   ├── OrderController.java
│   │       │   ├── GlobalExceptionHandler.java
│   │       │   └── dto/
│   │       │       └── OrderRequest.java
│   │       ├── config/
│   │       │   ├── RedisConfig.java
│   │       │   └── WebSocketConfig.java
│   │       ├── core/
│   │       │   ├── matching/
│   │       │   │   └── MatchingEngine.java
│   │       │   └── orderbook/
│   │       │       └── OrderBook.java
│   │       ├── domain/
│   │       │   ├── Order.java
│   │       │   ├── Trade.java
│   │       │   ├── OrderStatus.java
│   │       │   ├── OrderType.java
│   │       │   └── Side.java
│   │       ├── repository/
│   │       │   ├── OrderRepository.java
│   │       │   ├── TradeRepository.java
│   │       │   └── OrderBookCache.java
│   │       └── service/
│   │           ├── OrderService.java
│   │           ├── OrderServiceImpl.java
│   │           └── PersistenceService.java
│   └── resources/
│       └── application.properties
├── test/
│   └── java/
│       └── com/example/exchange/
│           └── ApplicationIT.java
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

## Getting Started

### Prerequisites

-   Java 17+
-   Docker & Docker Compose
-   PostgreSQL and Redis (can be started via Docker Compose)

### Setup

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/kyash99252/Order-Matching-Engine.git
    cd Order-Matching-Engine
    ```
2.  **Start dependencies:**
    ```bash
    docker-compose up -d
    ```
    This will start Redis and PostgreSQL containers.
3.  **Configure application properties:**
    See `application.properties` for all tunable settings (DB, Redis, thread pools, etc).
4.  **Build and run the application:**
    ```bash
    mvn clean package
    mvn spring-boot:run
    ```

## API Endpoints

-   **`POST /api/v1/orders`**
    -   Place a new order.
    -   Request body:
        ```json
        {
            "symbol": "BTC/USD",
            "side": "BUY",
            "orderType": "LIMIT",
            "quantity": 1.0,
            "price": 50000.0
        }
        ```
-   **`GET /api/v1/orderbook/{symbolPair}`**
    -   Retrieve the current order book for a symbol (e.g., `BTC_USD`).

-   **WebSocket Endpoint**: `/ws/trades`
    -   **Subscribe**: `/topic/orderbook` or `/topic/trades`
    -   **Protocol**: STOMP over WebSocket

## Configuration Highlights

-   **`application.properties`**: Database, Redis, thread pool, async executor, and compression settings. Performance tuning for high concurrency and low latency.
-   **`docker-compose.yml`**:
    -   Redis (port 6379)
    -   PostgreSQL (port 5433, DB: `exchange`, user: `user`, password: `mysecretpassword`)

## Design Principles

-   **Separation of Concerns**: Controllers are thin, delegating logic to services. Services encapsulate business logic and async persistence. Repositories abstract data access.
-   **Thread Safety**: Matching engine uses per-symbol locks for safe concurrent order processing.
-   **Scalability**: Redis caching, async persistence, and tuned thread pools.
-   **Extensibility**: DDD entities and interfaces allow for future enhancements (e.g., new order types, additional endpoints).

## License

[MIT License](LICENSE)

## Authors

-   kyash99252
