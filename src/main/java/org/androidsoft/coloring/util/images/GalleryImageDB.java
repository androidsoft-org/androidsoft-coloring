package org.androidsoft.coloring.util.images;

import android.os.Handler;
import android.os.Looper;

import org.androidsoft.coloring.util.cache.Cache;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.TimeZone;

public class GalleryImageDB extends Subject implements ImageDB, Runnable {

    private ImageDB db;
    /* This the gallery Jekyll date format.
     * 	"2020-03-10T22:16:41+01:00"
     * see https://stackoverflow.com/a/4216767/1320237
     * see https://learn.cloudcannon.com/jekyll-cheat-sheet/
     * see https://stackoverflow.com/a/3914498/1320237
     */
    //  public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH);

    public static Date parseTimeStamp(String dateString) throws NumberFormatException {
        // using the DATE_FORMAT did not work
        int year = parseInt(dateString, 0, 4, 1000, 3000);
        int month = parseInt(dateString, 5, 2, 1, 13) - 1;
        int day = parseInt(dateString, 8, 2, 1, 32);
        int hour = parseInt(dateString, 11, 2, 0, 24);
        int minute = parseInt(dateString, 14, 2, 0, 60);
        int second = parseInt(dateString, 17, 2, 1, 13);
        String tzSign = dateString.substring(19, 20);
        String tz = dateString.substring(19);
        int tzHour = parseInt(dateString, 20, 2, 0, 13);
        int tzMinute = parseInt(dateString, 23, 2, 0, 60);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour - 1);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        //calendar.set(Calendar.ZONE_OFFSET, (tzSign.equals("+") ?  1 : -1) * (tzHour * 60 + tzMinute) * 1000);
        //String tz = (tzSign.equals("+") ?  tzHour : 24 - tzHour) + ":" + (tzMinute < 10 ? "0" : "") + tzMinute;
        calendar.setTimeZone(TimeZone.getTimeZone(tz));
        return calendar.getTime();
    }

    /* Parse a string into a number, using max (exclusive) and min (inclusive) values
     *
     */
    private static int parseInt(String dateString, int index, int length, int min, int max) {
        String s = dateString.substring(index, index + length);
        return Integer.parseInt(s);
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
        Cache cache = retrievalOptions.getCache();
        try {
            JSONObject lastModification = getJSONFrom(getUrl("latest-modification.json"), cache);
            if (lastModification == null) {
                return;
            }
            String lastModifiedString = lastModification.getString("last-modified");
            Date lastModified = parseTimeStamp(lastModifiedString);
            String url = getUrl("images.json");
            JSONObject json = getJSONFrom(url, cache.forId(url, lastModified));
            if (json == null) {
                return;
            }
            final JoinedImageDB db = new JoinedImageDB();
            JSONArray images = json.getJSONArray("images");
            for (int i = 0; i < images.length(); i++) {
                JSONObject imageJSON = images.getJSONObject(i);
                String id = imageJSON.getString("id");
                String path = imageJSON.getString("path");
                lastModifiedString = imageJSON.getString("last-modified");
                URL imageUrl = new URL(getUrl(path));
                UrlImageWithPreview image = new UrlImageWithPreview(imageUrl, id, parseDate(lastModifiedString), retrievalOptions);
                JSONArray thumbs = imageJSON.getJSONArray("thumbnails");
                for (int j = 0; j < thumbs.length(); j++) {
                    JSONObject thumbJSON = thumbs.getJSONObject(j);
                    String thumbPath = thumbJSON.getString("path");
                    int thumbMaxWidth = thumbJSON.getInt("max-width");
                    String thumbLastModified = imageJSON.getString("last-modified");
                    String thumbId = thumbJSON.getString("id");
                    URL thumbUrl = new URL(getUrl(thumbPath));
                    ThumbNailImage thumb = new ThumbNailImage(thumbUrl, thumbId, parseDate(thumbLastModified), thumbMaxWidth, retrievalOptions);
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
        } catch (JSONException | ParseException | MalformedURLException e) {
            code = CODE.ERROR.JSON;
            e.printStackTrace();
            return;
        } catch (IOException e) {
            code = CODE.ERROR.DECODE;
            e.printStackTrace();
            return;
        }
    }

    private JSONObject getJSONFrom(String urlString, Cache cache) throws IOException, JSONException {
        InputStream stream;
        try {
            URL url = new URL(urlString);
            stream = cache.openStreamIfAvailable(url);
        } catch (IOException e) {
            e.printStackTrace();
            code = CODE.ERROR.URL;
            return null;
        }
        return getJSONFrom(stream);
    }

    private JSONObject getJSONFrom(InputStream stream) throws JSONException, IOException {
        byte[] rawBytesFromTheSource = IOUtils.toByteArray(stream);
        String data = new String(rawBytesFromTheSource, "UTF-8");
        JSONObject json = new JSONObject(data);
        if (!checkVersionIsCompatible(json)) {
            return null;
        }
        return json;
    }

    private boolean checkVersionIsCompatible(JSONObject json) throws JSONException {
        String version = json.getString(JSON_VERSION);
        String[] codes = version.split("\\.");
        if (codes.length < 2) {
            code = CODE.ERROR.VERSION_INVALID;
            return false;
        }
        if (Integer.parseInt(codes[0]) != VERSION_MAJOR || Integer.parseInt(codes[1]) < VERSION_MINOR) {
            code = CODE.VERSION_INCOMPATIBLE;
            return false;
        }
        return true;
    }

    private Date parseDate(String lastModified) throws ParseException {
        try {
            return parseTimeStamp(lastModified);
        } catch (Exception e){
            retrievalOptions.getErrorReporter().report(e);
            throw e;
        }
    }

    private String getUrl(String path) {
        return getUrl() + (getUrl().endsWith("/") ? "" : "/" ) + path;
    }
}
