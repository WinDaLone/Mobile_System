package edu.stevens.cs522.simplecloudchatapp.Callbacks;

import android.database.Cursor;

/**
 * Created by wyf920621 on 3/12/15.
 */
public interface IEntityCreator<T> {
    public T create(Cursor cursor);
}
