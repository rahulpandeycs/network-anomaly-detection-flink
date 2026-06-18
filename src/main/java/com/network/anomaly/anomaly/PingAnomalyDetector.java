package com.network.anomaly.anomaly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.network.anomaly.model.AnomalyEvent;
import com.network.anomaly.model.AnomalyType;
import com.network.anomaly.model.PingEvent;
import com.network.anomaly.model.Severity;

import java.util.Optional;

public class PingAnomalyDetector implements AnomalyDetector<PingEvent> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final double PACKET_LOSS_THRESHOLD = 10.0;
    private static final double RTT_THRESHOLD_MS = 1000.0;
    private static final int MIN_TTL = 10;

    @Override
    public Optional<AnomalyEvent> detect(PingEvent event) {
        String rawJson;
        try {
            rawJson = mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            rawJson = "{}";
        }

        if (event.getPacketLossPercent() > PACKET_LOSS_THRESHOLD) {
            return Optional.of(new AnomalyEvent(
                event.getEventId(), event.getTimestamp(), event.getEventType(),
                event.getSourceIp(), event.getDestinationIp(),
                AnomalyType.HIGH_PACKET_LOSS, Severity.HIGH,
                String.format("Packet loss %.1f%% to %s", event.getPacketLossPercent(), event.getDestinationIp()),
                rawJson
            ));
        }

        if (event.getRoundTripTimeMs() > RTT_THRESHOLD_MS) {
            return Optional.of(new AnomalyEvent(
                event.getEventId(), event.getTimestamp(), event.getEventType(),
                event.getSourceIp(), event.getDestinationIp(),
                AnomalyType.HIGH_RTT_LATENCY, Severity.MEDIUM,
                String.format("RTT %.1fms to %s", event.getRoundTripTimeMs(), event.getDestinationIp()),
                rawJson
            ));
        }

        if (event.getTtl() < MIN_TTL) {
            return Optional.of(new AnomalyEvent(
                event.getEventId(), event.getTimestamp(), event.getEventType(),
                event.getSourceIp(), event.getDestinationIp(),
                AnomalyType.UNUSUAL_TTL, Severity.LOW,
                String.format("Unusual TTL %d to %s", event.getTtl(), event.getDestinationIp()),
                rawJson
            ));
        }

        return Optional.empty();
    }
}
