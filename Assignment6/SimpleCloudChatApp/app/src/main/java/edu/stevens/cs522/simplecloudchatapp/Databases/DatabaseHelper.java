package edu.stevens.cs522.simplecloudchatapp.Databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = DatabaseHelper.class.getSimpleName();
    private static final String MESSAGE_TABLE = "Messages";
    private static final String MESSAGE_TABLE_CREATE =
            "CREATE TABLE " + MESSAGE_TABLE + " ( " + MessageContract.MESSAGE_ID + " INTEGER PRIMARY KEY, " +
                    MessageContract.MESSAGE_TEXT + " TEXT NOT NULL, " + MessageContract.TIMESTAMP + " INTEGER, " +
                    MessageContract.SENDER_ID + " INTEGER NOT NULL);";
    private static final String CREATE_INDEX =
            "CREATE INDEX MessageIndex ON " + MESSAGE_TABLE + "(" + MessageContract.SENDER_ID + ");";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MESSAGE_TABLE_CREATE);
        db.execSQL(CREATE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(TAG, "Upgrading from " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE);
        onCreate(db);
    }
}
