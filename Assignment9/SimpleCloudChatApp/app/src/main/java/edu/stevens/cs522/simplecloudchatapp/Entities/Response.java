package edu.stevens.cs522.simplecloudchatapp.Entities;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import edu.stevens.cs522.simplecloudchatapp.Contracts.ChatroomContract;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ClientContract;
import edu.stevens.cs522.simplecloudchatapp.Contracts.MessageContract;

/**
 * Created by wyf920621 on 3/12/15.
 */
public abstract class Response implements Parcelable {
    private final static String TAG = Response.class.getCanonicalName();

    public static enum ResponseType {
        ERROR,
        REGISTER,
        SYNCHRONIZE
    }

    // Human-readable response message
    public String responseMessage = "";

    // HTTP status code
    public int httpResponseCode = 0;

    // HTTP status line message
    public String httpResponseMessage = "";

    public static final Creator<Response> CREATOR = new Creator<Response>() {
        @Override
        public Response createFromParcel(Parcel source) {
            return createResponse(source);
        }

        @Override
        public Response[] newArray(int size) {
            return new Response[size];
        }
    };

    // Parse the json response entity
    protected void parseResponse(JsonReader reader) throws IOException {
    }

    protected static String parseString(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        } else {
            return reader.nextString();
        }
    }

    protected static void matchName(String name, JsonReader reader) throws IOException {
        String label = reader.nextName();
        if (!name.equals(label)) {
            throw new IOException("Error in response entity: expected " + name + ", encountered " + label);
        }
    }

    public Response(HttpURLConnection connection) throws IOException {
        // Use connection.getHeaderField() to get app-specific response headers
        httpResponseCode = connection.getResponseCode();
        httpResponseMessage = connection.getResponseMessage();
    }

    public Response(String responseMessage, int httpResponseCode, String httpResponseMessage) {
        this.responseMessage = responseMessage;
        this.httpResponseCode = httpResponseCode;
        this.httpResponseMessage = httpResponseMessage;
    }

    public Response(Parcel in) {
        responseMessage = in.readString();
        httpResponseCode = in.readInt();
        httpResponseMessage = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(responseMessage);
        dest.writeInt(httpResponseCode);
        dest.writeString(httpResponseMessage);
    }

    public static Response createResponse(Parcel in) {
        ResponseType requestType = ResponseType.valueOf(in.readString());
        switch (requestType) {
            case ERROR:
                Log.v(TAG, "Create Error Response");
                return new ErrorResponse(in);
            case REGISTER:
                Log.v(TAG, "Create Register Response");
                return new RegisterResponse(in);
        }
        throw new IllegalArgumentException("Unknown request type: " + requestType.name());
    }

    public abstract boolean isValid();


    /* ErrorResponse */
    public static class ErrorResponse extends Response implements Parcelable {
        public enum Status {
            NETWORK_UNAVAILABLE,
            SERVER_ERROR,
            SYSTEM_ERROR,
            APPLICATION_ERROR
        }
        public Status status;

        public ErrorResponse(int responseCode, Status status, String message) {
            this(responseCode, status, message, "");
            this.status = status;
        }

        public ErrorResponse(int responseCode, Status status, String message, String httpMessage) {
            super(message, responseCode, httpMessage);
            this.status = status;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(ResponseType.ERROR.name());
            super.writeToParcel(dest, flags);
            dest.writeString(status.name());
        }

        public ErrorResponse(Parcel in) {
            super(in);
            this.status = Status.valueOf(in.readString());
        }

        public static final Creator<ErrorResponse> CREATOR = new Creator<ErrorResponse>() {
            @Override
            public ErrorResponse createFromParcel(Parcel source) {
                source.readString();
                return new ErrorResponse(source);
            }

            @Override
            public ErrorResponse[] newArray(int size) {
                return new ErrorResponse[size];
            }
        };
    }

    public static class RegisterResponse extends Response implements Parcelable {
        public long id;
        public RegisterResponse(HttpURLConnection connection, JsonReader reader) throws IOException {
            super(connection);
            parseResponse(reader);
        }

        @Override
        public boolean isValid() {
            return this.id > 0;
        }

        @Override
        protected void parseResponse(JsonReader reader) throws IOException {
            reader.beginObject();
            matchName("id", reader);
            this.id = reader.nextInt();
            reader.endObject();
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeString(ResponseType.REGISTER.name());
            super.writeToParcel(out, flags);
            out.writeLong(id);
        }

        public RegisterResponse(Parcel in) {
            super(in);
            id = in.readInt();
        }

        public static final Creator<RegisterResponse> CREATOR = new Creator<RegisterResponse>() {
            @Override
            public RegisterResponse createFromParcel(Parcel source) {
                source.readString();
                return new RegisterResponse(source);
            }

            @Override
            public RegisterResponse[] newArray(int size) {
                return new RegisterResponse[size];
            }
        };
    }

    public static class SyncResponse extends Response implements Parcelable {
        public ArrayList<ContentValues> messageValues = null;
        public ArrayList<ContentValues> clientValues = null;
        public SyncResponse(HttpURLConnection connection, JsonReader reader) throws IOException {
            super(connection);
            parseResponse(reader);
        }

        @Override
        public boolean isValid() {
            return clientValues != null;
        }

        @Override
        protected void parseResponse(JsonReader reader) throws IOException {
            messageValues = new ArrayList<>();
            clientValues = new ArrayList<>();
            reader.beginObject();
            matchName("clients", reader);
            reader.beginArray();
            while (reader.peek() != JsonToken.END_ARRAY) {
                reader.beginObject();
                String clientName = null;
                double clientLatitude = 0;
                double clientLongitude = 0;
                while (reader.peek() != JsonToken.END_OBJECT) {
                    String label = reader.nextName();
                    if (label.equals("sender")) {
                        clientName = reader.nextString();
                    } else if (label.equals("latitude")) {
                        clientLatitude = reader.nextDouble();
                    } else if (label.equals("longitude")) {
                        clientLongitude = reader.nextDouble();
                    } else {
                        reader.skipValue();
                    }
                }
                ContentValues values = new ContentValues();
                values.put(ClientContract.NAME, clientName);
                values.put(ClientContract.LATITUDE, clientLatitude);
                values.put(ClientContract.LONGITUDE, clientLongitude);
                clientValues.add(values);
                reader.endObject();
            }
            reader.endArray();

            while (reader.peek() != JsonToken.END_OBJECT) {
                if ("messages".equals(reader.nextName())) {
                    reader.beginArray();
                    while (reader.peek() != JsonToken.END_ARRAY) {
                        ContentValues values = new ContentValues();
                        reader.beginObject();
                        while (reader.peek() != JsonToken.END_OBJECT) {
                            String label = reader.nextName();
                            if (label.equals(ChatroomContract.NAME)) {
                                values.put(ChatroomContract.NAME, reader.nextString());
                            }
                            else if (label.equals(MessageContract.TIMESTAMP)) {
                                long timestampValue = reader.nextLong();
                                values.put(MessageContract.TIMESTAMP, timestampValue);
                            }
                            else if (label.equals(MessageContract.SEQNUM)) {
                                values.put(MessageContract.SEQNUM, reader.nextLong());
                            }
                            else if (label.equals("sender")) {
                                values.put(ClientContract.NAME, reader.nextString());
                            }
                            else if (label.equals(MessageContract.MESSAGE_TEXT)) {
                                values.put(MessageContract.MESSAGE_TEXT, reader.nextString());
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                        messageValues.add(values);
                    }
                    reader.endArray();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(ResponseType.SYNCHRONIZE.name());
            super.writeToParcel(out, flags);
            out.writeTypedList(messageValues);
            out.writeTypedList(clientValues);
        }

        @SuppressWarnings("unchecked")
        public SyncResponse(Parcel in) {
            super(in);
            this.messageValues = in.readArrayList(ContentValues.class.getClassLoader());
            this.clientValues = in.readArrayList(ContentValues.class.getClassLoader());
        }

        public static final Creator<SyncResponse> CREATOR = new Creator<SyncResponse>() {
            @Override
            public SyncResponse createFromParcel(Parcel source) {
                return new SyncResponse(source);
            }

            @Override
            public SyncResponse[] newArray(int size) {
                return new SyncResponse[size];
            }
        };
    }
}
