package com.network.anomaly.generator;

import com.network.anomaly.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class AnomalyGenerator {

    private static final Random random = new Random();

    private static final String[] HTTP_METHODS = {"GET", "POST", "PUT", "DELETE"};
    private static final String[] URLS = {
        "/api/users", "/api/orders", "/api/products", "/api/auth/login",
        "/api/payments", "/api/search", "/api/notifications", "/health"
    };
    private static final String[] SUBNETS = {"10.0", "172.16", "192.168"};
    private static final String[] EXTERNAL_IPS = {
        "8.8.8.8", "1.1.1.1", "203.0.113.1", "198.51.100.1",
        "45.33.32.156", "104.16.0.1", "151.101.1.1"
    };

    private final int numEvents;
    private final double anomalyRatio;

    public AnomalyGenerator(int numEvents, double anomalyRatio) {
        this.numEvents = numEvents;
        this.anomalyRatio = anomalyRatio;
    }

    public List<NetworkEvent> generate() {
        List<NetworkEvent> events = new ArrayList<>(numEvents);

        int anomaliesToGenerate = (int) (numEvents * anomalyRatio);
        int normalEvents = numEvents - anomaliesToGenerate;

        for (int i = 0; i < normalEvents; i++) {
            events.add(generateNormalEvent());
        }

        for (int i = 0; i < anomaliesToGenerate; i++) {
            NetworkEvent anomaly = generateAnomalousEvent();
            anomaly.setLabeledAnomaly(true);
            anomaly.setLabeledAnomalyType(determineAnomalyType(anomaly));
            events.add(anomaly);
        }

        Collections.shuffle(events);
        return events;
    }

    private NetworkEvent generateNormalEvent() {
        EventType type = EventType.values()[random.nextInt(EventType.values().length)];
        switch (type) {
            case HTTP: return generateNormalHttp();
            case PING: return generateNormalPing();
            case LATENCY: return generateNormalLatency();
            case PATH_TRACE: return generateNormalPathTrace();
            default: return generateNormalHttp();
        }
    }

    private NetworkEvent generateAnomalousEvent() {
        EventType type = EventType.values()[random.nextInt(EventType.values().length)];
        switch (type) {
            case HTTP: return generateAnomalousHttp();
            case PING: return generateAnomalousPing();
            case LATENCY: return generateAnomalousLatency();
            case PATH_TRACE: return generateAnomalousPathTrace();
            default: return generateAnomalousHttp();
        }
    }

    private String randomSourceIp() {
        String subnet = SUBNETS[random.nextInt(SUBNETS.length)];
        return subnet + "." + random.nextInt(256) + "." + random.nextInt(256);
    }

    private String randomDestIp() {
        if (random.nextBoolean()) {
            String subnet = SUBNETS[random.nextInt(SUBNETS.length)];
            return subnet + "." + random.nextInt(256) + "." + random.nextInt(256);
        }
        return EXTERNAL_IPS[random.nextInt(EXTERNAL_IPS.length)];
    }

    private HttpEvent generateNormalHttp() {
        String sourceIp = randomSourceIp();
        String destIp = randomDestIp();
        String method = HTTP_METHODS[random.nextInt(HTTP_METHODS.length)];
        String url = URLS[random.nextInt(URLS.length)];
        int statusCode = weightedNormalHttpStatus();
        long responseTimeMs = Math.max(10, (long)(20 + random.nextGaussian() * 100 + 150));
        int requestSize = 100 + random.nextInt(5000);
        return new HttpEvent(sourceIp, destIp, method, url, statusCode, responseTimeMs, requestSize);
    }

    private HttpEvent generateAnomalousHttp() {
        String sourceIp = randomSourceIp();
        String destIp = randomDestIp();
        String method = HTTP_METHODS[random.nextInt(HTTP_METHODS.length)];
        String url = URLS[random.nextInt(URLS.length)];

        int anomalyKind = random.nextInt(3);
        int statusCode;
        long responseTimeMs;

        switch (anomalyKind) {
            case 0:
                statusCode = 500 + random.nextInt(12);
                responseTimeMs = 50 + random.nextInt(500);
                break;
            case 1:
                statusCode = 200;
                responseTimeMs = 3000 + random.nextInt(7000);
                break;
            default:
                statusCode = random.nextBoolean() ? 408 : 429;
                responseTimeMs = 100 + random.nextInt(400);
        }

        return new HttpEvent(sourceIp, destIp, method, url, statusCode, responseTimeMs, 100 + random.nextInt(5000));
    }

    private PingEvent generateNormalPing() {
        String sourceIp = randomSourceIp();
        String destIp = randomDestIp();
        double packetLoss = Math.max(0, random.nextGaussian() * 0.5);
        double rtt = Math.max(1, 20 + random.nextGaussian() * 30);
        int ttl = 32 + random.nextInt(200);
        int packetSize = 32 + random.nextInt(1500);
        return new PingEvent(sourceIp, destIp, packetLoss, rtt, ttl, packetSize);
    }

    private PingEvent generateAnomalousPing() {
        String sourceIp = randomSourceIp();
        String destIp = randomDestIp();

        int anomalyKind = random.nextInt(3);
        double packetLoss;
        double rtt;
        int ttl;

        switch (anomalyKind) {
            case 0:
                packetLoss = 10 + random.nextDouble() * 90;
                rtt = 20 + random.nextDouble() * 100;
                ttl = 32 + random.nextInt(200);
                break;
            case 1:
                packetLoss = random.nextDouble() * 2;
                rtt = 1000 + random.nextDouble() * 4000;
                ttl = 32 + random.nextInt(200);
                break;
            default:
                packetLoss = random.nextDouble() * 2;
                rtt = 10 + random.nextDouble() * 50;
                ttl = random.nextInt(9);
        }

        return new PingEvent(sourceIp, destIp, packetLoss, rtt, ttl, 32 + random.nextInt(1500));
    }

    private LatencyEvent generateNormalLatency() {
        String sourceIp = randomSourceIp();
        String destIp = randomDestIp();
        int hopCount = 3 + random.nextInt(20);
        double avgLatency = Math.max(5, 30 + random.nextGaussian() * 40 + hopCount * 5);
        double minLatency = avgLatency * (0.5 + random.nextDouble() * 0.3);
        double maxLatency = avgLatency * (1.2 + random.nextDouble() * 1.0);
        double jitter = 5 + random.nextDouble() * 50;
        return new LatencyEvent(sourceIp, destIp, hopCount, avgLatency, minLatency, maxLatency, jitter);
    }

    private LatencyEvent generateAnomalousLatency() {
        String sourceIp = randomSourceIp();
        String destIp = randomDestIp();
        int hopCount = 3 + random.nextInt(20);

        int anomalyKind = random.nextInt(3);
        double avgLatency;
        double minLatency;
        double maxLatency;
        double jitter;

        switch (anomalyKind) {
            case 0:
                avgLatency = 2000 + random.nextDouble() * 3000;
                minLatency = avgLatency * 0.5;
                maxLatency = avgLatency * 2;
                jitter = 50 + random.nextDouble() * 200;
                break;
            case 1:
                avgLatency = 50 + random.nextDouble() * 200;
                minLatency = 10 + random.nextDouble() * 50;
                maxLatency = 300 + random.nextDouble() * 2000;
                jitter = 300 + random.nextDouble() * 700;
                break;
            default:
                avgLatency = 100 + random.nextDouble() * 400;
                minLatency = avgLatency * 0.3;
                maxLatency = avgLatency * 3.5;
                jitter = 50 + random.nextDouble() * 100;
        }

        return new LatencyEvent(sourceIp, destIp, hopCount, avgLatency, minLatency, maxLatency, jitter);
    }

    private PathTraceEvent generateNormalPathTrace() {
        String sourceIp = randomSourceIp();
        String destIp = randomDestIp();
        int totalHops = 5 + random.nextInt(20);
        double completionPercent = 100;
        double avgHopLatency = 10 + random.nextDouble() * 50;
        List<String> hops = generateHops(totalHops, 100);
        return new PathTraceEvent(sourceIp, destIp, hops, totalHops, completionPercent, avgHopLatency);
    }

    private PathTraceEvent generateAnomalousPathTrace() {
        String sourceIp = randomSourceIp();
        String destIp = randomDestIp();

        int anomalyKind = random.nextInt(3);
        int totalHops;
        double completionPercent;
        double avgHopLatency;
        List<String> hops;

        switch (anomalyKind) {
            case 0:
                totalHops = 5 + random.nextInt(15);
                completionPercent = 30 + random.nextDouble() * 69;
                avgHopLatency = 20 + random.nextDouble() * 100;
                hops = generateHops(totalHops, completionPercent);
                break;
            case 1:
                totalHops = 5 + random.nextInt(15);
                completionPercent = 100;
                avgHopLatency = 1000 + random.nextDouble() * 2000;
                hops = generateHops(totalHops, completionPercent);
                break;
            default:
                totalHops = 31 + random.nextInt(50);
                completionPercent = 100;
                avgHopLatency = 30 + random.nextDouble() * 100;
                hops = generateHops(totalHops, completionPercent);
        }

        return new PathTraceEvent(sourceIp, destIp, hops, totalHops, completionPercent, avgHopLatency);
    }

    private List<String> generateHops(int totalHops, double completionPercent) {
        List<String> hops = new ArrayList<>();
        int completedHops = (int) Math.ceil(totalHops * completionPercent / 100.0);
        for (int i = 0; i < completedHops; i++) {
            int a = random.nextInt(256);
            int b = random.nextInt(256);
            int c = random.nextInt(256);
            hops.add(a + "." + b + "." + c + ".1");
        }
        return hops;
    }

    private int weightedNormalHttpStatus() {
        double r = random.nextDouble();
        if (r < 0.70) return 200;
        if (r < 0.80) return 201;
        if (r < 0.85) return 204;
        if (r < 0.90) return 301;
        if (r < 0.93) return 304;
        if (r < 0.96) return 400;
        if (r < 0.98) return 401;
        if (r < 0.99) return 403;
        return 404;
    }

    private String determineAnomalyType(NetworkEvent event) {
        if (event instanceof HttpEvent) {
            HttpEvent http = (HttpEvent) event;
            if (http.getStatusCode() >= 500) return "SERVER_ERROR";
            if (http.getResponseTimeMs() > 3000) return "SLOW_RESPONSE";
            if (http.getStatusCode() == 408 || http.getStatusCode() == 429) return "RATE_LIMITED";
        } else if (event instanceof PingEvent) {
            PingEvent ping = (PingEvent) event;
            if (ping.getPacketLossPercent() > 10) return "HIGH_PACKET_LOSS";
            if (ping.getRoundTripTimeMs() > 1000) return "HIGH_RTT_LATENCY";
            if (ping.getTtl() < 10) return "UNUSUAL_TTL";
        } else if (event instanceof LatencyEvent) {
            LatencyEvent latency = (LatencyEvent) event;
            if (latency.getAvgLatencyMs() > 2000) return "HIGH_LATENCY";
            if (latency.getJitterMs() > 300) return "HIGH_JITTER";
            if (latency.getMaxLatencyMs() > latency.getAvgLatencyMs() * 3) return "LATENCY_SPIKE";
        } else if (event instanceof PathTraceEvent) {
            PathTraceEvent trace = (PathTraceEvent) event;
            if (trace.getCompletionPercent() < 100) return "INCOMPLETE_PATH";
            if (trace.getAvgHopLatencyMs() > 1000) return "SLOW_HOP_LATENCY";
            if (trace.getTotalHops() > 30) return "EXCESSIVE_HOPS";
        }
        return "UNKNOWN";
    }

    public static String toCsvHeader() {
        return "eventId,timestamp,eventType,sourceIp,destinationIp,"
            + "httpMethod,url,statusCode,responseTimeMs,requestSize,"
            + "packetLossPercent,roundTripTimeMs,ttl,packetSize,"
            + "hopCount,avgLatencyMs,minLatencyMs,maxLatencyMs,jitterMs,"
            + "totalHops,completionPercent,avgHopLatencyMs,"
            + "isAnomaly,anomalyType";
    }

    public static String toCsvRow(NetworkEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append(event.getEventId()).append(",");
        sb.append(event.getTimestamp()).append(",");
        sb.append(event.getEventType()).append(",");
        sb.append(event.getSourceIp()).append(",");
        sb.append(event.getDestinationIp()).append(",");

        if (event instanceof HttpEvent) {
            HttpEvent http = (HttpEvent) event;
            sb.append(http.getHttpMethod()).append(",");
            sb.append(http.getUrl()).append(",");
            sb.append(http.getStatusCode()).append(",");
            sb.append(http.getResponseTimeMs()).append(",");
            sb.append(http.getRequestSize()).append(",");
            sb.append(",,,,,,,,");
        } else if (event instanceof PingEvent) {
            PingEvent ping = (PingEvent) event;
            sb.append(",,,,,");
            sb.append(ping.getPacketLossPercent()).append(",");
            sb.append(ping.getRoundTripTimeMs()).append(",");
            sb.append(ping.getTtl()).append(",");
            sb.append(ping.getPacketSize()).append(",");
            sb.append(",,,,,");
        } else if (event instanceof LatencyEvent) {
            LatencyEvent latency = (LatencyEvent) event;
            sb.append(",,,,,,,,");
            sb.append(latency.getHopCount()).append(",");
            sb.append(latency.getAvgLatencyMs()).append(",");
            sb.append(latency.getMinLatencyMs()).append(",");
            sb.append(latency.getMaxLatencyMs()).append(",");
            sb.append(latency.getJitterMs()).append(",");
            sb.append(",");
        } else if (event instanceof PathTraceEvent) {
            PathTraceEvent trace = (PathTraceEvent) event;
            sb.append(",,,,,,,,,,,,");
            sb.append(trace.getTotalHops()).append(",");
            sb.append(trace.getCompletionPercent()).append(",");
            sb.append(trace.getAvgHopLatencyMs()).append(",");
        } else {
            sb.append(",,,,,,,,,,,,,,,,");
        }

        sb.append(",");
        sb.append(event.isLabeledAnomaly() ? "true" : "false").append(",");
        sb.append(event.getLabeledAnomalyType() != null ? event.getLabeledAnomalyType() : "");
        return sb.toString();
    }
}
