package edu.stevens.cs522.simplecloudchatapp.Entities;

import android.os.Parcel;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class PostMessageResponse extends Response {
    public static final String TAG = PostMessageResponse.class.getCanonicalName();

    private long messageID;
    public static final Creator<PostMessageResponse> CREATOR = new Creator<PostMessageResponse>() {
        @Override
        public PostMessageResponse createFromParcel(Parcel source) {
            return new PostMessageResponse(source);
        }

        @Override
        public PostMessageResponse[] newArray(int size) {
            return new PostMessageResponse[size];
        }
    };

    public PostMessageResponse(JsonReader jsonReader) {
        this.parse(jsonReader);
    }

    public PostMessageResponse(Parcel parcel) {
        this.messageID = parcel.readLong();
    }

    private void parse(JsonReader jsonReader) {
        try {
            jsonReader.beginObject();
            while(jsonReader.peek() != JsonToken.END_OBJECT) {
                String label = jsonReader.nextName();
                if ("id".equals(label)) {
                    jsonReader.beginArray();
                    this.messageID = jsonReader.nextLong();
                    jsonReader.endArray();
                    jsonReader.endObject();
                    return;
                }
            }
            jsonReader.endObject();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            this.messageID = 0;
        }

    }
    @Override
    public boolean isValid() {
        return this.messageID != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(messageID);
    }
}
