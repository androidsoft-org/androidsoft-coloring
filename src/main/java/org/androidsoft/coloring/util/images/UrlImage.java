package org.androidsoft.coloring.util.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class UrlImage implements ImageDB.Image {
    private final URL url;
    private final String id;
    private final String lastModified;

    public UrlImage(URL url, String id, String lastModified) {
        this.url = url;
        this.id = id;
        this.lastModified = lastModified;
    }

    @Override
    public Bitmap asPreviewImage(Context context, int maxWidth) {
        return null;
    }

    @Override
    public boolean isVisible() {
        // visible if any of the thumbnails have loaded
        return false;
    }

    @Override
    public Bitmap getImage(Context context) {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url.toString());
        parcel.writeString(id);
        parcel.writeString(lastModified);
    }

    public static Creator CREATOR = new Creator() {
        @Override
        public ImageDB.Image createFromParcel(Parcel parcel) {
            String urlString = parcel.readString();
            String id = parcel.readString();
            String lastModified = parcel.readString();
            URL url = null;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return new NullImage();
            }
            return new UrlImage(url, id, lastModified);
        }

        @Override
        public ImageDB.Image[] newArray(int i) {
            return new ImageDB.Image[0];
        }
    };
}
