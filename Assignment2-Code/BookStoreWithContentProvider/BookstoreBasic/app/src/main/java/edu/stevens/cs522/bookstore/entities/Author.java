package edu.stevens.cs522.bookstore.entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import edu.stevens.cs522.bookstore.contracts.AuthorContract;

public class Author implements Parcelable{
    public static final String SEPARATE_KEY = " ";

	// TODO Modify this to implement the Parcelable interface.
    public static final Creator<Author> CREATOR = new Creator<Author>() {
        @Override
        public Author createFromParcel(Parcel parcel) {
            return new Author(parcel);
        }

        @Override
        public Author[] newArray(int i) {
            return new Author[i];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(firstName);
        if (middleInitial.compareTo("") == 0) {
            parcel.writeString("");
        }
        else {
            parcel.writeString(middleInitial);
        }
        parcel.writeString(lastName);
    }

    // NOTE: middleInitial may be NULL!
    public Author(String firstName, String middleInitial, String lastName) {
        this.firstName = firstName;
        this.middleInitial = middleInitial;
        this.lastName = lastName;
    }

    public Author(String firstName, String lastName) {
        this.firstName = firstName;
        this.middleInitial = "";
        this.lastName = lastName;
    }

    public Author(Cursor cursor) {
        String name = AuthorContract.getName(cursor);
        String[] author = name.split(SEPARATE_KEY);
        if (author.length == 2) {
            this.firstName = author[0];
            this.middleInitial = "";
            this.lastName = author[1];
        }
        else if (author.length == 3) {
            this.firstName = author[0];
            this.middleInitial = author[1];
            this.lastName = author[2];
        }
    }

    public Author(Parcel parcel) {
        this.id = parcel.readLong();
        this.firstName = parcel.readString();
        this.middleInitial = parcel.readString();
        this.lastName = parcel.readString();
    }

    public void writeToProvider(ContentValues values, long book_fk) {
        AuthorContract.putName(values, this.toString());
        AuthorContract.putBookForeignKey(values, book_fk);
    }

    @Override
    public String toString() {
        return firstName + SEPARATE_KEY + middleInitial + SEPARATE_KEY + lastName;
    }

    public long id;

    public String firstName;
	
	public String middleInitial;
	
	public String lastName;
}
