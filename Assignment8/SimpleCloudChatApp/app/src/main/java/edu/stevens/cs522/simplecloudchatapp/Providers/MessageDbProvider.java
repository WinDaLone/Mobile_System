package edu.stevens.cs522.simplecloudchatapp.Providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import edu.stevens.cs522.simplecloudchatapp.Contracts.ClientContract;
import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;
import edu.stevens.cs522.simplecloudchatapp.Databases.DatabaseHelper;

public class MessageDbProvider extends ContentProvider {
    public static final String TAG = MessageDbProvider.class.getCanonicalName();
    public static final String AUTHORITY = "edu.stevens.cs522.simplecloudchatapp";

    public static String CONTENT_PATH(Uri uri) {
        return uri.getPath().substring(1);
    }

    public static Uri CONTENT_URI(String authority, String path) {
        return new Uri.Builder().scheme("content").authority(authority).path(path).build();
    }

    public static Uri withExtendedPath(Uri uri, String... path) {
        Uri.Builder builder = uri.buildUpon();
        for (String p : path)
            builder.appendPath(p);
        return builder.build();
    }

    public static String contentType(String content) {
        return "vnd.android.cursor/vnd." + AUTHORITY + "." + content + "s";
    }

    public static String contentItemType(String content) {
        return "vnd,android.cursor.item/vnd." + AUTHORITY + "." + content;
    }

    private DatabaseHelper databaseHelper;
    private static final String DATABASE_NAME = "CloudChat.db";
    private static final int DATABASE_VERSION = 1;

    private static final int MESSAGE_ALL_ROWS = 1;
    private static final int MESSAGE_SINGLE_ROW = 2;
    private static final int CLIENT_ALL_ROWS = 3;
    private static final int CLIENT_SINGLE_ROW = 4;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, MessageContract.CONTENT_PATH, MESSAGE_ALL_ROWS);
        uriMatcher.addURI(AUTHORITY, MessageContract.CONTENT_ITEM_PATH, MESSAGE_SINGLE_ROW);
        uriMatcher.addURI(AUTHORITY, ClientContract.CONTENT_PATH, CLIENT_ALL_ROWS);
        uriMatcher.addURI(AUTHORITY, ClientContract.CONTENT_ITEM_PATH, CLIENT_SINGLE_ROW);
    }

    public MessageDbProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int rowId;
        switch (uriMatcher.match(uri)) {
            case MESSAGE_ALL_ROWS:
                rowId = database.delete(MessageContract.TABLE_NAME, null, null);
                getContext().getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
                return rowId;
            case MESSAGE_SINGLE_ROW:
                selection = MessageContract.MESSAGE_ID + "=?";
                selectionArgs = new String[] {String.valueOf(MessageContract.getMessageId(uri))};
                rowId = database.delete(MessageContract.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
                return rowId;
            case CLIENT_ALL_ROWS:
                rowId = database.delete(ClientContract.TABLE_NAME, null, null);
                getContext().getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
                return rowId;
            case CLIENT_SINGLE_ROW:
                selection = ClientContract.CLIENT_ID + "=?";
                selectionArgs = new String[] {String.valueOf(ClientContract.getClientId(uri))};
                rowId = database.delete(ClientContract.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
                return rowId;
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        // at the given URI.
        switch (uriMatcher.match(uri)) {
            case MESSAGE_ALL_ROWS:
                return contentType(MessageContract.CONTENT);
            case MESSAGE_SINGLE_ROW:
                return contentItemType(MessageContract.CONTENT);
            case CLIENT_ALL_ROWS:
                return contentType(ClientContract.CONTENT);
            case CLIENT_SINGLE_ROW:
                return contentItemType(ClientContract.CONTENT);
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        long rowId;
        switch (uriMatcher.match(uri)) {
            case MESSAGE_ALL_ROWS:
                rowId = database.insert(MessageContract.TABLE_NAME, null, values);
                if (rowId > 0) {
                    Uri instanceUri = MessageContract.CONTENT_URI(String.valueOf(rowId));
                    getContext().getContentResolver().notifyChange(uri, null);
                    return instanceUri;
                }
                throw new SQLException("Insertion failed");
            case CLIENT_ALL_ROWS:
                rowId = database.insert(ClientContract.TABLE_NAME, null, values);
                if (rowId > 0) {
                    Uri instanceUri = MessageContract.CONTENT_URI(String.valueOf(rowId));
                    getContext().getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
                    return instanceUri;
                }
                throw new SQLException("Insertion failed");
            default:
                throw new SQLException("Insertion failed");
        }
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database;
        try {
            database = databaseHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            database = databaseHelper.getReadableDatabase();
        }
        Cursor cursor;
        String query;
        switch (uriMatcher.match(uri)) {
            case MESSAGE_ALL_ROWS: // return all messages
                if (projection == null) {
                    Log.i(TAG, "Query all messages");
                    query = "SELECT " + MessageContract.TABLE_NAME + "." + MessageContract.MESSAGE_ID + " AS " + MessageContract.MESSAGE_ID + ", " +
                            MessageContract.TABLE_NAME + "." + MessageContract.CHATROOM + " AS " + MessageContract.CHATROOM + ", " +
                            MessageContract.TABLE_NAME + "." + MessageContract.MESSAGE_TEXT + " AS " + MessageContract.MESSAGE_TEXT + ", " +
                            MessageContract.TABLE_NAME + "." + MessageContract.TIMESTAMP + " AS " + MessageContract.TIMESTAMP + ", " +
                            MessageContract.TABLE_NAME + "." + MessageContract.SEQNUM + " AS " + MessageContract.SEQNUM + ", " +
                            ClientContract.TABLE_NAME + "." + ClientContract.NAME + " AS " + ClientContract.NAME + ", " +
                            MessageContract.TABLE_NAME + "." + MessageContract.SENDER_ID + " AS " + MessageContract.SENDER_ID +
                            " FROM " + MessageContract.TABLE_NAME + " LEFT OUTER JOIN " + ClientContract.TABLE_NAME + " ON " +
                            ClientContract.TABLE_NAME + "." + ClientContract.CLIENT_ID + " = " + MessageContract.TABLE_NAME + "." + MessageContract.SENDER_ID +
                            " ORDER BY " + MessageContract.TIMESTAMP + ";"; // Modified
                    cursor = database.rawQuery(query, null);
                    cursor.setNotificationUri(getContext().getContentResolver(), uri);
                    return cursor;
                } else {
                    Log.i(TAG, "Query a particular message");
                    return database.query(MessageContract.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                }
            case MESSAGE_SINGLE_ROW: // return a message
                Log.i(TAG, "Query a message by _id");
                query = "SELECT " + MessageContract.TABLE_NAME + "." + MessageContract.MESSAGE_ID + " AS " + MessageContract.MESSAGE_ID +  ", " +
                        MessageContract.TABLE_NAME + "." + MessageContract.CHATROOM + " AS " + MessageContract.CHATROOM + ", " +
                        MessageContract.TABLE_NAME + "." + MessageContract.MESSAGE_TEXT + " AS " + MessageContract.MESSAGE_TEXT + ", " +
                        MessageContract.TABLE_NAME + "." + MessageContract.TIMESTAMP + " AS " + MessageContract.TIMESTAMP + ", " +
                        MessageContract.TABLE_NAME + "." + MessageContract.SEQNUM + " AS " + MessageContract.SEQNUM + ", " +
                        ClientContract.TABLE_NAME + "." + ClientContract.NAME + " AS " + ClientContract.NAME +  ", " +
                        MessageContract.TABLE_NAME + "." + MessageContract.SENDER_ID + " AS " + MessageContract.SENDER_ID +
                        " FROM " + MessageContract.TABLE_NAME + " LEFT OUTER JOIN " + ClientContract.TABLE_NAME + " ON " +
                        ClientContract.TABLE_NAME + "." + ClientContract.CLIENT_ID + " = " + MessageContract.TABLE_NAME + "." + MessageContract.SENDER_ID +
                        " WHERE " + MessageContract.TABLE_NAME + "." + MessageContract.MESSAGE_ID + " = ?" +
                        " ORDER BY " + MessageContract.TIMESTAMP + ";";
                return database.rawQuery(query, new String[] {String.valueOf(MessageContract.getMessageId(uri))});
            case CLIENT_ALL_ROWS: // return all clients
                Log.i(TAG, "Query all clients");
                projection = new String[] {ClientContract.CLIENT_ID, ClientContract.NAME, ClientContract.UUID};
                cursor = database.query(ClientContract.TABLE_NAME, projection, null, null, null, null, null);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case CLIENT_SINGLE_ROW: // return all messages from a client
                if (projection == null) {
                    Log.i(TAG, "Query all messages from a client");
                    query = "SELECT " + MessageContract.TABLE_NAME + "." + MessageContract.MESSAGE_ID + " AS " + MessageContract.MESSAGE_ID + ", " +
                            MessageContract.TABLE_NAME + "." + MessageContract.CHATROOM + " AS " + MessageContract.CHATROOM + ", " +
                            MessageContract.TABLE_NAME + "." + MessageContract.MESSAGE_TEXT + " AS " + MessageContract.MESSAGE_TEXT + ", " +
                            MessageContract.TABLE_NAME + "." + MessageContract.TIMESTAMP + " AS " + MessageContract.TIMESTAMP + ", " +
                            MessageContract.TABLE_NAME + "." + MessageContract.SEQNUM + " AS " + MessageContract.SEQNUM + ", " +
                            ClientContract.TABLE_NAME + "." + ClientContract.NAME + " AS " + ClientContract.NAME +  ", " +
                            MessageContract.TABLE_NAME + "." + MessageContract.SENDER_ID + " AS " + MessageContract.SENDER_ID +
                            " FROM " + MessageContract.TABLE_NAME + " LEFT OUTER JOIN " + ClientContract.TABLE_NAME + " ON " +
                            ClientContract.TABLE_NAME + "." + ClientContract.CLIENT_ID + " = " + MessageContract.TABLE_NAME + "." + MessageContract.SENDER_ID +
                            " WHERE " + ClientContract.TABLE_NAME + "." + ClientContract.CLIENT_ID + " = ?" +
                            " ORDER BY " + MessageContract.TIMESTAMP + ";";
                    cursor = database.rawQuery(query, new String[]{String.valueOf(ClientContract.getClientId(uri))});
                    return cursor;
                } else { // return one client
                    Log.i(TAG, "Query a client");
                    projection = new String[] {ClientContract.CLIENT_ID, ClientContract.NAME, ClientContract.UUID};
                    selection = ClientContract.CLIENT_ID + "=?";
                    selectionArgs = new String[] {String.valueOf(ClientContract.getClientId(uri))};
                    return database.query(ClientContract.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
                }
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int rowId;
        switch (uriMatcher.match(uri)) {
            case MESSAGE_ALL_ROWS:
                rowId = database.update(MessageContract.TABLE_NAME, values, null, null);
                getContext().getContentResolver().notifyChange(uri, null);
                return rowId;
            case MESSAGE_SINGLE_ROW:
                selection = MessageContract.MESSAGE_ID + "=?";
                selectionArgs = new String[] {String.valueOf(MessageContract.getMessageId(uri))};
                rowId = database.update(MessageContract.TABLE_NAME, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
                return rowId;
            case CLIENT_ALL_ROWS:
                rowId = database.update(ClientContract.TABLE_NAME, values, null, null);
                getContext().getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
                return rowId;
            case CLIENT_SINGLE_ROW:
                selection = ClientContract.CLIENT_ID + "=?";
                selectionArgs = new String[] {String.valueOf(MessageContract.getMessageId(uri))};
                rowId = database.update(MessageContract.TABLE_NAME, values, selection, selectionArgs);
                return rowId;
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }
    }
}