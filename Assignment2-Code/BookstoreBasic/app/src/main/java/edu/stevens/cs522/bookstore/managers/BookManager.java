package edu.stevens.cs522.bookstore.managers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

import edu.stevens.cs522.bookstore.providers.AsyncContentResolver;
import edu.stevens.cs522.bookstore.IContinue;
import edu.stevens.cs522.bookstore.IEntityCreator;
import edu.stevens.cs522.bookstore.IQueryListener;
import edu.stevens.cs522.bookstore.ISimpleQueryListener;
import edu.stevens.cs522.bookstore.contracts.AuthorContract;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;

/**
 * Created by wyf920621 on 2/22/15.
 */
// Define a BookManager
public class BookManager extends Manager<Book>{
    private AsyncContentResolver asyncContentResolver;
    private ContentResolver syncContentResolver;
    @Override
    protected ContentResolver getSyncContentResolver() {
        return super.getSyncContentResolver();
    }

    @Override
    protected AsyncContentResolver getAsyncContentResolver() {
        return super.getAsyncContentResolver();
    }

    @Override
    protected void executeSimpleQuery(Uri uri, ISimpleQueryListener<Book> listener) {
        super.executeSimpleQuery(uri, listener);
    }

    @Override
    protected void executeSimpleQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, ISimpleQueryListener<Book> listener) {
        super.executeSimpleQuery(uri, projection, selection, selectionArgs, listener);
    }

    @Override
    protected void executeQuery(Uri uri, IQueryListener<Book> listener) {
        super.executeQuery(uri, listener);
    }

    @Override
    protected void executeQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, IQueryListener<Book> listener) {
        super.executeQuery(uri, projection, selection, selectionArgs, listener);
    }

    @Override
    protected void reexecuteQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, IQueryListener<Book> listener) {
        super.reexecuteQuery(uri, projection, selection, selectionArgs, listener);
    }

    private final Uri CONTENT_URI = BookContract.CONTENT_URI;

    public BookManager(Context context, IEntityCreator<Book> creator, int loaderID) {
        super(context, creator, loaderID);
        this.asyncContentResolver = getAsyncContentResolver();
        this.syncContentResolver = getSyncContentResolver();
    }
    // Sync insert
    public Uri insert(Book book) {
        ContentValues values = new ContentValues();
        book.writeToProvider(values);
        Uri uri = syncContentResolver.insert(BookContract.CONTENT_URI, values);
        book.id = BookContract.getId(uri);
        for (Author author : book.authors) {
            values.clear();
            author.writeToProvider(values, book.id);
            Uri authorUri = syncContentResolver.insert(AuthorContract.CONTENT_URI, values);
            author.id = AuthorContract.getId(authorUri);
        }
        return uri;
    }
    // Async insert
    public void persistAsync(final Book book) {
        final ContentValues values = new ContentValues();
        book.writeToProvider(values);
        asyncContentResolver.insertAsync(CONTENT_URI, values, new IContinue<Uri>() {
            @Override
            public void kontinue(Uri value) {
                book.id = BookContract.getId(value);
                ArrayList<ContentValues> valueses = new ArrayList<ContentValues>(book.authors.length);
                for (int i = 0; i < book.authors.length; i++) {
                    valueses.add(new ContentValues());
                }
                for (int i = 0; i < book.authors.length; i++) {
                    Author author = book.authors[i];
                    Log.v("Author to be inserted: ", author.toString());
                    author.writeToProvider(valueses.get(i), book.id);
                    asyncContentResolver.insertAsync(AuthorContract.CONTENT_URI, valueses.get(i), new IContinue<Uri>() {
                        @Override
                        public void kontinue(Uri value) {
                            Log.v("Author id: ", String.valueOf(AuthorContract.getId(value)));
                        }
                    });
                }

            }
        });

    }
    // Simple Query Async
    public void simpleQueryAsync(Uri uri, ISimpleQueryListener<Book> listener) {
        executeSimpleQuery(uri, listener);
    }
    public void simpleQueryAsync(Uri uri, String[] projection, String selection, String[] selectionArgs, ISimpleQueryListener<Book> listener) {
        executeSimpleQuery(uri, projection, selection, selectionArgs, listener);
    }

    // Sync Query
    public Book search(long rowId) {
        Cursor cursor = syncContentResolver.query(BookContract.CONTENT_URI(String.valueOf(rowId)),
                new String[] {BookContract.ID, BookContract.TITLE, BookContract.PRICE, BookContract.ISBN, BookContract.AUTHORS},
                BookContract.ID + "=?",
                new String[] {String.valueOf(rowId)},
                null);
        Book book = new Book(BookContract.getTitle(cursor), BookContract.getAuthors(cursor), BookContract.getIsbn(cursor), BookContract.getPrice(cursor));
        book.id = rowId;
        return book;
    }

    // Async Query
    public void queryAsync(Uri uri, IQueryListener<Book> listener) {
        executeQuery(uri, listener);
    }

    public void queryAsync(long rowId, IContinue<Cursor> iContinue) { // rowId: book rowID
        asyncContentResolver.queryAsync(BookContract.CONTENT_URI(String.valueOf(rowId)), new String[] {BookContract.ID, BookContract.TITLE, BookContract.PRICE, BookContract.ISBN, BookContract.AUTHORS}, BookContract.ID + "=?", new String[] {String.valueOf(rowId)}, null, iContinue);
    }

    // Async Requery
    public void requeryAsync(Uri uri, IQueryListener<Book> listener) {
        reexcuteQuery(uri, listener);
    }

    // Sync delete
    public int delete(Book book) {
        long id = book.id;
        return syncContentResolver.delete(BookContract.CONTENT_URI(String.valueOf(id)), BookContract.ID + "=?", new String[] {String.valueOf(id)});
    }

    // Async delete
    public void deleteAsync(final Book book) {
        asyncContentResolver.deleteAsync(BookContract.CONTENT_URI(String.valueOf(book.id)), BookContract.ID + "=?", new String[] {String.valueOf(book.id)});
    }
    // Sync deleteAll
    public int deleteAll() {
        return syncContentResolver.delete(BookContract.CONTENT_URI, null, null);
    }

    // Async deleteAll
    public void deleteAllAsync() {
        asyncContentResolver.deleteAsync(CONTENT_URI, null, null);
    }

}