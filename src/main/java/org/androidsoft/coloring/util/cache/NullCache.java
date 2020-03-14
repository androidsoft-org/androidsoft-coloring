package org.androidsoft.coloring.util.cache;

import android.os.Parcel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/* This class implements the cache interface but actually caches nothing.
 *
 */
public class NullCache implements Cache {
    @Override
    public InputStream openStreamIfAvailable(URL url) throws IOException {
        return url.openStream();
    }

    @Override
    public Cache forId(String id) {
        return this;
    }

    @Override
    public Cache forId(String id, Date lastModified) {
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    public static Creator CREATOR = new Creator() {
        @Override
        public Cache createFromParcel(Parcel parcel) {
            return new NullCache();
        }

        @Override
        public Cache[] newArray(int i) {
            return new Cache[0];
        }
    };
}
