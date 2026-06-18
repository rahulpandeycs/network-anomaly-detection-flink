package com.network.anomaly.anomaly;

import com.network.anomaly.model.AnomalyEvent;
import com.network.anomaly.model.NetworkEvent;

import java.util.Optional;

public interface AnomalyDetector<T extends NetworkEvent> {

    Optional<AnomalyEvent> detect(T event);
}
