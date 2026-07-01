package com.retail.order.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full integration test — hits the real H2 database seeded by data.sql.
 * This is the test that PROVES whether the bug is fixed end-to-end.
 *
 * Run: mvn test -Dtest=OrderControllerIntegrationTest
 *
 * BEFORE the fix: testOrd1042ReturnsCorrectStatus() FAILS — API returns PENDING.
 * AFTER the fix:  testOrd1042ReturnsCorrectStatus() PASSES — API returns SHIPPED.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @DisplayName("Control case: ORD-2001 has no shipment — correctly returns PENDING")
    void testOrd2001ReturnsPending() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/api/orders/ORD-2001/status"), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("\"status\":\"PENDING\"");
    }

    @Test
    @DisplayName("🎯 THE BUG TEST: ORD-1042 has a shipment but currently returns PENDING (should be SHIPPED)")
    void testOrd1042ReturnsCorrectStatus() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/api/orders/ORD-1042/status"), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);

        // 🐛 BEFORE FIX: this assertion FAILS — body actually contains "PENDING"
        // ✅ AFTER FIX:  this assertion PASSES — body contains "SHIPPED"
        assertThat(response.getBody()).contains("\"status\":\"SHIPPED\"");
    }

    @Test
    @DisplayName("404 for unknown order code")
    void testUnknownOrderReturns404() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/api/orders/ORD-9999/status"), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("Strict endpoint throws 500 with NullPointerException for ORD-1042 (before fix)")
    void testStrictEndpointThrowsNpeBeforeFix() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/api/orders/ORD-1042/status-strict"), String.class);

        // 🐛 BEFORE FIX: 500 error, body mentions NullPointerException
        // ✅ AFTER FIX:  200 OK, body contains "SHIPPED"
        // This test documents current behaviour — update the assertion after your fix.
        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }
}
