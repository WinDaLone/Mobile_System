package edu.stevens.cs522.simplecloudchatapp.Entities;

import android.os.Parcel;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;

/**
 * Created by wyf920621 on 3/12/15.
 */
public class RegisterResponse extends Response {
    public static final String TAG = RegisterResponse.class.getCanonicalName();
    public long id;
    public static final Creator<RegisterResponse> CREATOR = new Creator<RegisterResponse>() {
        @Override
        public RegisterResponse createFromParcel(Parcel source) {
            return new RegisterResponse(source);
        }

        @Override
        public RegisterResponse[] newArray(int size) {
            return new RegisterResponse[size];
        }
    };

    public RegisterResponse(JsonReader jsonReader) {
        this.parse(jsonReader);
    }

    public RegisterResponse(Parcel parcel) {
        this.id = parcel.readLong();
    }

    private void parse(JsonReader jsonReader) {
        try {
            jsonReader.beginObject();
            while (jsonReader.peek() != JsonToken.END_OBJECT) {
                String label = jsonReader.nextName();
                if ("id".equals(label)) {
                    jsonReader.beginArray();
                    this.id = jsonReader.nextLong();
                    jsonReader.endArray();
                    jsonReader.endObject();
                    return;
                }
            }
            jsonReader.endObject();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            this.id = 0;
        }
    }
    @Override
    public boolean isValid() {
        return this.id != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(id);
    }
}
