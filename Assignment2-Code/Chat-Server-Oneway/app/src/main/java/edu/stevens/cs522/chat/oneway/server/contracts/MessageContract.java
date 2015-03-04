package edu.stevens.cs522.chat.oneway.server.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import edu.stevens.cs522.chat.oneway.server.providers.ChatDbProvider;

/**
 * Created by wyf920621 on 2/10/15.
 */
public class MessageContract {
    public static final String ID = "_id";
    public static final String MESSAGE_TEXT = "messageText";
    public static final String SENDER = "sender";
    public static final String PEER_FK = "peer_fk";
    public static final String TABLE_NAME = "Messages";
    public static final String CONTENT = "Message";


    // edu.stevens.cs522.chat.oneway.server/Messages
    public static final Uri CONTENT_URI = ChatDbProvider.CONTENT_URI(ChatDbProvider.AUTHORITY, TABLE_NAME);



    public static Uri CONTENT_URI(String id) {
        return ChatDbProvider.withExtendedPath(CONTENT_URI, id);
    }

    public static String CONTENT_PATH = ChatDbProvider.CONTENT_PATH(CONTENT_URI); // Messages

    public static String CONTENT_PATH_ITEM = ChatDbProvider.CONTENT_PATH(CONTENT_URI("#")); // Messages/#



    public static long getId(Uri uri) {
        return Long.parseLong(uri.getLastPathSegment());
    }

    public static long getId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(ID));
    }

    public static void putId(ContentValues values, long id) {
        values.put(ID, id);
    }

    public static String getMessageText(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(MESSAGE_TEXT));
    }

    public static void putMessageText(ContentValues values, String message) {
        values.put(MESSAGE_TEXT, message);
    }

    public static String getSender(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(SENDER));
    }

    public static void putSender(ContentValues values, String sender) {
        values.put(SENDER, sender);
    }

    public static long getPeerFk(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(PEER_FK));
    }

    public static void putPeerFk(ContentValues values, long id) {
        values.put(PEER_FK, id);
    }
}
