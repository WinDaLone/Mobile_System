package edu.stevens.cs522.bookstore.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import edu.stevens.cs522.bookstore.contracts.AuthorContract;
import edu.stevens.cs522.bookstore.contracts.BookContract;

/**
 * Created by wyf920621 on 2/21/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String BOOK_TABLE_CREATE =
            "CREATE TABLE " + BookContract.TABLE_NAME + " (" + BookContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + BookContract.TITLE
                    + " TEXT NOT NULL, " + BookContract.ISBN + " TEXT NOT NULL, " + BookContract.PRICE + " TEXT NOT NULL " + ");";
    private static final String AUTHOR_TABLE_CREATE =
            "CREATE TABLE " + AuthorContract.TABLE_NAME + " (" + AuthorContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + AuthorContract.NAME
                    + " TEXT NOT NULL, " + AuthorContract.BOOK_FOREIGN_KEY + " INTEGER, " + "FOREIGN KEY (" + AuthorContract.BOOK_FOREIGN_KEY + ") REFERENCES " + BookContract.TABLE_NAME + "("
                    + BookContract.ID + ") ON DELETE CASCADE" + ");";
    private static final String CREATE_INDEX =
            "CREATE INDEX AuthorsBookIndex ON " + AuthorContract.TABLE_NAME + "(" + AuthorContract.BOOK_FOREIGN_KEY + ");";

    private static final String DATABASE_NAME = "cart.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("PRAGMA foreign_keys = ON");
        sqLiteDatabase.execSQL(BOOK_TABLE_CREATE);
        sqLiteDatabase.execSQL(AUTHOR_TABLE_CREATE);
        sqLiteDatabase.execSQL(CREATE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        // Log the version upgrade
        Log.w("DatabaseHelper", "Upgrading from version" + i + " to " + i2);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BookContract.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + AuthorContract.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
