package com.network.anomaly.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.UUID;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "eventType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = HttpEvent.class, name = "HTTP"),
    @JsonSubTypes.Type(value = PingEvent.class, name = "PING"),
    @JsonSubTypes.Type(value = LatencyEvent.class, name = "LATENCY"),
    @JsonSubTypes.Type(value = PathTraceEvent.class, name = "PATH_TRACE")
})
public abstract class NetworkEvent implements Serializable {

    private String eventId;
    private long timestamp;
    private String sourceIp;
    private String destinationIp;
    private boolean labeledAnomaly;
    private String labeledAnomalyType;

    public NetworkEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public NetworkEvent(String sourceIp, String destinationIp) {
        this();
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
    }

    public abstract EventType getEventType();

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getSourceIp() { return sourceIp; }
    public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }

    public String getDestinationIp() { return destinationIp; }
    public void setDestinationIp(String destinationIp) { this.destinationIp = destinationIp; }

    public boolean isLabeledAnomaly() { return labeledAnomaly; }
    public void setLabeledAnomaly(boolean labeledAnomaly) { this.labeledAnomaly = labeledAnomaly; }

    public String getLabeledAnomalyType() { return labeledAnomalyType; }
    public void setLabeledAnomalyType(String labeledAnomalyType) { this.labeledAnomalyType = labeledAnomalyType; }
}
