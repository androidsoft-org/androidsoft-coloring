package org.androidsoft.coloring.util.cache;

import android.os.Parcel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

public class FileCache implements Cache {
    private final File directory;

    public FileCache(File directory) {
        this.directory = directory;
    }

    @Override
    public InputStream openStreamIfAvailable(URL url) throws IOException {
        return forId(url.toString()).openStreamIfAvailable(url);
    }

    @Override
    public FileCacheWithId forId(String id) {
        return new FileCacheWithId(directory, id);
    }

    @Override
    public Cache forId(String id, Date lastModified) {
        FileCacheWithId cache = forId(id);
        cache.invalidateIfOlderThan(lastModified);
        return cache;
    }

    protected File getDirectory() {
        return directory;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(directory.toString());
    }

    public static Creator CREATOR = new Creator() {
        @Override
        public Cache createFromParcel(Parcel parcel) {
            File directory = new File(parcel.readString());
            return new FileCache(directory);
        }

        @Override
        public Cache[] newArray(int i) {
            return new Cache[0];
        }
    };
}
