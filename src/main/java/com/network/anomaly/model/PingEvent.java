package com.network.anomaly.model;

public class PingEvent extends NetworkEvent {

    private double packetLossPercent;
    private double roundTripTimeMs;
    private int ttl;
    private int packetSize;

    public PingEvent() {}

    public PingEvent(String sourceIp, String destinationIp, double packetLossPercent,
                     double roundTripTimeMs, int ttl, int packetSize) {
        super(sourceIp, destinationIp);
        this.packetLossPercent = packetLossPercent;
        this.roundTripTimeMs = roundTripTimeMs;
        this.ttl = ttl;
        this.packetSize = packetSize;
    }

    @Override
    public EventType getEventType() { return EventType.PING; }

    public double getPacketLossPercent() { return packetLossPercent; }
    public void setPacketLossPercent(double packetLossPercent) { this.packetLossPercent = packetLossPercent; }

    public double getRoundTripTimeMs() { return roundTripTimeMs; }
    public void setRoundTripTimeMs(double roundTripTimeMs) { this.roundTripTimeMs = roundTripTimeMs; }

    public int getTtl() { return ttl; }
    public void setTtl(int ttl) { this.ttl = ttl; }

    public int getPacketSize() { return packetSize; }
    public void setPacketSize(int packetSize) { this.packetSize = packetSize; }
}
