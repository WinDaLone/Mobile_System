package edu.stevens.cs522.chatapp.singleprocess;

import android.database.Cursor;

/**
 * Created by wyf920621 on 3/3/15.
 */
public interface IEntityCreator<T> {
    public T create (Cursor cursor);
}
