package com.network.anomaly;

import com.network.anomaly.anomaly.HttpAnomalyDetector;
import com.network.anomaly.anomaly.LatencyAnomalyDetector;
import com.network.anomaly.anomaly.PathTraceAnomalyDetector;
import com.network.anomaly.anomaly.PingAnomalyDetector;
import com.network.anomaly.model.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AnomalyDetectorTest {

    private final HttpAnomalyDetector httpDetector = new HttpAnomalyDetector();
    private final PingAnomalyDetector pingDetector = new PingAnomalyDetector();
    private final LatencyAnomalyDetector latencyDetector = new LatencyAnomalyDetector();
    private final PathTraceAnomalyDetector pathTraceDetector = new PathTraceAnomalyDetector();

    @Test
    void testHttpServerErrorAnomaly() {
        HttpEvent event = new HttpEvent("10.0.1.1", "8.8.8.8", "GET",
            "/api/users", 500, 100, 500);
        Optional<AnomalyEvent> result = httpDetector.detect(event);
        assertTrue(result.isPresent());
        assertEquals(AnomalyType.SERVER_ERROR, result.get().getAnomalyType());
        assertEquals(Severity.HIGH, result.get().getSeverity());
    }

    @Test
    void testHttpSlowResponseAnomaly() {
        HttpEvent event = new HttpEvent("10.0.1.1", "8.8.8.8", "POST",
            "/api/search", 200, 5000, 1000);
        Optional<AnomalyEvent> result = httpDetector.detect(event);
        assertTrue(result.isPresent());
        assertEquals(AnomalyType.SLOW_RESPONSE, result.get().getAnomalyType());
        assertEquals(Severity.MEDIUM, result.get().getSeverity());
    }

    @Test
    void testHttpRateLimitedAnomaly() {
        HttpEvent event = new HttpEvent("10.0.1.1", "8.8.8.8", "GET",
            "/api/auth/login", 429, 50, 200);
        Optional<AnomalyEvent> result = httpDetector.detect(event);
        assertTrue(result.isPresent());
        assertEquals(AnomalyType.RATE_LIMITED, result.get().getAnomalyType());
    }

    @Test
    void testHttpNormalEventNoAnomaly() {
        HttpEvent event = new HttpEvent("10.0.1.1", "8.8.8.8", "GET",
            "/api/users", 200, 120, 500);
        Optional<AnomalyEvent> result = httpDetector.detect(event);
        assertFalse(result.isPresent());
    }

    @Test
    void testPingHighPacketLossAnomaly() {
        PingEvent event = new PingEvent("10.0.1.1", "8.8.8.8", 25.0, 50, 64, 64);
        Optional<AnomalyEvent> result = pingDetector.detect(event);
        assertTrue(result.isPresent());
        assertEquals(AnomalyType.HIGH_PACKET_LOSS, result.get().getAnomalyType());
    }

    @Test
    void testPingHighRttAnomaly() {
        PingEvent event = new PingEvent("10.0.1.1", "8.8.8.8", 0.5, 2000, 64, 64);
        Optional<AnomalyEvent> result = pingDetector.detect(event);
        assertTrue(result.isPresent());
        assertEquals(AnomalyType.HIGH_RTT_LATENCY, result.get().getAnomalyType());
    }

    @Test
    void testPingUnusualTtlAnomaly() {
        PingEvent event = new PingEvent("10.0.1.1", "8.8.8.8", 0.0, 20, 3, 64);
        Optional<AnomalyEvent> result = pingDetector.detect(event);
        assertTrue(result.isPresent());
        assertEquals(AnomalyType.UNUSUAL_TTL, result.get().getAnomalyType());
    }

    @Test
    void testPingNormalNoAnomaly() {
        PingEvent event = new PingEvent("10.0.1.1", "8.8.8.8", 0.0, 20, 64, 64);
        Optional<AnomalyEvent> result = pingDetector.detect(event);
        assertFalse(result.isPresent());
    }

    @Test
    void testLatencyHighLatencyAnomaly() {
        LatencyEvent event = new LatencyEvent("10.0.1.1", "8.8.8.8", 15, 3000, 2000, 4000, 100);
        Optional<AnomalyEvent> result = latencyDetector.detect(event);
        assertTrue(result.isPresent());
        assertEquals(AnomalyType.HIGH_LATENCY, result.get().getAnomalyType());
        assertEquals(Severity.CRITICAL, result.get().getSeverity());
    }

    @Test
    void testLatencyHighJitterAnomaly() {
        LatencyEvent event = new LatencyEvent("10.0.1.1", "8.8.8.8", 10, 100, 50, 150, 500);
        Optional<AnomalyEvent> result = latencyDetector.detect(event);
        assertTrue(result.isPresent());
        assertEquals(AnomalyType.HIGH_JITTER, result.get().getAnomalyType());
    }

    @Test
    void testLatencySpikeAnomaly() {
        LatencyEvent event = new LatencyEvent("10.0.1.1", "8.8.8.8", 10, 100, 50, 600, 50);
        Optional<AnomalyEvent> result = latencyDetector.detect(event);
        assertTrue(result.isPresent());
        assertEquals(AnomalyType.LATENCY_SPIKE, result.get().getAnomalyType());
    }

    @Test
    void testLatencyNormalNoAnomaly() {
        LatencyEvent event = new LatencyEvent("10.0.1.1", "8.8.8.8", 10, 50, 30, 80, 15);
        Optional<AnomalyEvent> result = latencyDetector.detect(event);
        assertFalse(result.isPresent());
    }

    @Test
    void testPathTraceIncompleteAnomaly() {
        PathTraceEvent event = new PathTraceEvent("10.0.1.1", "8.8.8.8",
            Arrays.asList("1.1.1.1", "2.2.2.2"), 10, 20.0, 50);
        Optional<AnomalyEvent> result = pathTraceDetector.detect(event);
        assertTrue(result.isPresent());
        assertEquals(AnomalyType.INCOMPLETE_PATH, result.get().getAnomalyType());
    }

    @Test
    void testPathTraceSlowHopsAnomaly() {
        PathTraceEvent event = new PathTraceEvent("10.0.1.1", "8.8.8.8",
            Arrays.asList("1.1.1.1", "2.2.2.2"), 5, 100.0, 2000);
        Optional<AnomalyEvent> result = pathTraceDetector.detect(event);
        assertTrue(result.isPresent());
        assertEquals(AnomalyType.SLOW_HOP_LATENCY, result.get().getAnomalyType());
    }

    @Test
    void testPathTraceExcessiveHopsAnomaly() {
        PathTraceEvent event = new PathTraceEvent("10.0.1.1", "8.8.8.8",
            Arrays.asList("1.1.1.1"), 40, 100.0, 30);
        Optional<AnomalyEvent> result = pathTraceDetector.detect(event);
        assertTrue(result.isPresent());
        assertEquals(AnomalyType.EXCESSIVE_HOPS, result.get().getAnomalyType());
    }

    @Test
    void testPathTraceNormalNoAnomaly() {
        PathTraceEvent event = new PathTraceEvent("10.0.1.1", "8.8.8.8",
            Arrays.asList("1.1.1.1", "2.2.2.2", "3.3.3.3"), 15, 100.0, 30);
        Optional<AnomalyEvent> result = pathTraceDetector.detect(event);
        assertFalse(result.isPresent());
    }

    @Test
    void testAnomalyEventCsvFormat() {
        AnomalyEvent anomaly = new AnomalyEvent(
            "test-id", 123456789L, EventType.HTTP,
            "10.0.1.1", "8.8.8.8",
            AnomalyType.SERVER_ERROR, Severity.HIGH,
            "HTTP 500 error",
            "{\"eventId\":\"test-id\"}"
        );

        assertTrue(anomaly.toCsvRow().contains("test-id"));
        assertTrue(anomaly.toCsvRow().contains("SERVER_ERROR"));
        assertTrue(anomaly.toCsvRow().contains("HTTP 500 error"));
    }

    @Test
    void testAnomalyGeneratorCreatesMix() {
        com.network.anomaly.generator.AnomalyGenerator gen =
            new com.network.anomaly.generator.AnomalyGenerator(1000, 0.10);
        List<NetworkEvent> events = gen.generate();

        assertEquals(1000, events.size());
        long anomalyCount = events.stream().filter(NetworkEvent::isLabeledAnomaly).count();
        assertTrue(anomalyCount >= 50 && anomalyCount <= 150,
            "Expected ~10% anomalies but got " + anomalyCount);
    }
}
