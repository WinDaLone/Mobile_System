package edu.stevens.cs522.bookstore.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import edu.stevens.cs522.bookstore.providers.BookProvider;

/**
 * Created by wyf920621 on 2/8/15.
 */
public class AuthorContract {
    public static final String TABLE_NAME = "Authors";
    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String BOOK_FOREIGN_KEY = "book_fk";
    public static final String CONTENT = "Author"; // content
    public static final Uri CONTENT_URI = BookProvider.CONTENT_URI(BookProvider.AUTHORITY, TABLE_NAME);
    public static String CONTENT_PATH = BookProvider.CONTENT_PATH(CONTENT_URI);
    public static String CONTENT_PATH_ITEM = BookProvider.CONTENT_PATH(CONTENT_URI("#"));
    public static final String contentType = BookProvider.contentType(CONTENT);
    public static final String contentItemType = BookProvider.contentItemType(CONTENT);

    // CONTENT_URI(id)
    public static Uri CONTENT_URI (String id) {
        return BookProvider.withExtendedPath(CONTENT_URI, id);
    }

    public static long getId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(ID));
    }

    public static long getId(Uri uri) {
        return Long.parseLong(uri.getLastPathSegment());
    }


    public static void putId(ContentValues values, long id) {
        values.put(ID, id);
    }

    public static String getName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(NAME));
    }

    public static void putName(ContentValues values, String name) {
        values.put(NAME, name);
    }

    public static long getBookForeignKey(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(BOOK_FOREIGN_KEY));
    }

    public static void putBookForeignKey(ContentValues values, long book_fk) {
        values.put(BOOK_FOREIGN_KEY, book_fk);
    }
}
