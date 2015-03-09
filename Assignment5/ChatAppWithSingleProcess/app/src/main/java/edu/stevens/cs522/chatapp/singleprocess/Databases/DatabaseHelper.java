package edu.stevens.cs522.chatapp.singleprocess.Databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import edu.stevens.cs522.chatapp.singleprocess.Contracts.MessageContract;
import edu.stevens.cs522.chatapp.singleprocess.Contracts.PeerContract;

/**
 * Created by wyf920621 on 3/3/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String MESSAGE_TABLE = "Messages";
    private static final String PEER_TABLE = "Peers";
    private static final String PEER_TABLE_CREATE =
            "CREATE TABLE " + PEER_TABLE + " ( " + PeerContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PeerContract.NAME + " TEXT NOT NULL, " + PeerContract.ADDRESS + " TEXT NOT NULL, " +
                    PeerContract.PORT + " INTEGER NOT NULL);";
    private static final String MESSAGE_TABLE_CREATE =
            "CREATE TABLE " + MESSAGE_TABLE + " ( " + MessageContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MessageContract.MESSAGE_TEXT + " TEXT NOT NULL, " + MessageContract.SENDER + " TEXT NOT NULL, " + MessageContract.PEER_FK + " INTEGER, " +
                    "FOREIGN KEY (" + MessageContract.PEER_FK + ") REFERENCES " + PEER_TABLE + "(" + PeerContract.ID + ")" +
                    " ON DELETE CASCADE);";
    private static final String CREATE_INDEX =
            "CREATE INDEX MessagesPeerIndex ON " + MESSAGE_TABLE + "(" + MessageContract.PEER_FK + ");";




    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("PRAGMA foreign_keys = ON");
        sqLiteDatabase.execSQL(PEER_TABLE_CREATE);
        sqLiteDatabase.execSQL(MESSAGE_TABLE_CREATE);
        sqLiteDatabase.execSQL(CREATE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        Log.v("DatabaseHelper", "Upgrading from " + i + " to " + i2);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PEER_TABLE);
        onCreate(sqLiteDatabase);
    }
}