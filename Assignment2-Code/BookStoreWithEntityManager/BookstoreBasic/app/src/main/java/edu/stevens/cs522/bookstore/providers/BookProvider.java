package edu.stevens.cs522.bookstore.providers;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import edu.stevens.cs522.bookstore.contracts.AuthorContract;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.databases.DatabaseHelper;

public class BookProvider extends ContentProvider {
    public static final String AUTHORITY = "edu.stevens.cs522.bookstore";
    // CONTENT_URI(authority, path)
    public static Uri CONTENT_URI(String authority, String path) {
        return new Uri.Builder().scheme("content").authority(authority).path(path).build();
    }

    // withExtendedPath(Uri uri, String... path)
    public static Uri withExtendedPath(Uri uri, String... path) {
        Uri.Builder builder = uri.buildUpon();
        for (String s : path)
            builder.appendPath(s);
        return builder.build();
    }

    // CONTENT_PATH(Uri uri)
    public static String CONTENT_PATH(Uri uri) {
        return uri.getPath().substring(1);
    }

    // contentType(content)
    public static String contentType(String content) {
        return "vnd.android.cursor/vnd." + AUTHORITY + "." + content + "s";
    }

    // contentItemType(content)
    public static String contentItemType(String content) {
        return "vnd.android.cursor/vnd." + AUTHORITY + "." + content;
    }

    private DatabaseHelper databaseHelper;

    // To differentiate between the different URI requests
    private static final int BOOK_ALL_ROWS = 1;
    private static final int BOOK_SINGLE_ROW = 2;
    private static final int AUTHOR_ALL_ROWS = 3;
    private static final int AUTHOR_SINGLE_ROW = 4;

    // Used to dispatch operation based on URI
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, BookContract.CONTENT_PATH, BOOK_ALL_ROWS); // Books
        uriMatcher.addURI(AUTHORITY, BookContract.CONTENT_PATH_ITEM, BOOK_SINGLE_ROW); // Books/#
        uriMatcher.addURI(AUTHORITY, AuthorContract.CONTENT_PATH, AUTHOR_ALL_ROWS); // Authors
        uriMatcher.addURI(AUTHORITY, AuthorContract.CONTENT_PATH_ITEM, AUTHOR_SINGLE_ROW); // Authors/#
    }

    public BookProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.execSQL("PRAGMA foreign_keys = ON");
        // Implement this to handle requests to delete one or more rows.
        int res;
        switch (uriMatcher.match(uri)) {
            case BOOK_ALL_ROWS:
                res = database.delete(BookContract.TABLE_NAME, null, null);
                return res;
            case BOOK_SINGLE_ROW:
                selection = BookContract.ID + "=?";
                String[] args = {String.valueOf(BookContract.getId(uri))};
                res = database.delete(BookContract.TABLE_NAME, selection, args);
                return res;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        switch (uriMatcher.match(uri)) {
            case BOOK_ALL_ROWS:
                return BookContract.contentType;
            case BOOK_SINGLE_ROW:
                return BookContract.contentItemType;
            case AUTHOR_ALL_ROWS:
                return AuthorContract.contentType;
            case AUTHOR_SINGLE_ROW:
                return AuthorContract.contentItemType;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.execSQL("PRAGMA foreign_keys = ON");
        long rowId;
        switch (uriMatcher.match(uri)) {
            case BOOK_ALL_ROWS: // INSERT A BOOK
                rowId = database.insert(BookContract.TABLE_NAME, null, values);
                if (rowId > 0) {
                    Uri instanceUri = BookContract.CONTENT_URI(String.valueOf(rowId));
                    ContentResolver cr = getContext().getContentResolver();
                    cr.notifyChange(uri, null);
                    return instanceUri;
                }
                throw new SQLException("Insertion failed");
            case AUTHOR_ALL_ROWS: // INSERT AN AUTHOR
                rowId = database.insert(AuthorContract.TABLE_NAME, null, values);
                Log.v("Author to insert:", values.getAsString(AuthorContract.NAME));
                if (rowId > 0) {
                    Uri instanceUri = AuthorContract.CONTENT_URI(String.valueOf(rowId));
                    ContentResolver cr = getContext().getContentResolver();
                    cr.notifyChange(BookContract.CONTENT_URI, null);
                    return instanceUri;
                }
                throw new SQLException("Insertion failed");
            default:
                throw new SQLException("Insertion failed");
        }
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        databaseHelper = new DatabaseHelper(getContext());
        databaseHelper.getWritableDatabase().execSQL("PRAGMA foreign_keys = ON");
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database;
        try {
            database = databaseHelper.getWritableDatabase();
        } catch (SQLException e) {
            database = databaseHelper.getReadableDatabase();
        }
        database.execSQL("PRAGMA foreign_keys = ON");
        // TODO: Implement this to handle query requests from clients.
        String query;
        switch (uriMatcher.match(uri)) {
            case BOOK_ALL_ROWS:
                query = "SELECT " + BookContract.TABLE_NAME + "." + BookContract.ID + ", " + BookContract.TITLE + ", " + BookContract.PRICE
                        + ", " + BookContract.ISBN + ", GROUP_CONCAT(" + AuthorContract.NAME + ",\'" + BookContract.SEPARATE_CHAR + "\') as " + BookContract.AUTHORS + " FROM " +
                        BookContract.TABLE_NAME + " LEFT OUTER JOIN " + AuthorContract.TABLE_NAME + " ON " + BookContract.TABLE_NAME + "." + BookContract.ID + " = " + AuthorContract.TABLE_NAME + "." +
                        AuthorContract.BOOK_FOREIGN_KEY + " GROUP BY " + BookContract.TABLE_NAME + "." + BookContract.ID + ", " + BookContract.TITLE + ", "
                        + BookContract.PRICE + ", " + BookContract.ISBN + ";";
                return database.rawQuery(query, null);
            case BOOK_SINGLE_ROW:
                query = "SELECT " + BookContract.TABLE_NAME + "." + BookContract.ID + " AS " + BookContract.ID + ", " + BookContract.TABLE_NAME + "." + BookContract.TITLE + " AS " + BookContract.TITLE + ", "+ BookContract.TABLE_NAME + "." + BookContract.PRICE + " AS " + BookContract.PRICE
                        + ", " + BookContract.TABLE_NAME + "." + BookContract.ISBN + " AS " + BookContract.ISBN + ", GROUP_CONCAT(" + AuthorContract.TABLE_NAME + "." + AuthorContract.NAME + ",\'" + BookContract.SEPARATE_CHAR + "\') as " + BookContract.AUTHORS + " FROM " +
                        BookContract.TABLE_NAME + " LEFT OUTER JOIN " + AuthorContract.TABLE_NAME + " ON " + BookContract.TABLE_NAME + "." + BookContract.ID + " = " + AuthorContract.TABLE_NAME + "." +
                        AuthorContract.BOOK_FOREIGN_KEY + " WHERE " + BookContract.TABLE_NAME + "." + BookContract.ID + " = ?" + ";";
                return database.rawQuery(query, new String[] {String.valueOf(BookContract.getId(uri))});
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.execSQL("PRAGMA foreign_keys = ON");
        int res;
        switch (uriMatcher.match(uri)) {
            case BOOK_ALL_ROWS:
                res = database.update(BookContract.TABLE_NAME, values, null, null);
                return res;
            case BOOK_SINGLE_ROW:
                selection = BookContract.ID + "=?";
                String[] args = {String.valueOf(BookContract.getId(uri))};
                res = database.update(BookContract.TABLE_NAME, values, selection, args);
                return res;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}