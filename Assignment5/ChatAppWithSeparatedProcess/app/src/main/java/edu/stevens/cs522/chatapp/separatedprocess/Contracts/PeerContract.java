package edu.stevens.cs522.chatapp.separatedprocess.Contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

import edu.stevens.cs522.chatapp.separatedprocess.Providers.ChatDbProvider;

/**
 * Created by wyf920621 on 3/8/15.
 */
public class PeerContract {
    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String ADDRESS = "address";
    public static final String PORT = "port";
    public static final String TABLE_NAME = "Peers";
    public static final String CONTENT = "Peer";
    public static final Uri CONTENT_URI = ChatDbProvider.CONTENT_URI(ChatDbProvider.AUTHORITY, TABLE_NAME);


    public static Uri CONTENT_URI(String id) {
        return ChatDbProvider.withExtendedPath(CONTENT_URI, id);
    }

    public static String CONTENT_PATH = ChatDbProvider.CONTENT_PATH(CONTENT_URI); // Peers

    public static String CONTENT_PATH_ITEM = ChatDbProvider.CONTENT_PATH(CONTENT_URI("#")); // Peers/#


    public static long getId(Uri uri) {
        return Long.parseLong(uri.getLastPathSegment());
    }

    public static long getId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(ID));
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

    public static InetAddress getAddress(Cursor cursor) {
        String address = cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS));
        try {
            return InetAddress.getByName(address);
        }
        catch (UnknownHostException e) {
            Log.e("PeerContract", e.getMessage());
            return null;
        }
    }

    public static void putAddress(ContentValues values, InetAddress address) {
        values.put(ADDRESS, address.getHostAddress());
    }

    public static int getPort(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(PORT));
    }

    public static void putPort(ContentValues values, int port) {
        values.put(PORT, port);
    }
}
