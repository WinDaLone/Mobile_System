package edu.stevens.cs522.bookstore.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

import edu.stevens.cs522.bookstore.IEntityCreator;
import edu.stevens.cs522.bookstore.ISimpleQueryListener;
import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;
import edu.stevens.cs522.bookstore.managers.BookManager;

public class BookDetailActivity extends Activity {
    private final int Book_Detail_ID = 3;
    TextView id = null;
    TextView title = null;
    TextView author = null;
    TextView isbn = null;
    TextView price = null;
    BookManager bookManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        bookManager = new BookManager(this, new IEntityCreator<Book>() {
            @Override
            public Book create(Cursor cursor) {
                Book book = new Book(cursor);
                book.id = BookContract.getId(cursor);
                return book;
            }
        }, Book_Detail_ID);

        id = (TextView)findViewById(R.id.detail_id);
        title = (TextView)findViewById(R.id.detail_title);
        author = (TextView)findViewById(R.id.detail_author);
        isbn = (TextView)findViewById(R.id.detail_isbn);
        price = (TextView)findViewById(R.id.detail_price);
        Intent intent = getIntent();
        long book_id = intent.getLongExtra(BookStoreActivity.BOOK_STORE_KEY, 0);
        String[] projection = new String[] {BookContract.ID, BookContract.TITLE, BookContract.AUTHORS, BookContract.ISBN, BookContract.PRICE};
        String selection = BookContract.ID + "=?";
        String[] selectionArgs = new String[] {String.valueOf(book_id)};
        bookManager.simpleQueryAsync(BookContract.CONTENT_URI(String.valueOf(book_id)), projection, selection, selectionArgs, new ISimpleQueryListener<Book>() {
            @Override
            public void handleResults(List<Book> results) {
                for (Book book : results) {
                    id.setText(String.valueOf(book.id));
                    title.setText(book.title);
                    isbn.setText(book.isbn);
                    price.setText(book.price);
                    Author[] authors = book.authors;
                    String author_name = "";
                    for (int i = 0; i < authors.length; i++) {
                        Log.v("Author Readed: ", authors[i].toString());
                        author_name = author_name + authors[i].toString();
                        if (i != authors.length - 1){
                            author_name += "; ";
                        }
                    }
                    author.setText(author_name);
                }
            }
        });
    }

}
