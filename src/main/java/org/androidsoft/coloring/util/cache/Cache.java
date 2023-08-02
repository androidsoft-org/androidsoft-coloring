package org.androidsoft.coloring.util.cache;

import android.os.Parcelable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

public interface Cache extends Parcelable {
    /* Open a stream if it is available.
     * This means that if the url is available, it is opened.
     * If the Url is not available and nothing is cached, an IOError is raised.
     */
    InputStream openStreamIfAvailable(URL url) throws IOException;
    /* Return a new cache object which caches requests under a given id.
     */
    Cache forId(String id);
    Cache forId(String id, Date lastModified);
    boolean isCached(String id);
}
