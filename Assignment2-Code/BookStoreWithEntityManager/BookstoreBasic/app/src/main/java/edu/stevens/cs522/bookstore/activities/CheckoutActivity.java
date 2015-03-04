package edu.stevens.cs522.bookstore.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import edu.stevens.cs522.bookstore.R;

public class CheckoutActivity extends Activity {
    EditText name = null;
    EditText email = null;
    EditText creditAccount = null;
    EditText address = null;
    Intent intent = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        intent = getIntent();
		setContentView(R.layout.checkout);
        name = (EditText)findViewById(R.id.name);
        email = (EditText)findViewById(R.id.email);
        creditAccount = (EditText)findViewById(R.id.credit);
        address = (EditText)findViewById(R.id.address);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.checkout_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		// ORDER: display a toast message of how many books have been ordered and return
		
		// CANCEL: just return with REQUEST_CANCELED as the result code
        switch (item.getItemId()) {
            case R.id.checkout_order:
                int number = intent.getIntExtra(BookStoreActivity.BOOK_STORE_KEY, 0);
                String temp;
                if (number == 1) {
                    temp = " book ordered";
                }
                else {
                    temp = " books ordered";
                }
                Toast.makeText(this, number + temp, Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.checkout_cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
		return false;
	}
	
}