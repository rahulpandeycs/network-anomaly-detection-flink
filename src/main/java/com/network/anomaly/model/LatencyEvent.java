package com.network.anomaly.model;

public class LatencyEvent extends NetworkEvent {

    private int hopCount;
    private double avgLatencyMs;
    private double minLatencyMs;
    private double maxLatencyMs;
    private double jitterMs;

    public LatencyEvent() {}

    public LatencyEvent(String sourceIp, String destinationIp, int hopCount,
                        double avgLatencyMs, double minLatencyMs,
                        double maxLatencyMs, double jitterMs) {
        super(sourceIp, destinationIp);
        this.hopCount = hopCount;
        this.avgLatencyMs = avgLatencyMs;
        this.minLatencyMs = minLatencyMs;
        this.maxLatencyMs = maxLatencyMs;
        this.jitterMs = jitterMs;
    }

    @Override
    public EventType getEventType() { return EventType.LATENCY; }

    public int getHopCount() { return hopCount; }
    public void setHopCount(int hopCount) { this.hopCount = hopCount; }

    public double getAvgLatencyMs() { return avgLatencyMs; }
    public void setAvgLatencyMs(double avgLatencyMs) { this.avgLatencyMs = avgLatencyMs; }

    public double getMinLatencyMs() { return minLatencyMs; }
    public void setMinLatencyMs(double minLatencyMs) { this.minLatencyMs = minLatencyMs; }

    public double getMaxLatencyMs() { return maxLatencyMs; }
    public void setMaxLatencyMs(double maxLatencyMs) { this.maxLatencyMs = maxLatencyMs; }

    public double getJitterMs() { return jitterMs; }
    public void setJitterMs(double jitterMs) { this.jitterMs = jitterMs; }
}
