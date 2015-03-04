package edu.stevens.cs522.bookstore.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import edu.stevens.cs522.bookstore.IContinue;
import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.contracts.AuthorContract;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;
import edu.stevens.cs522.bookstore.providers.AsyncContentResolver;

public class AddBookActivity extends Activity {

	// Use this as the key to return the book details as a Parcelable extra in the result intent.
	public static final String BOOK_RESULT_KEY = "book_result";
    EditText searchTitle = null;
    EditText searchAuthor = null;
    EditText searchIsbn = null;
    EditText searchPrice = null;
    Intent intent = null;
    AsyncContentResolver asyncContentResolver;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        intent = getIntent();
		setContentView(R.layout.add_book);
        searchTitle = (EditText)findViewById(R.id.search_title);
        searchAuthor = (EditText)findViewById(R.id.search_author);
        searchIsbn = (EditText)findViewById(R.id.search_isbn);
        searchPrice = (EditText)findViewById(R.id.search_price);
        asyncContentResolver = new AsyncContentResolver(getContentResolver());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// TODO provide SEARCH and CANCEL options
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.addbook_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		// TODO
		
		// SEARCH: return the book details to the BookStore activity
		
		// CANCEL: cancel the search request
        switch (item.getItemId()) {
            case R.id.search:
                final Book newBook = searchBook();
                if (newBook == null) {
                    searchTitle.setText("");
                    searchAuthor.setText("");
                    searchIsbn.setText("");
                    searchPrice.setText("");
                    return false;
                }
                final ContentValues values = new ContentValues();
                newBook.writeToProvider(values);
                asyncContentResolver.insertAsync(BookContract.CONTENT_URI, values, new IContinue<Uri>() {
                    @Override
                    public void kontinue(Uri value) {
                        newBook.id = BookContract.getId(value);
                        ArrayList<ContentValues> valueses = new ArrayList<ContentValues>(newBook.authors.length);
                        for (int i = 0; i < newBook.authors.length; i++) {
                            valueses.add(new ContentValues());
                        }
                        for (int i = 0; i < newBook.authors.length; i++) {
                            Author author = newBook.authors[i];
                            Log.v("Author to be inserted: ", author.toString());
                            author.writeToProvider(valueses.get(i), newBook.id);
                            asyncContentResolver.insertAsync(AuthorContract.CONTENT_URI, valueses.get(i), new IContinue<Uri>() {
                                @Override
                                public void kontinue(Uri value) {
                                    Log.v("Author id: ", String.valueOf(AuthorContract.getId(value)));
                                }
                            });
                        }
                    }
                });
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
            }
        }
        String isbn = searchIsbn.getText().toString();
        String price = searchPrice.getText().toString();
        try {
            Double.parseDouble(price);
            Long.parseLong(isbn);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid author name", Toast.LENGTH_SHORT).show();
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