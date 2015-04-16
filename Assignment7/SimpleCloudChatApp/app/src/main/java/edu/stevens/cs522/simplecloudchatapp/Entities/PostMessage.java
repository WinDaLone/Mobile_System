package edu.stevens.cs522.simplecloudchatapp.Entities;

import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class PostMessage extends Request {
    public static final String TAG = PostMessage.class.getCanonicalName();

    public long messageID;
    public String host;
    public int port;
    public String chatroom;
    public Timestamp timestamp;
    public String text;

    public static final Creator<PostMessage> CREATOR = new Creator<PostMessage>() {
        @Override
        public PostMessage createFromParcel(Parcel source) {
            return new PostMessage(source);
        }

        @Override
        public PostMessage[] newArray(int size) {
            return new PostMessage[size];
        }
    };

    public PostMessage (String host, int port, UUID registrationID, long clientID, String chatroom, Timestamp timestamp, String text, long messageID) {
        this.host = host;
        this.port = port;
        this.registrationID = registrationID;
        this.clientID = clientID;
        this.chatroom = chatroom;
        this.timestamp = timestamp;
        this.text = text;
        this.messageID = messageID;
    }

    public PostMessage (String host, int port, UUID registrationID, long clientID, String chatroom, Timestamp timestamp, String text) {
        this.host = host;
        this.port = port;
        this.registrationID = registrationID;
        this.clientID = clientID;
        this.chatroom = chatroom;
        this.timestamp = timestamp;
        this.text = text;
        this.messageID = 0;
    }

    public PostMessage (Parcel parcel) {
        this.host = parcel.readString();
        this.port = parcel.readInt();
        ParcelUuid parcelUuid = parcel.readParcelable(ParcelUuid.class.getClassLoader());
        this.registrationID = parcelUuid.getUuid();
        this.clientID = parcel.readLong();
        this.chatroom = parcel.readString();
        this.timestamp = new Timestamp(parcel.readLong());
        this.text = parcel.readString();
        this.messageID = parcel.readLong();
    }
    @Override
    public Map<String, String> getRequestHeaders() {
        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("X-latitude", "40.7439905");
        stringMap.put("X-longitude", "-74.0323626");
        return stringMap;
    }

    @Override
    public Uri getRequestUri() {
        String requestString = "http://" + host + ":" + String.valueOf(port) + "/chat/" +
                String.valueOf(clientID) +
                "?";
        try {
            requestString += "regid=" + URLEncoder.encode(registrationID.toString(), encoding);
            Log.v(TAG, "Post Request URI: " + requestString);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        }
        return Uri.parse(requestString);
    }

    /* Json Part */
    @Override
    public String getRequestEntity() throws IOException {
        String entity = "";
        entity += "{ \"chatroom\" : \"" + chatroom +"\", \"timestamp\" : " + timestamp.getTime() + ", \"text\" : \"" + text + "\" }";
        Log.v(TAG, "Post Request Entity: " + entity);
        return entity;
    }

    @Override
    public Response getResponse(HttpURLConnection connection, JsonReader reader) throws IOException {
        return new Response.RegisterResponse(connection, reader);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.host);
        parcel.writeInt(this.port);
        ParcelUuid parcelUuid = new ParcelUuid(registrationID);
        parcel.writeParcelable(parcelUuid, Request.UUIDFlag);
        parcel.writeLong(this.clientID);
        parcel.writeString(this.chatroom);
        parcel.writeLong(this.timestamp.getTime());
        parcel.writeString(this.text);
        parcel.writeLong(this.messageID);
    }
}
