package edu.stevens.cs522.simplecloudchatapp.Entities;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import java.util.UUID;

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

    public Client(Parcel parcel) {
        this.id = parcel.readLong();
        this.name = parcel.readString();
        ParcelUuid parcelUuid = parcel.readParcelable(ParcelUuid.class.getClassLoader());
        this.uuid = parcelUuid.getUuid();
    }
}
