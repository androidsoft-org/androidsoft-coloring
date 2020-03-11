package org.androidsoft.coloring.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.androidsoft.coloring.util.images.GalleryImageDB;
import org.androidsoft.coloring.util.images.SettingsImageDB;
import org.json.JSONArray;
import org.json.JSONException;

import eu.quelltext.coloring.R;

public class Settings {
    public static final GalleryImageDB[] DEFAULT_GALLERIES = new GalleryImageDB[]{
            new DefaultGalleryImageDB("https://gallery.quelltext.eu", R.string.settings_gallery_quelltext),
            new DefaultGalleryImageDB("http://gallery.quelltext.eu", R.string.settings_gallery_quelltext_http),
    };
    private static final String KEY_SETTINGS = "settings";
    private final Context context;
    private final SharedPreferences preferences;
    private SharedPreferences.Editor editor = null;

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
}
