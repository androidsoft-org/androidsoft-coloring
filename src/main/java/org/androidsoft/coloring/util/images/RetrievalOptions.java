package org.androidsoft.coloring.util.images;

import android.os.Parcel;
import android.os.Parcelable;

import org.androidsoft.coloring.util.errors.ErrorReporter;
import org.androidsoft.coloring.util.errors.LoggingErrorReporter;
import org.androidsoft.coloring.util.errors.UIErrorReporter;
import org.androidsoft.coloring.util.cache.Cache;

/* This object represents the options of how remote urls should be retrieved.
 *
 */
public class RetrievalOptions implements Parcelable {
    private final Cache cache;
    private final boolean networkIsConnected;
    private ErrorReporter errorReporter = new LoggingErrorReporter();

    public RetrievalOptions(Cache cache, boolean networkIsConnected) {
        this.cache = cache;
        this.networkIsConnected = networkIsConnected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(cache, flags);
        parcel.writeInt(networkIsConnected ? 1 : 0);
    }

    public static Creator CREATOR = new Creator() {
        @Override
        public RetrievalOptions createFromParcel(Parcel parcel) {
            Cache cache = parcel.readParcelable(Cache.class.getClassLoader());
            boolean networkIsConnected = parcel.readInt() != 0;
            return new RetrievalOptions(cache, networkIsConnected);
        }

        @Override
        public RetrievalOptions[] newArray(int i) {
            return new RetrievalOptions[0];
        }
    };

    public Cache getCache() {
        return cache;
    }

    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }

    public void setErrorReporter(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    public boolean networkIsConnected() {
        return networkIsConnected;
    }
}
