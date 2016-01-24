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

/**
 * Created by wyf920621 on 4/29/15.
 */
public class Unregister extends Request {
    public static final String TAG = Unregister.class.getCanonicalName();
    public String host;
    public int port;

    public static final Creator<Unregister> CREATOR = new Creator<Unregister>() {
        @Override
        public Unregister createFromParcel(Parcel source) {
            return new Unregister(source);
        }

        @Override
        public Unregister[] newArray(int size) {
            return new Unregister[0];
        }
    };

    public Unregister(Parcel parcel) {
        this.host = parcel.readString();
        this.port = parcel.readInt();
        ParcelUuid parcelUuid = parcel.readParcelable(ParcelUuid.class.getClassLoader());
        this.registrationID = parcelUuid.getUuid();
        this.clientID = parcel.readLong();
        this.latitude = parcel.readDouble();
        this.longitude = parcel.readDouble();
    }

    public Unregister(String host, int port, Client client) {
        this.host = host;
        this.port = port;
        this.registrationID = client.uuid;
        this.clientID = client.id;
        this.latitude = client.latitude;
        this.longitude = client.longitude;
    }

    @Override
    public Uri getRequestUri() {
        String requestString = "http://" + host + ":" + String.valueOf(port) + "/chat/" +
                String.valueOf(clientID) +
                "?";
        try {
            requestString += "regid=" + URLEncoder.encode(registrationID.toString(), encoding);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        }
        Log.v(TAG, "Unregister Request URI: " + requestString);
        return Uri.parse(requestString);
    }

    @Override
    public Response getResponse(HttpURLConnection connection, JsonReader reader) throws IOException {
        return null;
    }

    @Override
    public String getRequestEntity() throws IOException {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(host);
        dest.writeInt(port);
        ParcelUuid parcelUuid = new ParcelUuid(registrationID);
        dest.writeParcelable(parcelUuid, flags);
        dest.writeLong(clientID);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
