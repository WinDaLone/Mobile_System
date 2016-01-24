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
import java.util.UUID;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class Register extends Request {
    public static final String TAG = Register.class.getCanonicalName();
    public String host;
    public int port;
    public String clientName;

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


    public Register(String host, int port, String name, UUID uuid, double latitude, double longitude) {
        this.host = host;
        this.port = port;
        this.clientName = name;
        this.clientID = 0;
        this.registrationID = uuid;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Register(Parcel parcel) {
        this.host = parcel.readString();
        this.port = parcel.readInt();
        this.clientName = parcel.readString();
        this.clientID = parcel.readLong();
        ParcelUuid parcelUuid = parcel.readParcelable(ParcelUuid.class.getClassLoader());
        this.registrationID = parcelUuid.getUuid();
        this.latitude = parcel.readDouble();
        this.longitude = parcel.readDouble();
    }


    @Override
    public Uri getRequestUri() {
        String RequestString = "http://" + host + ":" + String.valueOf(port) + "/chat?";
        try {
            RequestString += "username=" + URLEncoder.encode(clientName, encoding);
            RequestString += "&regid=" + URLEncoder.encode(String.valueOf(registrationID.toString()), encoding);
            Log.v(TAG, "Register Request URI: " + RequestString);
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
    public Response getResponse(HttpURLConnection connection, JsonReader reader) throws IOException{
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
        parcel.writeString(this.clientName);
        parcel.writeLong(this.clientID);
        ParcelUuid parcelUuid = new ParcelUuid(registrationID);
        parcel.writeParcelable(parcelUuid, flags);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
    }
}
