package edu.stevens.cs522.simplecloudchatapp.Entities;

import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;

/**
 * Created by wyf920621 on 4/6/15.
 */
// TODO
public class Synchronize extends Request{
    public String host;
    public int port;
    public long seqNum;
    List<Message> messages;

    public Synchronize(String host, int port, UUID registrationID, long clientID, long seqNum, List<Message> messages) {
        this.host = host;
        this.port = port;
        this.seqNum = seqNum;
        this.messages = messages;
        this.registrationID = registrationID;
        this.clientID = clientID;
    }

    @SuppressWarnings("unchecked")
    public Synchronize(Parcel in) {
        this.host = in.readString();
        this.port = in.readInt();
        ParcelUuid parcelUuid = in.readParcelable(ParcelUuid.class.getClassLoader());
        this.registrationID = parcelUuid.getUuid();
        this.clientID = in.readLong();
        this.seqNum = in.readLong();
        this.messages = new ArrayList<>();
        in.readTypedList(this.messages, Message.CREATOR);
    }

    public static final Creator<Synchronize> CREATOR = new Creator<Synchronize>() {
        @Override
        public Synchronize createFromParcel(Parcel source) {
            return new Synchronize(source);
        }

        @Override
        public Synchronize[] newArray(int size) {
            return new Synchronize[size];
        }
    };

    @Override
    // See setJsonWriter
    public String getRequestEntity() throws IOException {
        return null;
    }

    @Override
    // No header
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
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        }
        requestString += "&seqnum=" + String.valueOf(seqNum);
        Log.v(TAG, "Post Synchronize Request URI: " + requestString);
        return Uri.parse(requestString);
    }

    @Override
    public Response getResponse(HttpURLConnection connection, JsonReader reader) throws IOException {
        return new Response.SyncResponse(connection, reader);
    }

    public void setJsonWriter(JsonWriter writer) throws IOException {
        writer.beginArray();
        for (int i = 0; i < messages.size(); i++) {
            writer.beginObject();
            writer.name(MessageContract.CHATROOM);
            writer.value(messages.get(i).chatroom);
            writer.name(MessageContract.TIMESTAMP);
            writer.value(messages.get(i).timestamp.getTime());
            writer.name(MessageContract.MESSAGE_TEXT);
            writer.value(messages.get(i).messageText);
            writer.endObject();
        }
        writer.endArray();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(host);
        out.writeInt(port);
        ParcelUuid parcelUuid = new ParcelUuid(registrationID);
        out.writeParcelable(parcelUuid, Request.UUIDFlag);
        out.writeLong(this.clientID);
        out.writeLong(seqNum);
        out.writeTypedList(messages);
    }

}
