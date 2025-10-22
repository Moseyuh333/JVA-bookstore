package models;

import com.google.gson.JsonObject;

public class Store {
    private int id;
    private int ownerId;
    private String name;
    private String address;
    private String description;

    public Store() {}

    public Store(int id, int ownerId, String name, String address, String description) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.address = address;
        this.description = description;
    }

    // getters & setters
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

    // Trả về JsonObject (Gson)
    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("ownerId", ownerId);
        obj.addProperty("name", name != null ? name : "");
        obj.addProperty("address", address != null ? address : "");
        obj.addProperty("description", description != null ? description : "");
        return obj;
    }

    // hoặc trả về chuỗi JSON nếu muốn
    public String toJsonString() {
        return toJsonObject().toString();
    }
}
