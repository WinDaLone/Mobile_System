package edu.stevens.cs522.simplecloudchatapp.Entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import java.util.UUID;

import edu.stevens.cs522.simplecloudchatapp.Contracts.ClientContract;

/**
 * Created by wyf920621 on 4/6/15.
 */
// TODO
public class Client implements Parcelable {
    public long id;
    public String name;
    public UUID uuid;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        ParcelUuid parcelUuid = new ParcelUuid(uuid);
        dest.writeParcelable(parcelUuid, flags);
    }

    public Client(long id, String name, UUID uuid) {
        this.id = id;
        this.name = name;
        this.uuid = uuid;
    }

    public Client(String name, UUID uuid) {
        this.id = 0;
        this.name = name;
        this.uuid = uuid;
    }

    public Client(String name) {
        this.id = 0;
        this.name = name;
        this.uuid = null;
    }

    public Client(Parcel parcel) {
        this.id = parcel.readLong();
        this.name = parcel.readString();
        ParcelUuid parcelUuid = parcel.readParcelable(ParcelUuid.class.getClassLoader());
        this.uuid = parcelUuid.getUuid();
    }

    public static final Creator<Client> CREATOR = new Creator<Client>() {
        @Override
        public Client createFromParcel(Parcel source) {
            return new Client(source);
        }

        @Override
        public Client[] newArray(int size) {
            return new Client[size];
        }
    };

    public void writeToProvider(ContentValues values) {
        ClientContract.setClientId(values, id);
        ClientContract.setName(values, name);
        ClientContract.setUuid(values, uuid);
    }

    public Client(Cursor cursor) {
        this.id = ClientContract.getClientId(cursor);
        this.name = ClientContract.getName(cursor);
        this.uuid = ClientContract.getUUID(cursor);
    }
}
