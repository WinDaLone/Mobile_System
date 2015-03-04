package edu.stevens.cs522.chatapp.singleprocess.Entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

import edu.stevens.cs522.chatapp.singleprocess.Contracts.PeerContract;

/**
 * Created by wyf920621 on 3/3/15.
 */
public class Peer implements Parcelable {
    public long id;
    public String name;
    public InetAddress address;
    public int port;

    public static final Creator<Peer> CREATOR = new Creator<Peer>() {
        public Peer createFromParcel(Parcel parcel) {
            return new Peer(parcel);
        }

        public Peer[] newArray(int i) {
            return new Peer[i];
        }
    };
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(name);
        parcel.writeString(address.getHostName());
        parcel.writeInt(port);
    }

    public Peer(String name, InetAddress address, int port) {
        this.id = 0;
        this.name = name;
        this.address = address;
        this.port = port;
    }
    public Peer(Parcel parcel) {
        this.id = parcel.readLong();
        this.name = parcel.readString();
        String hostName = parcel.readString();
        try {
            address = InetAddress.getByName(hostName);
        }
        catch (UnknownHostException e) {
            Log.e("Peer", e.getMessage());
        }

        this.port = parcel.readInt();
    }

    public Peer(Cursor cursor) {
        this.id = PeerContract.getId(cursor);
        this.name = PeerContract.getName(cursor);
        this.address = PeerContract.getAddress(cursor);
        this.port = PeerContract.getPort(cursor);
    }
    public void writeToProvider(ContentValues values) {
        PeerContract.putName(values, this.name);
        PeerContract.putAddress(values, this.address);
        PeerContract.putPort(values, this.port);
    }
}