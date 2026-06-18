package com.network.anomaly.model;

import java.io.Serializable;

public class AnomalyEvent implements Serializable {

    private String eventId;
    private long timestamp;
    private EventType originalEventType;
    private String sourceIp;
    private String destinationIp;
    private AnomalyType anomalyType;
    private Severity severity;
    private String description;
    private String rawEventJson;

    public AnomalyEvent() {}

    public AnomalyEvent(String eventId, long timestamp, EventType originalEventType,
                        String sourceIp, String destinationIp, AnomalyType anomalyType,
                        Severity severity, String description, String rawEventJson) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.originalEventType = originalEventType;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.anomalyType = anomalyType;
        this.severity = severity;
        this.description = description;
        this.rawEventJson = rawEventJson;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public EventType getOriginalEventType() { return originalEventType; }
    public void setOriginalEventType(EventType originalEventType) { this.originalEventType = originalEventType; }

    public String getSourceIp() { return sourceIp; }
    public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }

    public String getDestinationIp() { return destinationIp; }
    public void setDestinationIp(String destinationIp) { this.destinationIp = destinationIp; }

    public AnomalyType getAnomalyType() { return anomalyType; }
    public void setAnomalyType(AnomalyType anomalyType) { this.anomalyType = anomalyType; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRawEventJson() { return rawEventJson; }
    public void setRawEventJson(String rawEventJson) { this.rawEventJson = rawEventJson; }

    public String toCsvHeader() {
        return "eventId,timestamp,originalEventType,sourceIp,destinationIp,anomalyType,severity,description";
    }

    public String toCsvRow() {
        return String.format("%s,%d,%s,%s,%s,%s,%s,\"%s\"",
            eventId, timestamp, originalEventType, sourceIp, destinationIp,
            anomalyType, severity,
            description != null ? description.replace("\"", "\"\"") : "");
    }
}
