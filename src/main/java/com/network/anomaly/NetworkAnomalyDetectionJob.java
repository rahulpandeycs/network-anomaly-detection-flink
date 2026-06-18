package com.network.anomaly;

import com.network.anomaly.model.AnomalyEvent;
import com.network.anomaly.model.NetworkEvent;
import com.network.anomaly.sink.AnomalyCsvSink;
import com.network.anomaly.anomaly.AnomalyDetectionProcessFunction;
import com.network.anomaly.serialization.NetworkEventDeserializationSchema;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class NetworkAnomalyDetectionJob {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkAnomalyDetectionJob.class);

    public static void main(String[] args) throws Exception {
        String bootstrapServers = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        String inputTopic = System.getenv().getOrDefault("KAFKA_INPUT_TOPIC", "network.raw.events");
        String outputPath = System.getenv().getOrDefault("ANOMALY_OUTPUT_PATH", "data/anomalies");
        String groupId = System.getenv().getOrDefault("KAFKA_GROUP_ID", "network-anomaly-detection");

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(Duration.ofSeconds(30).toMillis());

        KafkaSource<NetworkEvent> kafkaSource = KafkaSource.<NetworkEvent>builder()
            .setBootstrapServers(bootstrapServers)
            .setTopics(inputTopic)
            .setGroupId(groupId)
            .setStartingOffsets(OffsetsInitializer.latest())
            .setDeserializer(new NetworkEventDeserializationSchema())
            .build();

        DataStream<NetworkEvent> eventStream = env
            .fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "kafka-source")
            .name("Network Event Source");

        DataStream<AnomalyEvent> anomalyStream = eventStream
            .process(new AnomalyDetectionProcessFunction())
            .name("anomaly-detector")
            .uid("anomaly-detector");

        DataStream<String> csvStream = anomalyStream
            .map(anomaly -> {
                String csv = anomaly.toCsvRow();
                LOG.info("ANOMALY DETECTED: {}", anomaly.getDescription());
                return csv;
            })
            .name("anomaly-to-csv");

        csvStream.addSink(AnomalyCsvSink.create(outputPath))
            .name("anomaly-csv-sink")
            .uid("anomaly-csv-sink");

        LOG.info("Starting Network Anomaly Detection Flink job");
        LOG.info("Kafka: {} topic: {} output: {}", bootstrapServers, inputTopic, outputPath);

        env.execute("Network Anomaly Detection Pipeline");
    }
}
