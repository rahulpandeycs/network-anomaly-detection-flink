package com.network.anomaly.anomaly;

import com.network.anomaly.model.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AnomalyDetectionProcessFunction
    extends ProcessFunction<NetworkEvent, AnomalyEvent> {

    private transient Map<EventType, AnomalyDetector<?>> detectors;

    @Override
    public void open(Configuration parameters) {
        detectors = new HashMap<>();
        detectors.put(EventType.HTTP, new HttpAnomalyDetector());
        detectors.put(EventType.PING, new PingAnomalyDetector());
        detectors.put(EventType.LATENCY, new LatencyAnomalyDetector());
        detectors.put(EventType.PATH_TRACE, new PathTraceAnomalyDetector());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void processElement(NetworkEvent event, Context ctx, Collector<AnomalyEvent> out) {
        AnomalyDetector<NetworkEvent> detector =
            (AnomalyDetector<NetworkEvent>) detectors.get(event.getEventType());

        if (detector != null) {
            Optional<AnomalyEvent> result = detector.detect(event);
            result.ifPresent(out::collect);
        }
    }
}
