package org.androidsoft.coloring.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.androidsoft.coloring.util.cache.Cache;
import org.androidsoft.coloring.util.cache.FileCache;
import org.androidsoft.coloring.util.cache.NullCache;
import org.androidsoft.coloring.util.errors.UIErrorReporter;
import org.androidsoft.coloring.util.images.GalleryImageDB;
import org.androidsoft.coloring.util.images.RetrievalOptions;
import org.androidsoft.coloring.util.images.SettingsImageDB;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

import eu.quelltext.coloring.R;

import static org.androidsoft.coloring.util.cache.SimulateOfflineMode.SIMULATE_OFFLINE_MODE;

public class Settings {
    public static final Gallery[] DEFAULT_GALLERIES = new Gallery[]{
            new Gallery("https://gallery.quelltext.eu", R.string.settings_gallery_quelltext),
            new Gallery("http://gallery.quelltext.eu", R.string.settings_gallery_quelltext_http),
    };
    private static final String KEY_SETTINGS = "settings";
    private static final String URL_CACHE_DIRECTORY = "urls";
    private final Context context;
    private final SharedPreferences preferences;
    private SharedPreferences.Editor editor = null;
    // also see SimulateOfflineMode

    public Settings(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(KEY_SETTINGS, context.MODE_PRIVATE);
    }

    public static Settings of(Context context) {
        return new Settings(context);
    }

    public SettingsImageDB getImageDB() {
        SettingsImageDB db = new SettingsImageDB(this);
        return db;
    }

    public Context getContext() {
        return context;
    }

    public void setStringArray(String key, String[] array) {
        JSONArray json = new JSONArray();
        for (String item : array) {
            json.put(item);
        }
        getEditor().putString(key, json.toString());
    }

    public String[] getStringArray(String key) {
        String data = preferences.getString(key, null);
        if (data == null) {
            return null;
        }
        try {
            JSONArray json = new JSONArray(data);
            String[] result = new String[json.length()];
            for (int i = 0; i < json.length(); i++) {
                result[i] = json.getString(i);
            }
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void save() {
        editor.apply();
        editor = null;
    }

    private SharedPreferences.Editor getEditor() {
        if (editor == null) {
            editor = preferences.edit();
        }
        return editor;
    }

    public RetrievalOptions getRetrievalOptions() {
        File directory = new File(context.getCacheDir(), URL_CACHE_DIRECTORY);
        Cache cache = new FileCache(directory);
        boolean networkIsConnected = hasNetworkConnection();
        //cache = new NullCache();
        RetrievalOptions options = new RetrievalOptions(cache, networkIsConnected);
        options.setErrorReporter(UIErrorReporter.of(context));
        return options;
    }

    private boolean hasNetworkConnection() {
        if (SIMULATE_OFFLINE_MODE) {
            return false;
        }
        // check for wifi connection, see https://stackoverflow.com/a/34904367/1320237
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if( wifiInfo.getNetworkId() == -1 ){
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    public boolean requireInternetConnection() {
        SettingsImageDB db = getImageDB();
        return db.requiresInternetConnection();
    }

    public static class Gallery {

        private final String url;
        private final int description;

        public Gallery(String url, int description) {
            this.url = url;
            this.description = description;
        }

        public String getUrl() {
            return url;
        }

        public int getDescription() {
            return description;
        }
    }
}
