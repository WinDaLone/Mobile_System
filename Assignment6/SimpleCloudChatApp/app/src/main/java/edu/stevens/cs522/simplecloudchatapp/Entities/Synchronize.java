package edu.stevens.cs522.simplecloudchatapp.Entities;

import android.net.Uri;
import android.os.Parcel;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

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

    public Synchronize(String host, int port, long seqNum, List<Message> messages) {
        this.host = host;
        this.port = port;
        this.seqNum = seqNum;
        this.messages = messages;
    }

    @SuppressWarnings("unchecked")
    public Synchronize(Parcel in) {
        this.host = in.readString();
        this.port = in.readInt();
        this.seqNum = in.readLong();
        this.messages = in.readArrayList(Message.class.getClassLoader());
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
        return null;
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
        writer.setIndent("  ");
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
        out.writeLong(seqNum);
        out.writeTypedList(messages);
    }

}
