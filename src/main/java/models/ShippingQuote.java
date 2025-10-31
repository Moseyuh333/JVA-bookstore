package models;

import java.math.BigDecimal;

/**
 * Represents the resolved shipping method and fee for a specific address.
 */
public class ShippingQuote {
    public enum MatchLevel {
        NONE(0),
        GLOBAL(1),
        REGION(2),
        PROVINCE(3),
        CITY(4),
        DISTRICT(5);

        private final int priority;

        MatchLevel(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    private Long shipperId;
    private String shipperName;
    private BigDecimal fee;
    private String estimatedTime;
    private String serviceArea;
    private MatchLevel matchLevel = MatchLevel.NONE;

    public Long getShipperId() {
        return shipperId;
    }

    public void setShipperId(Long shipperId) {
        this.shipperId = shipperId;
    }

    public String getShipperName() {
        return shipperName;
    }

    public void setShipperName(String shipperName) {
        this.shipperName = shipperName;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public MatchLevel getMatchLevel() {
        return matchLevel;
    }

    public void setMatchLevel(MatchLevel matchLevel) {
        this.matchLevel = matchLevel;
    }
}

