package com.user.exoplayer.player.data.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Video.class, Subtitle.class} , version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract VideoDao urlDao();

}
