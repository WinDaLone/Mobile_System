package edu.stevens.cs522.chatapp.singleprocess.Providers;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;

import edu.stevens.cs522.chatapp.singleprocess.Contracts.MessageContract;
import edu.stevens.cs522.chatapp.singleprocess.Contracts.PeerContract;
import edu.stevens.cs522.chatapp.singleprocess.Databases.DatabaseHelper;

public class ChatDbProvider extends ContentProvider {
    public static final String AUTHORITY = "edu.stevens.cs522.chatapp.singleprocess";

    public static String CONTENT_PATH(Uri uri) {
        return uri.getPath().substring(1); // Trim leading "/"
    }


    public static Uri CONTENT_URI(String authority, String path) {
        return new Uri.Builder().scheme("content").authority(authority).path(path).build();
    }

    public static Uri withExtendedPath(Uri uri, String... path){
        Uri.Builder builder = uri.buildUpon();
        for (String p : path)
            builder.appendPath(p);
        return builder.build();
    }

    public static String contentType(String content) {
        return "vnd.android.cursor/vnd." + AUTHORITY + "." + content + "s";
    }

    public static String contentItemType(String content) {
        return "vnd.android.cursor.item/vnd." + AUTHORITY + "." + content;
    }

    private DatabaseHelper databaseHelper;
    private static final String DATABASE_NAME = "chat.db";
    private static final int DATABASE_VERSION = 1;


    private static final int MESSAGE_ALL_ROWS = 1;
    private static final int MESSAGE_SINGLE_ROW = 2;
    private static final int PEER_ALL_ROWS = 3;
    private static final int PEER_SINGLE_ROW = 4;
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, MessageContract.CONTENT_PATH, MESSAGE_ALL_ROWS);
        uriMatcher.addURI(AUTHORITY, MessageContract.CONTENT_PATH_ITEM, MESSAGE_SINGLE_ROW);
        uriMatcher.addURI(AUTHORITY, PeerContract.CONTENT_PATH, PEER_ALL_ROWS);
        uriMatcher.addURI(AUTHORITY, PeerContract.CONTENT_PATH_ITEM, PEER_SINGLE_ROW);
    }


    public ChatDbProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        databaseHelper.getWritableDatabase().execSQL("PRAGMA foreign_keys = ON");
        int rowId;
        switch (uriMatcher.match(uri)) {
            case MESSAGE_ALL_ROWS:
                rowId = database.delete(MessageContract.TABLE_NAME, null, null);
                getContext().getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
                return rowId;
            case MESSAGE_SINGLE_ROW:
                selection = MessageContract.ID + "=?";
                selectionArgs = new String[] {String.valueOf(MessageContract.getId(uri))};
                rowId = database.delete(MessageContract.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
                return rowId;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case MESSAGE_ALL_ROWS:
                return contentType(MessageContract.CONTENT);
            case MESSAGE_SINGLE_ROW:
                return contentItemType(MessageContract.CONTENT);
            case PEER_ALL_ROWS:
                return contentType(PeerContract.CONTENT);
            case PEER_SINGLE_ROW:
                return contentItemType(PeerContract.CONTENT);
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        databaseHelper.getWritableDatabase().execSQL("PRAGMA foreign_keys = ON");
        long rowId;
        switch (uriMatcher.match(uri)) {
            case MESSAGE_ALL_ROWS:
                rowId = database.insert(MessageContract.TABLE_NAME, null, values);
                if (rowId > 0) {
                    Uri instanceUri = MessageContract.CONTENT_URI(String.valueOf(rowId));
                    ContentResolver contentResolver = getContext().getContentResolver();
                    contentResolver.notifyChange(uri, null);
                    return instanceUri;
                }
                throw new SQLException("Insertion Failed");
            case PEER_ALL_ROWS:
                rowId = database.insert(PeerContract.TABLE_NAME, null, values);
                if (rowId > 0) {
                    Uri instanceUri = PeerContract.CONTENT_URI(String.valueOf(rowId));
                    ContentResolver contentResolver = getContext().getContentResolver();
                    contentResolver.notifyChange(MessageContract.CONTENT_URI, null);
                    return instanceUri;
                }
                throw new SQLException("Insertion Failed");
            default:
                throw new SQLException("Insertion Failed");
        }
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        databaseHelper.getWritableDatabase().execSQL("PRAGMA foreign_keys = ON");
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
        database.execSQL("PRAGMA foreign_keys = ON");
        Cursor cursor;
        switch (uriMatcher.match(uri)) {
            case MESSAGE_ALL_ROWS:
                String query = "SELECT " + MessageContract.TABLE_NAME + "." + MessageContract.ID + " AS " + MessageContract.ID + ", " + PeerContract.TABLE_NAME + "." + PeerContract.NAME + " AS " + PeerContract.NAME + ", " +
                        MessageContract.TABLE_NAME + "." + MessageContract.MESSAGE_TEXT + " AS " + MessageContract.MESSAGE_TEXT + " FROM " + PeerContract.TABLE_NAME + " LEFT JOIN " + MessageContract.TABLE_NAME + " ON " + PeerContract.TABLE_NAME + "." + PeerContract.ID + "=" + MessageContract.TABLE_NAME +
                        "." + MessageContract.PEER_FK + ";";
                cursor = database.rawQuery(query, null);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case MESSAGE_SINGLE_ROW:
                return database.query(MessageContract.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            case PEER_ALL_ROWS:
                projection = new String[] {PeerContract.ID, PeerContract.NAME, PeerContract.ADDRESS, PeerContract.PORT};
                cursor = database.query(PeerContract.TABLE_NAME, projection, null, null, null, null, null, null);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case PEER_SINGLE_ROW:
                if (PeerContract.getId(uri) == 0) {
                    return database.query(PeerContract.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
                }
                else {
                    long rowId = PeerContract.getId(uri);
                    selection = MessageContract.PEER_FK + "=?";
                    selectionArgs = new String[] {String.valueOf(rowId)};
                    projection = new String[] {MessageContract.ID, MessageContract.MESSAGE_TEXT, MessageContract.SENDER};
                    return database.query(MessageContract.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
                }
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.execSQL("PRAGMA foreign_keys = ON");
        int rowId;
        switch (uriMatcher.match(uri)) {
            case MESSAGE_ALL_ROWS:
                rowId = database.update(MessageContract.TABLE_NAME, values, null, null);
                getContext().getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
                return rowId;
            case MESSAGE_SINGLE_ROW:
                selection = MessageContract.ID + "=?";
                selectionArgs = new String[] {String.valueOf(MessageContract.getId(uri))};
                rowId = database.update(MessageContract.TABLE_NAME, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(MessageContract.CONTENT_URI, null);
                return rowId;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}
