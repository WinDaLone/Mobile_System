package edu.stevens.cs522.simplecloudchatapp.Contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.sql.Timestamp;

import edu.stevens.cs522.simplecloudchatapp.Providers.MessageDbProvider;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class MessageContract {
    public static final String MESSAGE_ID = "_id";
    public static final String CHATROOM_FK = "chatroom_fk"; // CHATROOM_FK
    public static final String MESSAGE_TEXT = "text";
    public static final String TIMESTAMP = "timestamp";
    public static final String SEQNUM = "seqnum";
    public static final String SENDER_ID = "senderID"; // SENDER_FK

    public static final String TABLE_NAME = "Messages";
    public static final String CONTENT = "Message";

    public static final Uri CONTENT_URI = MessageDbProvider.CONTENT_URI(MessageDbProvider.AUTHORITY, TABLE_NAME);

    public static Uri CONTENT_URI(String id) {
        return MessageDbProvider.withExtendedPath(CONTENT_URI, id);
    }

    public static String CONTENT_PATH = MessageDbProvider.CONTENT_PATH(CONTENT_URI); // Messages
    public static String CONTENT_ITEM_PATH = MessageDbProvider.CONTENT_PATH(CONTENT_URI("#")); // Messages/#

    public static long getMessageId(Uri uri) {
        return Long.parseLong(uri.getLastPathSegment());
    }

    public static long getMessageId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(MESSAGE_ID));
    }

    public static void setMessageId(ContentValues values, long id) {
        values.put(MESSAGE_ID, id);
    }

    public static long getChatroomId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(CHATROOM_FK));
    }

    public static void setChatroomId(ContentValues values, long chatroom_fk) {
        values.put(CHATROOM_FK, chatroom_fk);
    }

    public static String getMessageText(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(MESSAGE_TEXT));
    }

    public static void setMessageText(ContentValues values, String message) {
        values.put(MESSAGE_TEXT, message);
    }

    public static Timestamp getTimestamp(Cursor cursor) {
        long timestampValue = cursor.getLong(cursor.getColumnIndexOrThrow(TIMESTAMP));
        return new Timestamp(timestampValue);
    }

    public static void setTimestamp(ContentValues values, Timestamp timestamp) {
        values.put(TIMESTAMP, timestamp.getTime());
    }

    public static long getSeqnum(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(SEQNUM));
    }

    public static void setSeqnum(ContentValues values, long seqnum) {
        values.put(SEQNUM, seqnum);
    }

    public static long getSenderId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(SENDER_ID));
    }

    public static void setSenderId(ContentValues values, long id) {
        values.put(SENDER_ID, id);
    }
}
