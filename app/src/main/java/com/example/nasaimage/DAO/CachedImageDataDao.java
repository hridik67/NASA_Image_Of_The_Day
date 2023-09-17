package com.example.nasaimage.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import com.example.nasaimage.Models.CachedImageData;

@Dao
public interface CachedImageDataDao {
    @Insert
    void insert(CachedImageData imageData);

    @Query("SELECT * FROM cached_images WHERE imageUrl = :imageUrl")
    CachedImageData getImageData(String imageUrl);

    @Delete
    void delete(CachedImageData imageData);
}
