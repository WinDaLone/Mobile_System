package edu.stevens.cs522.simplecloudchatapp.Entities;

import android.net.Uri;
import android.os.Parcel;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class Register extends Request {
    public static final String TAG = Register.class.getCanonicalName();
    private String host;
    private int port;
    private String clientName;

    public static final Creator<Register> CREATOR = new Creator<Register>() {
        @Override
        public Register createFromParcel(Parcel source) {
            return new Register(source);
        }

        @Override
        public Register[] newArray(int size) {
            return new Register[size];
        }
    };


    public Register(String host, int port, String clientName, long clientId) {
        this.host = host;
        this.port = port;
        this.clientName = clientName;
        this.clientID = clientId;
    }

    public Register(Parcel parcel) {
        this.host = parcel.readString();
        this.port = parcel.readInt();
        this.clientName = parcel.readString();
        this.clientID = parcel.readLong();
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
        String RequestString = "http://" + host + ":" + String.valueOf(port) + "/chat?";
        try {
            RequestString += "username=" + URLEncoder.encode(clientName, encoding);
            RequestString += "&regid=" + URLEncoder.encode(String.valueOf(clientID), encoding);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        }
        return Uri.parse(RequestString);
    }

    @Override
    public String getRequestEntity() throws IOException {
        return null;
    }

    @Override
    public Response getResponse(HttpURLConnection connection, JsonReader reader) {
        return new RegisterResponse(reader);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.host);
        parcel.writeInt(this.port);
        parcel.writeString(this.clientName);
        parcel.writeLong(this.clientID);
    }
}
