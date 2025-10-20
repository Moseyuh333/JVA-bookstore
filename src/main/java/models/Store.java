package models;

public class Store {
    private Long id;
    private Long ownerUserId;
    private String name;
    private String description;
    private String avatarUrl;
    private String coverUrl;
    private String featuredImagesJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getFeaturedImagesJson() { return featuredImagesJson; }
    public void setFeaturedImagesJson(String featuredImagesJson) { this.featuredImagesJson = featuredImagesJson; }
}