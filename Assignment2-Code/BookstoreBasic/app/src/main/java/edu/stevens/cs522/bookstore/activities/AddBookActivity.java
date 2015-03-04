package edu.stevens.cs522.bookstore.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import edu.stevens.cs522.bookstore.IEntityCreator;
import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;
import edu.stevens.cs522.bookstore.managers.BookManager;

public class AddBookActivity extends Activity {
    public static final int ADD_LOADER_ID = 2;
	// Use this as the key to return the book details as a Parcelable extra in the result intent.
    EditText searchTitle = null;
    EditText searchAuthor = null;
    EditText searchIsbn = null;
    EditText searchPrice = null;
    Intent intent = null;
    BookManager bookManager;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        intent = getIntent();
		setContentView(R.layout.add_book);
        searchTitle = (EditText)findViewById(R.id.search_title);
        searchAuthor = (EditText)findViewById(R.id.search_author);
        searchIsbn = (EditText)findViewById(R.id.search_isbn);
        searchPrice = (EditText)findViewById(R.id.search_price);
        bookManager = new BookManager(this, new IEntityCreator<Book>() {
            @Override
            public Book create(Cursor cursor) {
                Book book = new Book(cursor);
                book.id = BookContract.getId(cursor);
                return book;
            }
        },
        ADD_LOADER_ID);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.addbook_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		// SEARCH: return the book details to the BookStore activity
		
		// CANCEL: cancel the search request
        switch (item.getItemId()) {
            case R.id.search:
                Book newBook = searchBook();
                if (newBook == null) {
                    searchTitle.setText("");
                    searchAuthor.setText("");
                    searchIsbn.setText("");
                    searchPrice.setText("");
                    return false;
                }
                bookManager.persistAsync(newBook);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.search_cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
		return true;
	}

    public Book searchBook(){
		/*
		 * Search for the specified book.
		 */
		// TODO Just build a Book object with the search criteria and return that.
        String title = searchTitle.getText().toString();
        String author = searchAuthor.getText().toString();
        String[] nameList = BookContract.readStringArray(author);
        Author[] authors = new Author[nameList.length];
        boolean right = true;
        for (int i = 0; i < nameList.length; i++) {
            String[] name = nameList[i].split(Author.SEPARATE_KEY);
            if (name.length == 3) {
                authors[i] = new Author(name[0], name[1], name[2]);
            }
            else if (name.length == 2) {
                authors[i] = new Author(name[0], name[1]);
            }
            else {
                right = false;
                break;
            }
        }
        String isbn = searchIsbn.getText().toString();
        String price = searchPrice.getText().toString();
        try {
            long isbn_v = Long.parseLong(isbn);
            double price_v = Double.parseDouble(price);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid isbn or price", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (right) {
            return new Book(title, authors, isbn, price);
        }
        else {
            Toast.makeText(this, "Invalid author name", Toast.LENGTH_SHORT).show();
            return null;
        }
	}

}