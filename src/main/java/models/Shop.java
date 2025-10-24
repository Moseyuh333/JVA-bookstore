package models;

public class Shop {
    private int id;
    private int ownerId;
    private String name;
    private String address;
    private String description;
    private double commissionRate; // DECIMAL(4, 2)
    // Các trường created_at và updated_at (Tùy chọn)

    // Constructor (Tùy chọn)
    public Shop() {}
    
    // Constructor đầy đủ
    public Shop(int id, int ownerId, String name, String address, String description, double commissionRate) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.address = address;
        this.description = description;
        this.commissionRate = commissionRate;
    }

    // Getters và Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getCommissionRate() { return commissionRate; }
    public void setCommissionRate(double commissionRate) { this.commissionRate = commissionRate; }
    
    // Bạn có thể thêm các getters/setters cho created_at và updated_at nếu cần
}