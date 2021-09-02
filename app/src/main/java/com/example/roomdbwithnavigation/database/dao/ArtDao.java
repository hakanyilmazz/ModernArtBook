package com.example.roomdbwithnavigation.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.roomdbwithnavigation.model.Art;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface ArtDao {
    @Query("SELECT * FROM Art")
    Flowable<List<Art>> all();

    @Query("SELECT * FROM Art WHERE id = :id")
    Single<Art> getArtById(int id);

    @Insert
    Completable insert(Art art);
}
