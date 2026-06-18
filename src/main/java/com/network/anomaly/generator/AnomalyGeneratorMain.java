package com.network.anomaly.generator;

import com.network.anomaly.model.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

public class AnomalyGeneratorMain {

    private static final Logger LOG = LoggerFactory.getLogger(AnomalyGeneratorMain.class);

    public static void main(String[] args) throws Exception {
        int numEvents = Integer.parseInt(System.getenv().getOrDefault("NUM_EVENTS", "10000"));
        double anomalyRatio = Double.parseDouble(System.getenv().getOrDefault("ANOMALY_RATIO", "0.10"));
        String outputPath = System.getenv().getOrDefault("OUTPUT_PATH", "data/generated_events.csv");

        LOG.info("Generating {} events with {}% anomaly ratio", numEvents, (int)(anomalyRatio * 100));

        AnomalyGenerator generator = new AnomalyGenerator(numEvents, anomalyRatio);
        List<NetworkEvent> events = generator.generate();

        long anomalyCount = events.stream().filter(NetworkEvent::isLabeledAnomaly).count();
        LOG.info("Generated {} events ({} normal, {} anomalies)",
            events.size(), events.size() - anomalyCount, anomalyCount);

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println(AnomalyGenerator.toCsvHeader());
            for (NetworkEvent event : events) {
                writer.println(AnomalyGenerator.toCsvRow(event));
            }
        }

        LOG.info("Events written to {}", outputPath);

        long httpCount = events.stream().filter(e -> e.getEventType().name().equals("HTTP")).count();
        long pingCount = events.stream().filter(e -> e.getEventType().name().equals("PING")).count();
        long latencyCount = events.stream().filter(e -> e.getEventType().name().equals("LATENCY")).count();
        long traceCount = events.stream().filter(e -> e.getEventType().name().equals("PATH_TRACE")).count();

        LOG.info("Breakdown: HTTP={}, PING={}, LATENCY={}, PATH_TRACE={}",
            httpCount, pingCount, latencyCount, traceCount);
    }
}
