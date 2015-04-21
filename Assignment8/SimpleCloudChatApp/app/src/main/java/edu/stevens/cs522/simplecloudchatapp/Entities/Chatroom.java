package edu.stevens.cs522.simplecloudchatapp.Entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import edu.stevens.cs522.simplecloudchatapp.Contracts.ChatroomContract;

/**
 * Created by wyf920621 on 4/20/15.
 */
public class Chatroom implements Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
    }

    public static final Creator<Chatroom> CREATOR = new Creator<Chatroom>() {
        @Override
        public Chatroom createFromParcel(Parcel source) {
            return new Chatroom(source);
        }

        @Override
        public Chatroom[] newArray(int size) {
            return new Chatroom[size];
        }
    };

    public long id;
    public String name;

    public Chatroom(String name) {
        this.name = name;
    }

    public Chatroom(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Chatroom(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
    }

    public Chatroom(Cursor cursor) {
        this.id = ChatroomContract.getId(cursor);
        this.name = ChatroomContract.getName(cursor);
    }

    public void writeToProvider(ContentValues values) {
        ChatroomContract.setName(values, this.name);
    }
}
