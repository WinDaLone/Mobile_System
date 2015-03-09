package edu.stevens.cs522.chatapp.separatedprocess.Entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import edu.stevens.cs522.chatapp.separatedprocess.Contracts.MessageContract;

/**
 * Created by wyf920621 on 3/8/15.
 */
public class Message implements Parcelable {
    public long id;
    public String messageText;
    public String sender;


    public static final Creator<Message> CREATOR = new Creator<Message>() {
        public Message createFromParcel(Parcel parcel) {
            return new Message(parcel);
        }

        public Message[] newArray(int i) {
            return new Message[i];
        }
    };
    public int describeContents() { return 0; }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(messageText);
        parcel.writeString(sender);
    }

    public Message(String messageText, String sender) {
        this.id = 0;
        this.messageText = messageText;
        this.sender = sender;
    }

    public Message(Parcel parcel) {
        this.id = parcel.readLong();
        this.messageText = parcel.readString();
        this.sender = parcel.readString();
    }
    public void writeToProvider(ContentValues values, long peer_fk) {
        MessageContract.putMessageText(values, this.messageText);
        MessageContract.putSender(values, this.sender);
        MessageContract.putPeerFk(values, peer_fk);
    }

    public Message(Cursor cursor) {
        this.id = MessageContract.getId(cursor);
        this.messageText = MessageContract.getMessageText(cursor);
        this.sender = MessageContract.getSender(cursor);
    }
}