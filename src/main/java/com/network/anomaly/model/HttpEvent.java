package com.network.anomaly.model;

public class HttpEvent extends NetworkEvent {

    private String httpMethod;
    private String url;
    private int statusCode;
    private long responseTimeMs;
    private int requestSize;

    public HttpEvent() {}

    public HttpEvent(String sourceIp, String destinationIp, String httpMethod,
                     String url, int statusCode, long responseTimeMs, int requestSize) {
        super(sourceIp, destinationIp);
        this.httpMethod = httpMethod;
        this.url = url;
        this.statusCode = statusCode;
        this.responseTimeMs = responseTimeMs;
        this.requestSize = requestSize;
    }

    @Override
    public EventType getEventType() { return EventType.HTTP; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public int getRequestSize() { return requestSize; }
    public void setRequestSize(int requestSize) { this.requestSize = requestSize; }
}
