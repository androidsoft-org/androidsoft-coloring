package org.androidsoft.coloring.util.images;

import android.net.Uri;
import android.os.Handler;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import eu.quelltext.coloring.R;

public class GalleryImageDB extends Subject implements ImageDB, Runnable {

    private ImageDB db;

    public static class CODE {
        public final static int UNINITIALIZED = 0;
        public static final int STARTED = 1;
        public static final int VERSION_INCOMPATIBLE = 2;
        public static final int SUCCESS = 3;

        public static class ERROR {
            public static final int URL = -1;
            public static final int DECODE = -2;
            public static final int JSON = -3;
            public static final int VERSION_INVALID = -4;
        }
    }

    private static final int VERSION_MAJOR = 1;
    private static final int VERSION_MINOR = 0;
    private static final String JSON_VERSION = "version";
    private static final String JSON_IMAGES = "images";
    private final String url;
    private final Thread thread;
    private int code = CODE.UNINITIALIZED;

    public GalleryImageDB(String url) {
        this.url = url;
        thread = new Thread(this);
        db = new JoinedImageDB();
        start();
    }

    private void start() {
        thread.start();
    }

    public int getDescriptionResourceId() {
        return R.string.settings_galleries_user_defined;
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
            url = new URL(this.url);
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
            String[] codes = version.split(".");
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
                URL imageUrl = new URL(getUrl() + (getUrl().endsWith("/") ? "" : "/" ) + path);
                UrlImageWithPreview image = new UrlImageWithPreview(imageUrl, id, lastModified);
                JSONArray thumbs = imageJSON.getJSONArray("thumbnails");
                for (int j = 0; j < thumbs.length(); j++) {
                    JSONObject thumbJSON = thumbs.getJSONObject(j);
                    String thumbPath = thumbJSON.getString("path");
                    int thumbMaxWidth = thumbJSON.getInt("max-width");
                    String thumbLastModified = imageJSON.getString("last-modified");
                    String thumbId = thumbJSON.getString("id");
                    URL thumbUrl = new URL(getUrl() + (getUrl().endsWith("/") ? "" : "/" ) + thumbPath);
                    ThumbNailImage thumb = new ThumbNailImage(thumbUrl, thumbId, thumbLastModified, thumbMaxWidth);
                    image.addPreviewImage(thumb);
                }
                db.add(image);
            }
            Handler handler = new Handler();
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
}
