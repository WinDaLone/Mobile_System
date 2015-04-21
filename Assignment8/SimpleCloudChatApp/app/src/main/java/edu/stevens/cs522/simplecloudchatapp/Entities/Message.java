package edu.stevens.cs522.simplecloudchatapp.Entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import java.sql.Timestamp;

import edu.stevens.cs522.simplecloudchatapp.Contracts.ChatroomContract;
import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class Message implements Parcelable{
    public long chatroom_fk;
    public String chatroom;
    public long messageID;
    public String messageText;
    public Timestamp timestamp;
    public long seqnum;
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

    public Message() {
        this.chatroom_fk = 0;
        this.chatroom = "";
        this.messageID = 0;
        this.messageText = "";
        this.timestamp = null;
        this.seqnum = 0;
        this.senderID = 0;
    }

    public Message(String messageText, Timestamp timestamp) {
        this.chatroom_fk = 0;
        this.chatroom = "";
        this.messageID = 0;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.seqnum = 0;
        this.senderID = 0;
    }

    public Message(Parcel parcel) {
        this.chatroom_fk = parcel.readLong();
        this.chatroom = parcel.readString();
        this.messageID = parcel.readLong();
        this.messageText = parcel.readString();
        this.timestamp = new Timestamp(parcel.readLong());
        this.seqnum = parcel.readLong();
        this.senderID = parcel.readLong();
    }

    public Message(Cursor cursor) {
        this.chatroom_fk = MessageContract.getChatroomId(cursor);
        this.chatroom = ChatroomContract.getName(cursor);
        this.messageID = MessageContract.getMessageId(cursor);
        this.messageText = MessageContract.getMessageText(cursor);
        this.timestamp = MessageContract.getTimestamp(cursor);
        this.seqnum = MessageContract.getSeqnum(cursor);
        this.senderID = MessageContract.getSenderId(cursor);
    }

    public void writeToProvider(ContentValues values, long senderID, long chatroom_fk) {
        this.chatroom_fk = chatroom_fk;
        MessageContract.setChatroomId(values, chatroom_fk);
        MessageContract.setMessageText(values, messageText);
        MessageContract.setTimestamp(values, timestamp);
        MessageContract.setSeqnum(values, seqnum);
        this.senderID = senderID;
        MessageContract.setSenderId(values, senderID);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(chatroom_fk);
        parcel.writeString(chatroom);
        parcel.writeLong(messageID);
        parcel.writeString(messageText);
        parcel.writeLong(timestamp.getTime());
        parcel.writeLong(seqnum);
        parcel.writeLong(senderID);
    }
}
