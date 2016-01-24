package edu.stevens.cs522.simplecloudchatapp.Databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import edu.stevens.cs522.simplecloudchatapp.Contracts.ClientContract;
import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String MESSAGE_TABLE_CREATE =
            "CREATE TABLE " + MessageContract.TABLE_NAME + " ( " + MessageContract.MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MessageContract.CHATROOM + " TEXT NOT NULL, " +
                    MessageContract.MESSAGE_TEXT + " TEXT NOT NULL, " + MessageContract.TIMESTAMP + " INTEGER NOT NULL, " +
                    MessageContract.SEQNUM + " INTEGER NOT NULL, " +
                    MessageContract.SENDER_ID + " INTEGER NOT NULL, " + "FOREIGN KEY (" + MessageContract.SENDER_ID + ") REFERENCES " + ClientContract.TABLE_NAME +
                    "(" + ClientContract.CLIENT_ID + ") ON DELETE CASCADE);";
    private static final String CLIENT_TABLE_CREATE =
            "CREATE TABLE " + ClientContract.TABLE_NAME + " ( " + ClientContract.CLIENT_ID + " INTEGER PRIMARY KEY, " +
                    ClientContract.NAME + " TEXT NOT NULL, " + ClientContract.UUID + " TEXT );";
    private static final String CREATE_INDEX =
            "CREATE INDEX MessageIndex ON " + MessageContract.TABLE_NAME + "(" + MessageContract.SENDER_ID + ");";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON");
        db.execSQL(MESSAGE_TABLE_CREATE);
        db.execSQL(CLIENT_TABLE_CREATE);
        db.execSQL(CREATE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(TAG, "Upgrading from " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + MessageContract.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ClientContract.TABLE_NAME);
        onCreate(db);
    }
}
