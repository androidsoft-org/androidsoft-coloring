package org.androidsoft.coloring.util.images;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import eu.quelltext.coloring.R;

public class GalleryImageDB extends Subject implements ImageDB, Runnable {

    private ImageDB db;
    /* This the gallery Jekyll date format.
     * 	"2020-03-10T22:16:41+01:00"
     * see https://stackoverflow.com/a/4216767/1320237
     * see https://learn.cloudcannon.com/jekyll-cheat-sheet/
     * see https://stackoverflow.com/a/3914498/1320237
     */
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.ENGLISH);

    public static Calendar parse(String dateString) throws ParseException {
        Date date = DATE_FORMAT.parse(dateString);
        Calendar calendar = Calendar.getInstance();
        // see https://mkyong.com/java/java-date-and-calendar-examples/
        calendar.setTime(date);
        return calendar;
    }

    public static class CODE {
        public final static int UNINITIALIZED = 0;
        public static final int STARTED = 1;
        public static final int VERSION_INCOMPATIBLE = 2;
        public static final int WAITING_FOR_MAIN_THREAD = 3;
        public static final int SUCCESS = 4;

        public static class ERROR {
            public static final int URL = -1;
            public static final int DECODE = -2;
            public static final int JSON = -3;
            public static final int VERSION_INVALID = -4;
        }
    }

    /* This is the version compatibility
     * If the VERSION_MAJOR changes, it is incompatible.
     * If the VERSION_MINOR is lower, it is incompatible.
     */
    private static final int VERSION_MAJOR = 2;
    private static final int VERSION_MINOR = 0;
    private static final String JSON_VERSION = "version";
    private static final String JSON_IMAGES = "images";
    private final String url;
    private final RetrievalOptions retrievalOptions;
    private final Thread thread;
    private int code = CODE.UNINITIALIZED;

    public GalleryImageDB(String url, RetrievalOptions retrievalOptions) {
        this.url = url;
        this.retrievalOptions = retrievalOptions;
        thread = new Thread(this);
        db = new JoinedImageDB();
    }

    public void start() {
        thread.start();
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int size() {
        return db.size();
    }

    @Override
    public Image get(int index) {
        return db.get(index);
    }

    @Override
    public void run() {
        code = CODE.STARTED;
        InputStream stream = null;
        URL url;
        try {
            url = new URL(getUrl("images.json"));
            stream = url.openStream();
        } catch (IOException e) {
            e.printStackTrace();
            code = CODE.ERROR.URL;
            return;
        }
        String data;
        try {
            byte[] rawBytesFromTheSource = IOUtils.toByteArray(stream);
            data = new String(rawBytesFromTheSource, "UTF-8");
        } catch (IOException e) {
            code = CODE.ERROR.DECODE;
            e.printStackTrace();
            return;
        }
        try {
            JSONObject json = new JSONObject(data);
            String version = json.getString(JSON_VERSION);
            String[] codes = version.split("\\.");
            if (codes.length < 2) {
                code = CODE.ERROR.VERSION_INVALID;
                return;
            }
            if (Integer.parseInt(codes[0]) != VERSION_MAJOR || Integer.parseInt(codes[1]) < VERSION_MINOR) {
                code = CODE.VERSION_INCOMPATIBLE;
                return;
            }
            final JoinedImageDB db = new JoinedImageDB();
            JSONArray images = json.getJSONArray(JSON_IMAGES);
            for (int i = 0; i < images.length(); i++) {
                JSONObject imageJSON = images.getJSONObject(i);
                String id = imageJSON.getString("id");
                String path = imageJSON.getString("path");
                String lastModified = imageJSON.getString("last-modified");
                URL imageUrl = new URL(getUrl(path));
                UrlImageWithPreview image = new UrlImageWithPreview(imageUrl, id, lastModified, retrievalOptions);
                JSONArray thumbs = imageJSON.getJSONArray("thumbnails");
                for (int j = 0; j < thumbs.length(); j++) {
                    JSONObject thumbJSON = thumbs.getJSONObject(j);
                    String thumbPath = thumbJSON.getString("path");
                    int thumbMaxWidth = thumbJSON.getInt("max-width");
                    String thumbLastModified = imageJSON.getString("last-modified");
                    String thumbId = thumbJSON.getString("id");
                    URL thumbUrl = new URL(getUrl(thumbPath));
                    ThumbNailImage thumb = new ThumbNailImage(thumbUrl, thumbId, thumbLastModified, thumbMaxWidth, retrievalOptions);
                    image.addPreviewImage(thumb);
                }
                db.add(image);
            }
            // start a handler in the UI thread
            // see https://developer.android.com/training/multiple-threads/communicate-ui
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    code = CODE.SUCCESS;
                    GalleryImageDB.this.db = db;
                    if (db.size() > 0) {
                        notifyObservers();
                    }
                }
            });
            code = CODE.WAITING_FOR_MAIN_THREAD;
        } catch (JSONException e) {
            code = CODE.ERROR.JSON;
            e.printStackTrace();
            return;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            code = CODE.ERROR.JSON;
            return;
        }
    }

    private String getUrl(String path) {
        return getUrl() + (getUrl().endsWith("/") ? "" : "/" ) + path;
    }
}
