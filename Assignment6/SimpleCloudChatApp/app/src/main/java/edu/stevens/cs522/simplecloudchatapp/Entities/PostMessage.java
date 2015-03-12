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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class PostMessage extends Request {
    public static final String TAG = PostMessage.class.getCanonicalName();
    private static final int UUIDFlag = 1;

    private String host;
    private int port;
    private String chatroom;
    private String timestamp;
    private String text;

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

    public PostMessage (String host, int port, UUID registrationID, long clientID, String chatroom, String timestamp, String text) {
        this.host = host;
        this.port = port;
        this.registrationID = registrationID;
        this.clientID = clientID;
        this.chatroom = chatroom;
        this.timestamp = timestamp;
        this.text = text;
    }

    public PostMessage (Parcel parcel) {
        this.host = parcel.readString();
        this.port = parcel.readInt();
        ParcelUuid parcelUuid = parcel.readParcelable(ParcelUuid.class.getClassLoader());
        this.registrationID = parcelUuid.getUuid();
        this.clientID = parcel.readLong();
        this.chatroom = parcel.readString();
        this.timestamp = parcel.readString();
        this.text = parcel.readString();
    }
    @Override
    public Map<String, String> getRequestHeaders() {
        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("Content-Type", "application/json");
        stringMap.put("X-latitude", "40.7439905");
        stringMap.put("X-longitude", "-74.0323626");
        return stringMap;
    }

    @Override
    public Uri getRequestUri() {
        String requestString = "http://" + host + ":" + String.valueOf(port) + "/chat/" +
                String.valueOf(registrationID.getMostSignificantBits()) + String.valueOf(registrationID.getLeastSignificantBits()) +
                "?";
        try {
            requestString += "regid=" + URLEncoder.encode(String.valueOf(clientID), encoding);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        }
        return Uri.parse(requestString);
    }

    /* Json Part */
    @Override
    public String getRequestEntity() throws IOException {
        String entity = "";
        entity += "{ \"chatroom\" : \"" + chatroom +"\", \"timestamp\" : " + timestamp + ", \"text\" : \"" + text + "\" }";
        return entity;
    }

    @Override
    public Response getResponse(HttpURLConnection connection, JsonReader reader) {
        return new PostMessageResponse(reader);
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
        parcel.writeParcelable(parcelUuid, UUIDFlag);
        parcel.writeLong(this.clientID);
        parcel.writeString(this.chatroom);
        parcel.writeString(this.timestamp);
        parcel.writeString(this.text);
    }
}
