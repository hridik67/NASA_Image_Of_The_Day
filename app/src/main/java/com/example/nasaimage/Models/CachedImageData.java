package com.example.nasaimage.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cached_images")
public class CachedImageData {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String image;
    private String imageUrl;
    private String title;
    private String date;
    private String description;

    // Getters and setters


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
