package edu.stevens.cs522.bookstore;

import android.database.Cursor;

/**
 * Created by wyf920621 on 2/22/15.
 */
public interface IEntityCreator<T> {
    public T create(Cursor cursor);
}
