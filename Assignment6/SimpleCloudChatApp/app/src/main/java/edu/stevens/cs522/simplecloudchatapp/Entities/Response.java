package edu.stevens.cs522.simplecloudchatapp.Entities;

import android.os.Parcelable;

/**
 * Created by wyf920621 on 3/12/15.
 */
public abstract class Response implements Parcelable {
    public abstract boolean isValid();
}
