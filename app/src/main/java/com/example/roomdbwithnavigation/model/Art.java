package com.example.roomdbwithnavigation.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Art {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "art_name")
    public String artName;

    @ColumnInfo(name = "painter_name")
    public String painterName;

    @ColumnInfo(name = "year")
    public int year;

    @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB)
    public byte[] image;
}
