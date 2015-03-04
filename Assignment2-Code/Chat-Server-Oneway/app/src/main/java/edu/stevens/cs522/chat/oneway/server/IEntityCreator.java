package edu.stevens.cs522.chat.oneway.server;

import android.database.Cursor;

/**
 * Created by wyf920621 on 2/23/15.
 */
public interface IEntityCreator<T> {
    public T create (Cursor cursor);
}
