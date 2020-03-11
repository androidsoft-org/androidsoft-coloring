package org.androidsoft.coloring.util.images;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.ContextCompat;

import org.androidsoft.coloring.ui.activity.ChoosePictureActivity;
import org.androidsoft.coloring.ui.activity.SettingsActivity;
import org.androidsoft.coloring.util.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.quelltext.coloring.R;

import static org.androidsoft.coloring.util.Settings.DEFAULT_GALLERIES;

public class SettingsImageDB extends Subject implements ImageDB {

    private static final String VERSION = "-0";
    private static final String ID_RESOURCES = "ResourceImageDB";
    private static final String ID_SAVED_IMAGES = "SavedImages";
    private static final String ID_LAST_PAINTED = "LastPaintedImage";
    private static final String KEY_ENTRY_ORDER = "SettingsImageDB.entryIds" + VERSION;
    private static final String KEY_ENTRY_ACTIVATED = "SettingsImageDB.entriesActivated" + VERSION;
    private final Settings settings;
    private final JoinedImageDB paintedImage;
    private List<Entry> entries = new ArrayList<>();

    public SettingsImageDB(Settings settings) {
        this.settings = settings;
        paintedImage = new JoinedImageDB();
        entries.add(new Entry(
                ID_LAST_PAINTED,
                paintedImage,
                getString(R.string.settings_galleries_last_painted),
                getString(R.string.settings_galleries_last_painted_title)));
        entries.add(new BrowsableEntry(
                ID_SAVED_IMAGES,
                DirectoryImageDB.atSaveLocationOf(settings.getContext()),
                getString(R.string.settings_galleries_saved_images),
                getString(R.string.settings_galleries_saved_images_title),
                new Browsable(){
                    @Override
                    public void browse(Context context) {
                        Intent intent = new Intent(context, ChoosePictureActivity.class);
                        context.startActivity(intent);
                    }
                }));
        entries.add(new BrowsableEntry(
                ID_RESOURCES,
                new ResourceImageDB(),
                getString(R.string.settings_galleries_resources),
                getString(R.string.settings_galleries_resources_title),
                new Browsable(){
                    @Override
                    public void browse(Context context) {
                        Intent intent = new Intent(context, ChoosePictureActivity.class);
                        context.startActivity(intent);
                    }
                }));

        for (GalleryImageDB gallery : DEFAULT_GALLERIES) {
            entries.add(new GalleryEntry(gallery));
        }
        orderEntries();
    }

    private String getString(int resourceId) {
        return settings.getContext().getString(resourceId);
    }

    private void orderEntries() {
        String[] ids = getEntryOrderIds();
        List<Entry> newEntries = new ArrayList<>();
        for (String id: ids) {
            Entry entry = getEntryById(id);
            newEntries.add(entry);
        }
        entries = newEntries;
    }

    private String[] getEntryOrderIds() {
        String[] ids = settings.getStringArray(KEY_ENTRY_ORDER);
        if (ids == null) {
            ids = new String[3 + DEFAULT_GALLERIES.length];
            ids[0] = ID_LAST_PAINTED;
            ids[1] = ID_RESOURCES;
            ids[2] = ID_SAVED_IMAGES;
            int i = 3;
            for (GalleryImageDB entry : DEFAULT_GALLERIES) {
                ids[i] = entry.getUrl();
                i++;
            }
        }
        return ids;
    }

    @Override
    public void attachObserver(Observer observer) {
        super.attachObserver(observer);
        getJoinedImageDB().attachObserver(observer);
    }

    private Entry getEntryById(String id) {
        Entry result = new UserDefinedEntry(id);
        for (Entry entry : entries) {
            if (entry.getId().equals(id)) {
                result = entry;
                break;
            }
        }
        result.setActivationFromSettings(false);
        String[] activated = getActivatedIds();
        for (String activatedId : activated) {
            if (id.equals(activatedId)) {
                result.setActivationFromSettings(true);
            }
        }
        return result;
    }

    private String[] getActivatedIds() {
        String[] ids = settings.getStringArray(KEY_ENTRY_ACTIVATED);
        if (ids == null) {
            ids = new String[]{ID_LAST_PAINTED, ID_RESOURCES, ID_SAVED_IMAGES};
        }
        return ids;
    }

    private void save() {
        String[] ids = new String[entries.size()];
        Set<String> activated = new HashSet<>();
        int i = 0;
        for (Entry entry : entries) {
            ids[i] = entry.getId();
            if (entry.isActivated()) {
                activated.add(entry.getId());
            }
            i++;
        }
        settings.setStringArray(KEY_ENTRY_ORDER, ids);
        settings.setStringArray(KEY_ENTRY_ACTIVATED, activated.toArray(new String[0]));
        settings.save();
    }

    public void addPaintedImage(Image image) {
         paintedImage.add(image);
    }

    public List<Entry> entries() {
        return entries;
    }

    @Override
    public int size() {
        return getJoinedImageDB().size();
    }

    private ImageDB getJoinedImageDB() {
        return new JoinedImageDB(new ArrayList<ImageDB>(entries));
    }

    @Override
    public Image get(int index) {
        return getJoinedImageDB().get(index);
    }

    public boolean addUserDefinedGallery(String url) {
        for (Entry entry : entries) {
            if (entry.getId().equals(url)) {
                return false; // no duplicates
            }
        }
        entries.add(new UserDefinedEntry(url));
        notifyObservers();
        return true;
    }

    public class Entry implements ImageDB {
        private final String id;
        private final ImageDB db;
        private final String description;
        private final String title;
        private boolean activated;

        public Entry(String id, ImageDB db, String description, String title) {
            this.id = id;
            this.db = db;
            this.description = description;
            this.title = title;
        }

        public void setActivation(boolean activated) {
            if (this.activated != activated) {
                this.activated = activated;
                save();
            }
        }

        public void setActivationFromSettings(boolean activated) {
            this.activated = activated;
        }

        public String getDescription() {
            return description;
        }

        public String getName() {
            return title;
        }

        public boolean canBeDeleted() {
            return false;
        }

        public void delete() {
        }

        @Override
        public int size() {
            return isActivated() ? db.size() : 0;
        }

        @Override
        public Image get(int index) {
            return db.get(index);
        }

        @Override
        public void attachObserver(Observer observer) {
            db.attachObserver(observer);
        }

        public String getId() {
            return id;
        }

        public boolean isActivated() {
            return activated;
        }

        public boolean canBrowse() {
            return false;
        }

        public void browse(Context context) {
        }
    }

    private class GalleryEntry extends Entry {
        public GalleryEntry(GalleryImageDB gallery) {
            super(gallery.getUrl(), gallery, getString(gallery.getDescriptionResourceId()), gallery.getUrl());
        }

        @Override
        public String getName() {
            Uri uri = Uri.parse(getUrl());
            return uri.getHost();
        }

        @Override
        public boolean canBrowse() {
            return true;
        }

        @Override
        public void browse(Context context) {
            // open url in browser, see https://stackoverflow.com/a/2201999/1320237
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getUrl()));
            context.startActivity(browserIntent);
        }

        public String getUrl() {
            return getId();
        }
    }

    private class UserDefinedEntry extends GalleryEntry {
        public UserDefinedEntry(String id) {
            super(new GalleryImageDB(id));
        }

        @Override
        public String getDescription() {
            return settings.getContext().getString(R.string.settings_galleries_user_defined, getId());
        }

        @Override
        public boolean canBeDeleted() {
            return true;
        }

        @Override
        public void delete() {
            entries.remove(this);
            notifyObservers();
            save();
        }
    }

    interface Browsable {
        void browse(Context context);
    }

    private class BrowsableEntry extends Entry {

        private final Browsable browsable;

        public BrowsableEntry(String id, ImageDB db, String description, String title, Browsable browsable) {
            super(id, db, description, title);
            this.browsable = browsable;
        }

        @Override
        public boolean canBrowse() {
            return true;
        }

        @Override
        public void browse(Context context) {
            browsable.browse(context);
        }
    }
}
