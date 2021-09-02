package com.example.roomdbwithnavigation.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.roomdbwithnavigation.database.dao.ArtDao;
import com.example.roomdbwithnavigation.model.Art;

@Database(entities = {Art.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "Arts";

    public abstract ArtDao artDao();
}
