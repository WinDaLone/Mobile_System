package edu.stevens.cs522.bookstore.activities;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
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
import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Book;
import edu.stevens.cs522.bookstore.providers.AsyncContentResolver;

public class BookStoreActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>  {
	// LOADER_ID
    private static final int MY_LOADER_ID = 1;

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
    private AsyncContentResolver asyncContentResolver;
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
		// TODO Set the layout (use cart.xml layout)
		setContentView(R.layout.cart);
		// TODO use an array adapter to display the cart contents.
        listView = (ListView)findViewById(android.R.id.list);
        fillData(null);
        final LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(MY_LOADER_ID, null, this);
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
        asyncContentResolver = new AsyncContentResolver(getContentResolver());
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// TODO provide ADD, DELETE and CHECKOUT options - Done
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.bookstore_menu, menu);
		return true;
	}

    // ADD and CHECKOUT
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		// TODO

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
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		// TODO Handle results from the Search and Checkout activities.
		
		// Use SEARCH_REQUEST and CHECKOUT_REQUEST codes to distinguish the cases.
		
		// SEARCH: add the book that is returned to the shopping cart.
		
		// CHECKOUT: empty the shopping cart.
        if (requestCode == ADD_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.v("ADD_REQUEST: ", "RESULT_OK");
                getContentResolver().notifyChange(BookContract.CONTENT_URI, null);
            }
        }
        else if (requestCode == CHECKOUT_REQUEST) {
            if (resultCode == RESULT_OK) {
                // delete all
                Log.v("CHECKOUT_REQUEST", "RESULT_OK");
                asyncContentResolver.deleteAsync(BookContract.CONTENT_URI, null, null);
                getContentResolver().notifyChange(BookContract.CONTENT_URI, null);
            }
        }
        //getLoaderManager().restartLoader(MY_LOADER_ID, null, BookStoreActivity.this);
    }


    @Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// TODO save the shopping cart contents (which should be a list of parcelables).
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
            final long rowId = (Long)mActionMode.getTag();
            String[] projection = new String[] {BookContract.ID, BookContract.TITLE, BookContract.PRICE, BookContract.ISBN, BookContract.AUTHORS};
            String selection = BookContract.ID + "=?";
            String[] selectionArgs = new String[] {String.valueOf(rowId)};
            asyncContentResolver.queryAsync(BookContract.CONTENT_URI(String.valueOf(rowId)), projection, selection, selectionArgs, null, new IContinue<Cursor>() {
                @Override
                public void kontinue(Cursor value) {
                    if(value.moveToFirst()) {
                        Book book = new Book(value);
                        switch (menuItem.getItemId()) {
                            case R.id.detail:
                                Intent intent = new Intent(BookStoreActivity.this, BookDetailActivity.class);
                                intent.putExtra(BOOK_STORE_KEY, book);
                                startActivity(intent);
                                break;
                            case R.id.delete:
                                String selection = BookContract.ID + "=?";
                                String[] selectionArgs = new String[]{String.valueOf(rowId)};
                                asyncContentResolver.deleteAsync(BookContract.CONTENT_URI(String.valueOf(rowId)), selection, selectionArgs);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case MY_LOADER_ID:
                String[] projection = {BookContract.ID, BookContract.TITLE, BookContract.AUTHORS};
                return new CursorLoader(this, BookContract.CONTENT_URI, projection, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        this.adapter.swapCursor(cursor);
        cursor.setNotificationUri(getContentResolver(), BookContract.CONTENT_URI);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        this.adapter.swapCursor(null);
    }

}