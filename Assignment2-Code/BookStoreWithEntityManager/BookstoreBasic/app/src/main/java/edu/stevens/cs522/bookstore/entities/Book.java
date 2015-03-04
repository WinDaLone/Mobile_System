package edu.stevens.cs522.bookstore.entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.BadParcelableException;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import edu.stevens.cs522.bookstore.contracts.BookContract;

public class Book implements Parcelable {
	
	// TODO Modify this to implement the Parcelable interface.
    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel parcel) {
            return new Book(parcel);
        }

        @Override
        public Book[] newArray(int i) {
            return new Book[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(title);
        parcel.writeInt(authors.length);
        for (int j = 0; j < authors.length; j++) {
            parcel.writeParcelable(authors[j], i);
        }
        parcel.writeString(isbn);
        parcel.writeString(price);
    }

    public void writeToProvider(ContentValues values) {
        BookContract.putTitle(values, this.title);
        BookContract.putIsbn(values, this.isbn);
        BookContract.putPrice(values, this.price);
    }


    // TODO redefine toString() to display book title and price (why?).
    @Override
    public String toString() {
        return "Title: " + this.title + "     Price: " + this.price;
    }

    public long id;

	public String title;
	
	public Author[] authors;
	
	public String isbn;
	
	public String price;

	public Book(String title, Author[] authors, String isbn, String price) {
		this.title = title;
		this.authors = authors;
		this.isbn = isbn;
		this.price = price;
	}

    public Book (Parcel parcel) {
        this.id = parcel.readLong();
        this.title = parcel.readString();
        int length = parcel.readInt();
        this.authors = new Author[length];
        for (int j = 0; j < length; j++) {
            try {
                this.authors[j] = parcel.readParcelable(Author.class.getClassLoader());
            }
            catch (BadParcelableException e) {
                Log.e(Book.class.getCanonicalName(), "BadParcelableException", e);
            }
        }
        this.isbn = parcel.readString();
        this.price = parcel.readString();
    }

    public Book(Cursor cursor) {
        this.id = BookContract.getId(cursor);
        this.title = BookContract.getTitle(cursor);
        this.authors = BookContract.getAuthors(cursor);
        this.isbn = BookContract.getIsbn(cursor);
        this.price = BookContract.getPrice(cursor);
    }
}