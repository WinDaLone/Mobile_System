package edu.stevens.cs522.simplecloudchatapp.Entities;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import java.sql.Timestamp;

import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class Message implements Parcelable{
    public long messageID;
    public String messageText;
    public Timestamp timestamp;
    public long senderID;

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public Message(String messageText, Timestamp timestamp, long senderID) {
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.senderID = senderID;
    }

    public Message(Parcel parcel) {
        this.messageID = parcel.readLong();
        this.messageText = parcel.readString();
        this.timestamp = new Timestamp(parcel.readLong());
        this.senderID = parcel.readLong();
    }

    public void writeToProvider(ContentValues values) {
        MessageContract.setMessageId(values, messageID);
        MessageContract.setMessageText(values, messageText);
        MessageContract.setTimestamp(values, timestamp);
        MessageContract.setSenderId(values, senderID);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(messageID);
        parcel.writeString(messageText);
        parcel.writeLong(timestamp.getTime());
        parcel.writeLong(senderID);
    }
}
