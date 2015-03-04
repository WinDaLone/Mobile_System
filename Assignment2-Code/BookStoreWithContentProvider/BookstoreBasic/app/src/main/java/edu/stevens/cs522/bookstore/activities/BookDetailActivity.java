package edu.stevens.cs522.bookstore.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;

public class BookDetailActivity extends Activity {
    TextView id = null;
    TextView title = null;
    TextView author = null;
    TextView isbn = null;
    TextView price = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        id = (TextView)findViewById(R.id.detail_id);
        title = (TextView)findViewById(R.id.detail_title);
        author = (TextView)findViewById(R.id.detail_author);
        isbn = (TextView)findViewById(R.id.detail_isbn);
        price = (TextView)findViewById(R.id.detail_price);
        Intent intent = getIntent();
        Book book = intent.getParcelableExtra(BookStoreActivity.BOOK_STORE_KEY);
        id.setText(String.valueOf(book.id));
        title.setText(book.title);
        isbn.setText(book.isbn);
        price.setText(book.price);
        Author[] authors = book.authors;
        String author_name = "";
        for (int i = 0; i < authors.length; i++) {
            author_name = author_name + authors[i].toString();
            if (i != authors.length - 1){
                author_name += "; ";
            }
        }
        author.setText(author_name);
    }

}
