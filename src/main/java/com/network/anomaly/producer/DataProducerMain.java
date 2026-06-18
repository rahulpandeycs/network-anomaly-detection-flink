package com.network.anomaly.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.network.anomaly.model.*;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class DataProducerMain {

    private static final Logger LOG = LoggerFactory.getLogger(DataProducerMain.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Random random = new Random();

    private static final String[] HTTP_METHODS = {"GET", "POST", "PUT", "DELETE", "PATCH"};
    private static final String[] URLS = {
        "/api/users", "/api/orders", "/api/products", "/api/auth/login",
        "/api/payments", "/api/search", "/api/notifications", "/health"
    };
    private static final String[] SUBNETS = {"10.0", "172.16", "192.168"};
    private static final String[] EXTERNAL_IPS = {
        "8.8.8.8", "1.1.1.1", "203.0.113.1", "198.51.100.1",
        "45.33.32.156", "104.16.0.1", "151.101.1.1"
    };

    private final String bootstrapServers;
    private final String topic;
    private final int eventsPerSecond;
    private final double anomalyRate;
    private final AtomicLong counter = new AtomicLong(0);

    public DataProducerMain(String bootstrapServers, String topic,
                            int eventsPerSecond, double anomalyRate) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.eventsPerSecond = eventsPerSecond;
        this.anomalyRate = anomalyRate;
    }

    public void run() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "1");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        LOG.info("Starting data producer: {} events/sec, {} anomaly rate, topic '{}'",
            eventsPerSecond, anomalyRate, topic);

        scheduler.scheduleAtFixedRate(() -> {
            int batchSize = Math.max(1, eventsPerSecond / 10);
            for (int i = 0; i < batchSize; i++) {
                try {
                    NetworkEvent event = generateEvent();
                    String json = mapper.writeValueAsString(event);
                    producer.send(new ProducerRecord<>(topic, event.getEventId(), json),
                        (metadata, exception) -> {
                            if (exception != null) {
                                LOG.error("Failed to send event", exception);
                            }
                        });
                    long count = counter.incrementAndGet();
                    if (count % 100 == 0) {
                        LOG.info("Produced {} events to {}", count, topic);
                    }
                } catch (Exception e) {
                    LOG.error("Error generating event", e);
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down producer");
            scheduler.shutdown();
            producer.close();
        }));
    }

    private NetworkEvent generateEvent() {
        boolean isAnomaly = random.nextDouble() < anomalyRate;
        EventType type = EventType.values()[random.nextInt(EventType.values().length)];

        NetworkEvent event;
        switch (type) {
            case HTTP:
                event = generateHttpEvent(isAnomaly);
                break;
            case PING:
                event = generatePingEvent(isAnomaly);
                break;
            case LATENCY:
                event = generateLatencyEvent(isAnomaly);
                break;
            case PATH_TRACE:
                event = generatePathTraceEvent(isAnomaly);
                break;
            default:
                event = generateHttpEvent(isAnomaly);
        }

        return event;
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

    private HttpEvent generateHttpEvent(boolean isAnomaly) {
        String sourceIp = randomSourceIp();
        String destIp = randomDestIp();
        String method = HTTP_METHODS[random.nextInt(HTTP_METHODS.length)];
        String url = URLS[random.nextInt(URLS.length)];

        int statusCode;
        long responseTimeMs;
        int requestSize = 100 + random.nextInt(5000);

        if (isAnomaly) {
            int anomalyType = random.nextInt(3);
            switch (anomalyType) {
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
        } else {
            statusCode = randomWeighedHttpStatus();
            responseTimeMs = 20 + (long)(random.nextGaussian() * 100 + 150);
            if (responseTimeMs < 10) responseTimeMs = 10;
        }

        return new HttpEvent(sourceIp, destIp, method, url, statusCode, responseTimeMs, requestSize);
    }

    private int randomWeighedHttpStatus() {
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

    private PingEvent generatePingEvent(boolean isAnomaly) {
        String sourceIp = randomSourceIp();
        String destIp = randomDestIp();

        double packetLoss;
        double rtt;
        int ttl;
        int packetSize = 32 + random.nextInt(1500);

        if (isAnomaly) {
            int anomalyType = random.nextInt(3);
            switch (anomalyType) {
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
        } else {
            packetLoss = Math.max(0, random.nextGaussian() * 0.5);
            rtt = Math.max(1, 20 + random.nextGaussian() * 30);
            ttl = 32 + random.nextInt(200);
        }

        return new PingEvent(sourceIp, destIp, packetLoss, rtt, ttl, packetSize);
    }

    private LatencyEvent generateLatencyEvent(boolean isAnomaly) {
        String sourceIp = randomSourceIp();
        String destIp = randomDestIp();
        int hopCount = 3 + random.nextInt(20);

        double avgLatency;
        double minLatency;
        double maxLatency;
        double jitter;

        if (isAnomaly) {
            int anomalyType = random.nextInt(3);
            switch (anomalyType) {
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
        } else {
            avgLatency = 30 + random.nextGaussian() * 40 + hopCount * 5;
            minLatency = avgLatency * (0.5 + random.nextDouble() * 0.3);
            maxLatency = avgLatency * (1.2 + random.nextDouble() * 1.0);
            jitter = 5 + random.nextDouble() * 50;
            if (avgLatency < 5) avgLatency = 5;
        }

        return new LatencyEvent(sourceIp, destIp, hopCount, avgLatency, minLatency, maxLatency, jitter);
    }

    private PathTraceEvent generatePathTraceEvent(boolean isAnomaly) {
        String sourceIp = randomSourceIp();
        String destIp = randomDestIp();

        int totalHops;
        double completionPercent;
        double avgHopLatency;
        List<String> hops;

        if (isAnomaly) {
            int anomalyType = random.nextInt(3);
            switch (anomalyType) {
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
        } else {
            totalHops = 5 + random.nextInt(20);
            completionPercent = 100;
            avgHopLatency = 10 + random.nextDouble() * 50;
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

    public static void main(String[] args) {
        String bootstrapServers = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        String topic = System.getenv().getOrDefault("KAFKA_TOPIC", "network.raw.events");
        int eventsPerSecond = Integer.parseInt(System.getenv().getOrDefault("EVENTS_PER_SECOND", "50"));
        double anomalyRate = Double.parseDouble(System.getenv().getOrDefault("ANOMALY_RATE", "0.05"));

        DataProducerMain producer = new DataProducerMain(bootstrapServers, topic, eventsPerSecond, anomalyRate);
        producer.run();

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
