package org.androidsoft.coloring.util.cache;

import android.os.Parcel;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import static org.androidsoft.coloring.util.cache.SimulateOfflineMode.SIMULATE_OFFLINE_MODE;

/* This caches requests to a url under a given id.
 *
 */
public class FileCacheWithId extends FileCache {
    private static final String[] REMOVE_START = new String[]{"https://", "http://"};
    private final String id;
    private final boolean isCached;
    private Date lastModified = null;

    public FileCacheWithId(File directory, String id) {
        super(directory);
        this.id = makeIdReadyForDirectory(id);
        isCached = getPath().isFile();
    }

    public static String makeIdReadyForDirectory(String id) {
        id = id.endsWith("/") ? id + "index" : id;
        for (String start: REMOVE_START) {
            if (id.toLowerCase().startsWith(start)) {
                id = id.substring(start.length());
            }
        }
        id = id.replaceAll("//+", "/");
        id = id.replaceAll("^/+", "");
        return id;
    }

    /* Open a URL and cache it.
     * 1. If the file is available and up to date, return the file
     * 2. If the file is available and not up to date and the url is available, return the url
     * 3. If the file is available and not up to date and the url is not available, return the file
     * 4. If the file is not available, and the url is available, return the url
     * 5. If the file is not available and the url is not available, throw an IOException.
     */
    @Override
    public InputStream openStreamIfAvailable(URL url) throws IOException {
        File path = getPath();
        if (path.isFile()) {
            /* case 1, 2, 3 */
            boolean upToDate = isUpToDate(path);
            if (upToDate) {
                /* case 1 */
                try {
                    // return a stream from file
                    // see https://www.baeldung.com/convert-file-to-input-stream
                    return new FileInputStream(path);
                } catch (IOException e) {
                    /* case 4, 5 */
                    e.printStackTrace(); // This should not happen.
                    return retrieveFromUrlToFile(path, url);
                }
            } else {
                try {
                    /* case 2 */
                    // return a stream from file
                    // see https://www.baeldung.com/convert-file-to-input-stream
                    return retrieveFromUrlToFile(path, url);
                } catch (IOException e) {
                    /* case 3, 5 */
                    return new FileInputStream(path);
                }
            }
        } else {
            /* case 4, 5 */
            return retrieveFromUrlToFile(path, url);
        }
    }

    /* Check the update status.
     * The lastModified of the file is rounded to seconds.
     */
    private boolean isUpToDate(File path) {
        if (lastModified == null) {
            return false;
        }
        long pathLastModified = path.lastModified() / 1000;
        long urlLastModified = lastModified.getTime() / 1000;
        return pathLastModified >= urlLastModified;
    }

    private InputStream retrieveFromUrlToFile(File path, URL url) throws IOException {
        if (SIMULATE_OFFLINE_MODE) {
            throw new IOException("test the offline capabilities");
        }
        new File(path.getParent()).mkdirs();
        InputStream source = url.openStream();
        FileOutputStream destination = new FileOutputStream(path);
        IOUtils.copy(source, destination);
        if (lastModified != null) {
            // set the time if available
            path.setLastModified(lastModified.getTime());
        }
        return new FileInputStream(path);
    }

    public String getId() {
        return id;
    }

    protected File getPath() {
        return new File(getDirectory(), getId());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getDirectory().toString());
        parcel.writeString(getId());
    }

    public static Creator CREATOR = new Creator() {
        @Override
        public Cache createFromParcel(Parcel parcel) {
            File directory = new File(parcel.readString());
            String id = parcel.readString();
            return new FileCacheWithId(directory, id);
        }

        @Override
        public Cache[] newArray(int i) {
            return new Cache[0];
        }
    };

    public void invalidateIfOlderThan(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isCached() {
        return isCached;
    }
}
