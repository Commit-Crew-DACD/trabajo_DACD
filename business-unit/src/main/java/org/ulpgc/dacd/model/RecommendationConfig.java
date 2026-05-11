package org.ulpgc.dacd.model;

public class RecommendationConfig {
    private final String originAirport;
    private final int outboundMarginHours;
    private final int returnMarginHours;
    private final int defaultEventDurationHours;

    public RecommendationConfig(String originAirport, int outboundMarginHours,
                                int returnMarginHours, int defaultEventDurationHours) {
        this.originAirport = originAirport;
        this.outboundMarginHours = outboundMarginHours;
        this.returnMarginHours = returnMarginHours;
        this.defaultEventDurationHours = defaultEventDurationHours;
    }

    public String getOriginAirport() { return originAirport; }
    public int getOutboundMarginHours() { return outboundMarginHours; }
    public int getReturnMarginHours() { return returnMarginHours; }
    public int getDefaultEventDurationHours() { return defaultEventDurationHours; }
}
