package com.network.anomaly.model;

import java.util.List;

public class PathTraceEvent extends NetworkEvent {

    private List<String> hops;
    private int totalHops;
    private double completionPercent;
    private double avgHopLatencyMs;

    public PathTraceEvent() {}

    public PathTraceEvent(String sourceIp, String destinationIp, List<String> hops,
                          int totalHops, double completionPercent, double avgHopLatencyMs) {
        super(sourceIp, destinationIp);
        this.hops = hops;
        this.totalHops = totalHops;
        this.completionPercent = completionPercent;
        this.avgHopLatencyMs = avgHopLatencyMs;
    }

    @Override
    public EventType getEventType() { return EventType.PATH_TRACE; }

    public List<String> getHops() { return hops; }
    public void setHops(List<String> hops) { this.hops = hops; }

    public int getTotalHops() { return totalHops; }
    public void setTotalHops(int totalHops) { this.totalHops = totalHops; }

    public double getCompletionPercent() { return completionPercent; }
    public void setCompletionPercent(double completionPercent) { this.completionPercent = completionPercent; }

    public double getAvgHopLatencyMs() { return avgHopLatencyMs; }
    public void setAvgHopLatencyMs(double avgHopLatencyMs) { this.avgHopLatencyMs = avgHopLatencyMs; }
}
