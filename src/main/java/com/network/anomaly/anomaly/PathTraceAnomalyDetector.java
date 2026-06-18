package com.network.anomaly.anomaly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.network.anomaly.model.AnomalyEvent;
import com.network.anomaly.model.AnomalyType;
import com.network.anomaly.model.PathTraceEvent;
import com.network.anomaly.model.Severity;

import java.util.Optional;

public class PathTraceAnomalyDetector implements AnomalyDetector<PathTraceEvent> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final double MIN_COMPLETION_PERCENT = 100.0;
    private static final double SLOW_HOP_THRESHOLD_MS = 1000.0;
    private static final int MAX_HOPS = 30;

    @Override
    public Optional<AnomalyEvent> detect(PathTraceEvent event) {
        String rawJson;
        try {
            rawJson = mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            rawJson = "{}";
        }

        if (event.getCompletionPercent() < MIN_COMPLETION_PERCENT) {
            return Optional.of(new AnomalyEvent(
                event.getEventId(), event.getTimestamp(), event.getEventType(),
                event.getSourceIp(), event.getDestinationIp(),
                AnomalyType.INCOMPLETE_PATH, Severity.HIGH,
                String.format("Incomplete path trace to %s: %.1f%% completed",
                    event.getDestinationIp(), event.getCompletionPercent()),
                rawJson
            ));
        }

        if (event.getAvgHopLatencyMs() > SLOW_HOP_THRESHOLD_MS) {
            return Optional.of(new AnomalyEvent(
                event.getEventId(), event.getTimestamp(), event.getEventType(),
                event.getSourceIp(), event.getDestinationIp(),
                AnomalyType.SLOW_HOP_LATENCY, Severity.MEDIUM,
                String.format("Slow hop latency avg %.1fms on path to %s",
                    event.getAvgHopLatencyMs(), event.getDestinationIp()),
                rawJson
            ));
        }

        if (event.getTotalHops() > MAX_HOPS) {
            return Optional.of(new AnomalyEvent(
                event.getEventId(), event.getTimestamp(), event.getEventType(),
                event.getSourceIp(), event.getDestinationIp(),
                AnomalyType.EXCESSIVE_HOPS, Severity.LOW,
                String.format("Excessive hops (%d) on path to %s",
                    event.getTotalHops(), event.getDestinationIp()),
                rawJson
            ));
        }

        return Optional.empty();
    }
}
