package com.network.anomaly.anomaly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.network.anomaly.model.AnomalyEvent;
import com.network.anomaly.model.AnomalyType;
import com.network.anomaly.model.HttpEvent;
import com.network.anomaly.model.Severity;

import java.util.Optional;

public class HttpAnomalyDetector implements AnomalyDetector<HttpEvent> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final long SLOW_RESPONSE_THRESHOLD_MS = 3000;

    @Override
    public Optional<AnomalyEvent> detect(HttpEvent event) {
        String rawJson;
        try {
            rawJson = mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            rawJson = "{}";
        }

        if (event.getStatusCode() >= 500) {
            return Optional.of(new AnomalyEvent(
                event.getEventId(), event.getTimestamp(), event.getEventType(),
                event.getSourceIp(), event.getDestinationIp(),
                AnomalyType.SERVER_ERROR, Severity.HIGH,
                String.format("HTTP %d on %s %s", event.getStatusCode(), event.getHttpMethod(), event.getUrl()),
                rawJson
            ));
        }

        if (event.getResponseTimeMs() > SLOW_RESPONSE_THRESHOLD_MS) {
            return Optional.of(new AnomalyEvent(
                event.getEventId(), event.getTimestamp(), event.getEventType(),
                event.getSourceIp(), event.getDestinationIp(),
                AnomalyType.SLOW_RESPONSE, Severity.MEDIUM,
                String.format("Slow response %dms for %s %s", event.getResponseTimeMs(), event.getHttpMethod(), event.getUrl()),
                rawJson
            ));
        }

        if (event.getStatusCode() == 408 || event.getStatusCode() == 429) {
            return Optional.of(new AnomalyEvent(
                event.getEventId(), event.getTimestamp(), event.getEventType(),
                event.getSourceIp(), event.getDestinationIp(),
                AnomalyType.RATE_LIMITED, Severity.LOW,
                String.format("HTTP %d on %s %s", event.getStatusCode(), event.getHttpMethod(), event.getUrl()),
                rawJson
            ));
        }

        return Optional.empty();
    }
}
