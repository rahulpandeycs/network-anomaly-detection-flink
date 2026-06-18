# Network Anomaly Detection with Apache Flink

Real-time anomaly detection pipeline for network telemetry data (HTTP, ping, latency, path trace) using Apache Flink streaming, Kafka, and Docker.

## Architecture

```
┌─────────────────────┐     produces       ┌──────────────────┐
│  DataProducerMain   │─────► Kafka ──────►│  NetworkAnomaly  │
│  (random generator) │     topic:         │  DetectionJob    │
│                     │  network.raw.events│  (Flink stream)  │
└─────────────────────┘                    └────────┬─────────┘
                                                    │ writes detected
┌─────────────────────┐                             │ anomalies
│  AnomalyGenerator   │─────► labeled CSV           ▼
│  (standalone)       │     (test/validation) ┌──────────────┐
└─────────────────────┘                       │  FileSink    │
                                              │  (CSV files) │
                                              └──────────────┘
```

## Project Structure

```
src/main/java/com/network/anomaly/
├── NetworkAnomalyDetectionJob.java   # Flink pipeline entry point
├── model/                            # Event data models
│   ├── EventType.java                # HTTP, PING, LATENCY, PATH_TRACE
│   ├── Severity.java                 # CRITICAL, HIGH, MEDIUM, LOW
│   ├── AnomalyType.java              # 12 anomaly categories
│   ├── NetworkEvent.java             # Abstract base (Jackson polymorphic)
│   ├── HttpEvent.java
│   ├── PingEvent.java
│   ├── LatencyEvent.java
│   ├── PathTraceEvent.java
│   └── AnomalyEvent.java             # Detection output
├── serialization/
│   └── NetworkEventDeserializationSchema.java  # Kafka → POJO
├── anomaly/                           # Detection logic
│   ├── AnomalyDetector.java          # Interface
│   ├── HttpAnomalyDetector.java
│   ├── PingAnomalyDetector.java
│   ├── LatencyAnomalyDetector.java
│   ├── PathTraceAnomalyDetector.java
│   └── AnomalyDetectionProcessFunction.java
├── sink/
│   └── AnomalyCsvSink.java           # StreamingFileSink for CSV
├── producer/
│   └── DataProducerMain.java         # Kafka event producer
└── generator/
    ├── AnomalyGeneratorMain.java
    └── AnomalyGenerator.java          # Labeled test CSV generator
```

## Anomaly Detection Rules

| Event | Anomaly | Severity | Condition |
|---|---|---|---|
| **HTTP** | `SERVER_ERROR` | HIGH | Status code >= 500 |
| | `SLOW_RESPONSE` | MEDIUM | Response time > 3000ms |
| | `RATE_LIMITED` | LOW | Status 408 or 429 |
| **Ping** | `HIGH_PACKET_LOSS` | HIGH | Packet loss > 10% |
| | `HIGH_RTT_LATENCY` | MEDIUM | Round-trip time > 1000ms |
| | `UNUSUAL_TTL` | LOW | TTL < 10 |
| **Latency** | `HIGH_LATENCY` | CRITICAL | Avg latency > 2000ms |
| | `HIGH_JITTER` | HIGH | Jitter > 300ms |
| | `LATENCY_SPIKE` | MEDIUM | Max > avg × 3 (and > 500ms) |
| **PathTrace** | `INCOMPLETE_PATH` | HIGH | Completion < 100% |
| | `SLOW_HOP_LATENCY` | MEDIUM | Avg hop latency > 1000ms |
| | `EXCESSIVE_HOPS` | LOW | Hops > 30 |

## Prerequisites

- Java 11+
- Docker & Docker Compose
- Gradle (wrapper included)

## Getting Started

### 1. Start Infrastructure

```bash
docker compose up -d
```

Starts Kafka (port 9092), Zookeeper (2181), and Flink cluster (web UI at http://localhost:8081).

### 2. Generate Labeled Test Data

```bash
./gradlew runGenerator
```

Writes `data/generated_events.csv` with 10,000 events (10% labeled anomalies).

### 3. Produce Live Events to Kafka

```bash
./gradlew runProducer
```

Generates 50 events/sec (5% anomaly rate) to Kafka topic `network.raw.events`.

### 4. Run Flink Detection Pipeline

```bash
./gradlew runFlinkJob
```

Consumes from Kafka, detects anomalies in real-time, writes results to `data/anomalies/`.

### 5. Build Fat JAR (for cluster submission)

```bash
./gradlew shadowJar
# Submit build/libs/network-anomaly-detection-1.0.0-all.jar to Flink UI at :8081
```

## Configuration

All configuration is via environment variables:

| Variable | Default | Used By |
|---|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Flink job + producer |
| `KAFKA_INPUT_TOPIC` | `network.raw.events` | Flink job |
| `ANOMALY_OUTPUT_PATH` | `data/anomalies` | Flink job |
| `KAFKA_GROUP_ID` | `network-anomaly-detection` | Flink job |
| `EVENTS_PER_SECOND` | `50` | Producer |
| `ANOMALY_RATE` | `0.05` | Producer |
| `NUM_EVENTS` | `10000` | Generator |
| `ANOMALY_RATIO` | `0.10` | Generator |

## Testing

```bash
./gradlew test
```

Runs 18 tests covering all 4 detectors (every anomaly type + normal cases), CSV formatting, and generator distribution.

## Tech Stack

- **Apache Flink 1.19** — stream processing
- **Apache Kafka 3.7** — event ingestion
- **Jackson 2.17** — JSON serialization
- **Gradle 8.9 / Shadow JAR** — build & packaging
- **JUnit 5** — testing
- **Docker Compose** — local infrastructure
