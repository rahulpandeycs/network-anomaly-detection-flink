package com.network.anomaly.model;

public enum AnomalyType {
    SERVER_ERROR,
    SLOW_RESPONSE,
    RATE_LIMITED,
    HIGH_PACKET_LOSS,
    HIGH_RTT_LATENCY,
    UNUSUAL_TTL,
    HIGH_LATENCY,
    HIGH_JITTER,
    LATENCY_SPIKE,
    INCOMPLETE_PATH,
    SLOW_HOP_LATENCY,
    EXCESSIVE_HOPS
}
