import http from 'k6/http';
import { check, sleep } from 'k6';

// This is the configuration for our load test.
export const options = {
    // We define "stages" to simulate a ramp-up, peak load, and ramp-down.
    // This is much more realistic than hitting the server with max users instantly.
    stages: [
        { duration: '30s', target: 50 },  // Ramp-up to 50 virtual users over 30 seconds
        { duration: '1m', target: 100 }, // Stay at 100 virtual users for 1 minute
        { duration: '30s', target: 0 },   // Ramp-down to 0 users
    ],
    // We can define thresholds for our test to pass or fail.
    // E.g., fail the test if the error rate is > 1% or 95% of requests take longer than 800ms.
    thresholds: {
        'http_req_failed': ['rate<0.01'], // < 1% error rate
        'http_req_duration': ['p(95)<800'], // 95th percentile response time must be < 800ms
    },
};

const API_BASE_URL = 'http://localhost:8080/api/v1';

// This is the main function that k6 will execute over and over for each virtual user.
export default function () {
    const symbol = 'BTC/USD';
    
    // 1. Simulate placing a BUY order
    const buyPayload = JSON.stringify({
        symbol: symbol,
        side: 'BUY',
        orderType: 'LIMIT',
        quantity: 0.1,
        price: 50000.0 + Math.random() * 100 // Add some price variation
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const buyRes = http.post(`${API_BASE_URL}/orders`, buyPayload, params);

    // Check if the request was successful
    check(buyRes, {
        'BUY order created': (r) => r.status === 201,
    });

    sleep(0.5); // Wait for half a second

    // 2. Simulate placing a SELL order that might match
    const sellPayload = JSON.stringify({
        symbol: symbol,
        side: 'SELL',
        orderType: 'LIMIT',
        quantity: 0.05,
        price: 50000.0 - Math.random() * 100 // Sell at a slightly lower price
    });
    
    const sellRes = http.post(`${API_BASE_URL}/orders`, sellPayload, params);
    
    check(sellRes, {
        'SELL order created': (r) => r.status === 201,
    });

    sleep(1); // Wait 1 second

    // 3. Simulate querying the order book
    const orderBookRes = http.get(`${API_BASE_URL}/orderbook/BTC_USD`);
    check(orderBookRes, {
        'Order book retrieved': (r) => r.status === 200,
    });
}