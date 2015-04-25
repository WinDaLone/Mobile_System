package edu.stevens.cs522.simplecloudchatapp.Contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import edu.stevens.cs522.simplecloudchatapp.Providers.MessageDbProvider;

/**
 * Created by wyf920621 on 4/20/15.
 */
public class ChatroomContract {
    public static final String ID = "_id";
    public static final String NAME = "chatroom";

    public static final String TABLE_NAME = "Chatrooms";
    public static final String CONTENT = "Chatroom";

    public static final Uri CONTENT_URI = MessageDbProvider.CONTENT_URI(MessageDbProvider.AUTHORITY, TABLE_NAME);

    public static Uri CONTENT_URI(String id) {
        return MessageDbProvider.withExtendedPath(CONTENT_URI, id);
    }

    public static String CONTENT_PATH = MessageDbProvider.CONTENT_PATH(CONTENT_URI); // Messages
    public static String CONTENT_ITEM_PATH = MessageDbProvider.CONTENT_PATH(CONTENT_URI("#")); // Messages/#

    public static long getId(Uri uri) {
        return Long.parseLong(uri.getLastPathSegment());
    }
    public static long getId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(ID));
    }

    public static void setId(ContentValues values, long id) {
        values.put(ID, id);
    }

    public static String getName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(NAME));
    }

    public static void setName(ContentValues values, String name) {
        values.put(NAME, name);
    }
}
