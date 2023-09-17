package com.example.nasaimage.DAO;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.nasaimage.Models.CachedImageData;

@Database(entities = {CachedImageData.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CachedImageDataDao cachedImageDataDao();
}

