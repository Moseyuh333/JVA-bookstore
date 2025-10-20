package models;

import java.sql.Timestamp;

public class Shipment {
    private long id;
    private Long orderId;
    private String shipperUserId;
    private String status;
    private double codAmount;
    private boolean codCollected;
    private Timestamp pickupAt;
    private Timestamp deliveredAt;
    private String proofImageUrl;
    private Timestamp lastUpdateAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getShipperUserId() {
        return shipperUserId;
    }

    public void setShipperUserId(String shipperUserId) {
        this.shipperUserId = shipperUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCodAmount() {
        return codAmount;
    }

    public void setCodAmount(double codAmount) {
        this.codAmount = codAmount;
    }

    public boolean isCodCollected() {
        return codCollected;
    }

    public void setCodCollected(boolean codCollected) {
        this.codCollected = codCollected;
    }

    public Timestamp getPickupAt() {
        return pickupAt;
    }

    public void setPickupAt(Timestamp pickupAt) {
        this.pickupAt = pickupAt;
    }

    public Timestamp getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Timestamp deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getProofImageUrl() {
        return proofImageUrl;
    }

    public void setProofImageUrl(String proofImageUrl) {
        this.proofImageUrl = proofImageUrl;
    }

    public Timestamp getLastUpdateAt() {
        return lastUpdateAt;
    }
    
    public void setLastUpdateAt(Timestamp lastUpdateAt) {
        this.lastUpdateAt = lastUpdateAt;
    }

}
