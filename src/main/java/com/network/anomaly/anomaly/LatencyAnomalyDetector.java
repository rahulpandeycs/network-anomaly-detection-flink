package com.network.anomaly.anomaly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.network.anomaly.model.AnomalyEvent;
import com.network.anomaly.model.AnomalyType;
import com.network.anomaly.model.LatencyEvent;
import com.network.anomaly.model.Severity;

import java.util.Optional;

public class LatencyAnomalyDetector implements AnomalyDetector<LatencyEvent> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final double HIGH_LATENCY_THRESHOLD_MS = 2000.0;
    private static final double HIGH_JITTER_THRESHOLD_MS = 300.0;

    @Override
    public Optional<AnomalyEvent> detect(LatencyEvent event) {
        String rawJson;
        try {
            rawJson = mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            rawJson = "{}";
        }

        if (event.getAvgLatencyMs() > HIGH_LATENCY_THRESHOLD_MS) {
            return Optional.of(new AnomalyEvent(
                event.getEventId(), event.getTimestamp(), event.getEventType(),
                event.getSourceIp(), event.getDestinationIp(),
                AnomalyType.HIGH_LATENCY, Severity.CRITICAL,
                String.format("High avg latency %.1fms on path to %s (%d hops)",
                    event.getAvgLatencyMs(), event.getDestinationIp(), event.getHopCount()),
                rawJson
            ));
        }

        if (event.getJitterMs() > HIGH_JITTER_THRESHOLD_MS) {
            return Optional.of(new AnomalyEvent(
                event.getEventId(), event.getTimestamp(), event.getEventType(),
                event.getSourceIp(), event.getDestinationIp(),
                AnomalyType.HIGH_JITTER, Severity.HIGH,
                String.format("High jitter %.1fms on path to %s", event.getJitterMs(), event.getDestinationIp()),
                rawJson
            ));
        }

        if (event.getMaxLatencyMs() > event.getAvgLatencyMs() * 3
            && event.getMaxLatencyMs() > 500) {
            return Optional.of(new AnomalyEvent(
                event.getEventId(), event.getTimestamp(), event.getEventType(),
                event.getSourceIp(), event.getDestinationIp(),
                AnomalyType.LATENCY_SPIKE, Severity.MEDIUM,
                String.format("Latency spike: max %.1fms vs avg %.1fms to %s",
                    event.getMaxLatencyMs(), event.getAvgLatencyMs(), event.getDestinationIp()),
                rawJson
            ));
        }

        return Optional.empty();
    }
}
