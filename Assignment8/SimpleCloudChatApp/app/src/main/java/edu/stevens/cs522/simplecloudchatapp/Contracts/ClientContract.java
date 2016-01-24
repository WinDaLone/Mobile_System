package edu.stevens.cs522.simplecloudchatapp.Contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import edu.stevens.cs522.simplecloudchatapp.Providers.MessageDbProvider;

/**
 * Created by wyf920621 on 4/11/15.
 */
public class ClientContract {
    public static final String CLIENT_ID = "_id";
    public static final String NAME = "name";
    public static final String UUID = "uuid";

    public static final String TABLE_NAME = "Clients";
    public static final String CONTENT = "Client";

    public static final Uri CONTENT_URI = MessageDbProvider.CONTENT_URI(MessageDbProvider.AUTHORITY, TABLE_NAME);

    public static Uri CONTENT_URI(String id) {
        return MessageDbProvider.withExtendedPath(CONTENT_URI, id);
    }

    public static String CONTENT_PATH = MessageDbProvider.CONTENT_PATH(CONTENT_URI); // Messages
    public static String CONTENT_ITEM_PATH = MessageDbProvider.CONTENT_PATH(CONTENT_URI("#")); // Messages/#

    public static long getClientId(Uri uri) {
        return Long.parseLong(uri.getLastPathSegment());
    }

    public static long getClientId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(CLIENT_ID));
    }

    public static void setClientId(ContentValues values, long id) {
        values.put(CLIENT_ID, id);
    }

    public static String getName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(NAME));
    }

    public static void setName(ContentValues values, String name) {
        values.put(NAME, name);
    }

    public static java.util.UUID getUUID (Cursor cursor) {
        String uuid = cursor.getString(cursor.getColumnIndexOrThrow(UUID));
        if (uuid.equals("")) {
            return null;
        } else {
            return java.util.UUID.fromString(uuid);
        }
    }

    public static void setUuid(ContentValues values, java.util.UUID uuid) {
        if (uuid != null) {
            values.put(UUID, uuid.toString());
        } else {
            values.put(UUID, "");
        }
    }
}
