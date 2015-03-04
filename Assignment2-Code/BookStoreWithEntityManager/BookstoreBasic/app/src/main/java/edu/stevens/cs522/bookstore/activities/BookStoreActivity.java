package edu.stevens.cs522.bookstore.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import edu.stevens.cs522.bookstore.BookAdapter;
import edu.stevens.cs522.bookstore.IContinue;
import edu.stevens.cs522.bookstore.IEntityCreator;
import edu.stevens.cs522.bookstore.IQueryListener;
import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Book;
import edu.stevens.cs522.bookstore.managers.BookManager;
import edu.stevens.cs522.bookstore.managers.TypedCursor;

public class BookStoreActivity extends ListActivity {
	// LOADER_ID
    public static final int MY_LOADER_ID = 1;

	// Use this when logging errors and warnings.
	@SuppressWarnings("unused")
	private static final String TAG = BookStoreActivity.class.getCanonicalName();
	
	// These are request codes for subactivity request calls
	static final private int ADD_REQUEST = 1;
	
	@SuppressWarnings("unused")
	static final private int CHECKOUT_REQUEST = ADD_REQUEST + 1;

	// There is a reason this must be an ArrayList instead of a List.
	@SuppressWarnings("unused")

    private ActionMode mActionMode = null;
    private BookAdapter adapter = null;
    private BookManager bookManager = null;
    ListView listView;

    public static final String BOOK_STORE_KEY = "BOOK_STORE_KEY";

    private void fillData(Cursor c) {
        String[] to = new String[] { BookContract.TITLE, BookContract.AUTHORS};
        int[] from = new int[] {R.id.cart_row_title, R.id.cart_row_author};
        adapter = new BookAdapter(this, null);
        listView.setAdapter(this.adapter);
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cart);

        listView = (ListView)findViewById(android.R.id.list);
        fillData(null); // take no cursor
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                view.setSelected(true);
                Long rowId = adapterView.getAdapter().getItemId(position);
                if (mActionMode != null) {
                    return false;
                }
                mActionMode = BookStoreActivity.this.startActionMode(mActionModeCallback);
                mActionMode.setTag(rowId);
                return true;
            }

        });
        bookManager = new BookManager(this,
                new IEntityCreator<Book>() {
                    @Override
                    public Book create(Cursor cursor) {
                        Book book = new Book(cursor);
                        book.id = BookContract.getId(cursor);
                        return book;
                    }
                },
                MY_LOADER_ID
        );
        bookManager.queryAsync(BookContract.CONTENT_URI, new IQueryListener<Book>() {
            @Override
            public void handleResults(TypedCursor<Book> results) {
                adapter.swapCursor(results.getCursor());
                results.getCursor().setNotificationUri(getContentResolver(), BookContract.CONTENT_URI);
            }

            @Override
            public void closeResults() {
                adapter.swapCursor(null);
            }
        });
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.bookstore_menu, menu);
		return true;
	}

    // ADD and CHECKOUT
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		// ADD provide the UI for adding a book
		// Intent addIntent = new Intent(this, AddBookActivity.class);
		// startActivityForResult(addIntent, ADD_REQUEST);
		
		// DELETE delete the currently selected book
		
		// CHECKOUT provide the UI for checking out
		switch (item.getItemId()) {
            case R.id.add: // Add
                Intent addIntent = new Intent(this, AddBookActivity.class);
                startActivityForResult(addIntent, ADD_REQUEST);
                break;
            case R.id.checkout: // Checkout
                if (listView.getAdapter().getCount() < 1) {
                    Toast.makeText(this, "No books", Toast.LENGTH_LONG).show();
                    break;
                }
                Intent checkoutIntent = new Intent(this, CheckoutActivity.class);
                checkoutIntent.putExtra(BOOK_STORE_KEY, listView.getAdapter().getCount());
                startActivityForResult(checkoutIntent, CHECKOUT_REQUEST);
                break;
        }
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, final int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		// Use SEARCH_REQUEST and CHECKOUT_REQUEST codes to distinguish the cases.
		
		// SEARCH: add the book that is returned to the shopping cart.
		
		// CHECKOUT: empty the shopping cart.
        if (requestCode == ADD_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.v("ADD_REQUEST", "RESULT OK");
                getContentResolver().notifyChange(BookContract.CONTENT_URI, null);
            }
        }
        else if (requestCode == CHECKOUT_REQUEST) {
            if (resultCode == RESULT_OK) {
                bookManager.deleteAllAsync();
                getContentResolver().notifyChange(BookContract.CONTENT_URI, null);
            }
        }
	}

    @Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.setClassLoader(getClass().getClassLoader());
        super.onSaveInstanceState(savedInstanceState);
	}

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.bookstore_cab_menu, menu);
            return true;
        }


        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, final MenuItem menuItem) {
            long rowId = (Long)mActionMode.getTag();
            bookManager.queryAsync(rowId, new IContinue<Cursor>() {
                @Override
                public void kontinue(Cursor value) {
                    Book book;
                    if (value.moveToFirst()) {
                        book = new Book(BookContract.getTitle(value), BookContract.getAuthors(value), BookContract.getIsbn(value), BookContract.getPrice(value));
                        book.id = BookContract.getId(value);
                        switch (menuItem.getItemId()) {
                            case R.id.detail:
                                Intent intent = new Intent(BookStoreActivity.this, BookDetailActivity.class);
                                intent.putExtra(BOOK_STORE_KEY, book.id);
                                startActivity(intent);
                                break;
                            case R.id.delete:
                                bookManager.deleteAsync(book);
                                getContentResolver().notifyChange(BookContract.CONTENT_URI, null);
                                break;
                        }
                    }
                    value.close();
                }
            });
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mActionMode = null;
        }
    };
}