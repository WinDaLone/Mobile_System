package edu.stevens.cs522.bookstore.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.regex.Pattern;

import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.providers.BookProvider;

/**
 * Created by wyf920621 on 2/8/15.
 */

public class BookContract {
    public static final String TABLE_NAME = "Books";
    public static final String ID = "_id";
    public static final String TITLE = "title";
    public static final String ISBN = "isbn";
    public static final String PRICE = "price";
    public static final String AUTHORS = "authors";
    public static final String SEPARATE_CHAR = "|";
    public static final String CONTENT = "Book"; // content
    private static final Pattern SEPARATOR = Pattern.compile(Character.toString(SEPARATE_CHAR.charAt(0)), Pattern.LITERAL);

    // CONTENT_URI(id)
    public static Uri CONTENT_URI (String id) {
        return BookProvider.withExtendedPath(CONTENT_URI, id);
    }

    public static final Uri CONTENT_URI = BookProvider.CONTENT_URI(BookProvider.AUTHORITY, TABLE_NAME);
    public static String CONTENT_PATH = BookProvider.CONTENT_PATH(CONTENT_URI); // books
    public static String CONTENT_PATH_ITEM = BookProvider.CONTENT_PATH(CONTENT_URI("#")); // books/#
    public static final String contentType = BookProvider.contentType(CONTENT);
    public static final String contentItemType = BookProvider.contentItemType(CONTENT);

    public static String getTitle(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
    }

    public static void putTitle(ContentValues values, String title) {
        values.put(TITLE, title);
    }

    public static long getId(Uri uri) {
        return Long.parseLong(uri.getLastPathSegment());
    }

    public static long getId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(ID));
    }

    public static void putId(ContentValues values, long id) {
        values.put(ID, id);
    }

    public static String getIsbn(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(ISBN));
    }

    public static Author[] getAuthors(Cursor cursor) {
        String temp = cursor.getString(cursor.getColumnIndexOrThrow(AUTHORS));
        String[] authors = SEPARATOR.split(temp);
        int len = authors.length;
        Author[] author = new Author[len];
        for (int i = 0; i < len; i++) {
            String[] names = authors[i].split(Author.SEPARATE_KEY);
            if (names.length == 2) {
                author[i] = new Author(names[0], names[1]);
            }
            else if (names.length == 3) {
                author[i] = new Author(names[0], names[1], names[2]);
            }
        }
        return author;
    }

    public static Author[] getAuthors(String nameList) {
        String[] authors = SEPARATOR.split(nameList);
        int len = authors.length;
        Author[] author = new Author[len];
        for (int i = 0; i < len; i++) {
            String[] names = authors[i].split(Author.SEPARATE_KEY);
            if (names.length == 2) {
                author[i] = new Author(names[0], names[1]);
            }
            else if (names.length == 3) {
                author[i] = new Author(names[0], names[1], names[2]);
            }
        }
        return author;
    }

    public static void putAuthors(ContentValues values, Author[] authors) {
        String temp = "";
        for (int i = 0; i < authors.length; i++) {
            temp += authors[i].toString();
            if (i < authors.length - 1) {
                temp += SEPARATE_CHAR;
            }
        }
        values.put(AUTHORS, temp);
    }

    public static void putIsbn (ContentValues values, String isbn) {
        values.put(ISBN, isbn);
    }

    public static String getPrice(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(PRICE));
    }

    public static void putPrice(ContentValues values, String price) {
        values.put(PRICE, price);
    }

    public static String[] readStringArray(String in) {
        return SEPARATOR.split(in);
    }
}
