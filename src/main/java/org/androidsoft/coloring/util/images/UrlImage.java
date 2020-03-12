package org.androidsoft.coloring.util.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;
import org.androidsoft.coloring.util.FloodFill;
import org.androidsoft.coloring.util.imports.ImagePreview;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

class UrlImage implements ImageDB.Image {
    private final URL url;
    private final String id;
    private final String lastModified;
    private final RetrievalOptions retrievalOptions;

    public UrlImage(URL url, String id, String lastModified, RetrievalOptions retrievalOptions) {
        this.url = url;
        this.id = id;
        this.lastModified = lastModified;
        this.retrievalOptions = retrievalOptions;
    }

    @Override
    public void asPreviewImage(ImagePreview preview, LoadImageProgress progress) {
        progress.stepFail();
    }

    @Override
    public boolean canBePainted() {
        return false;
    }

    @Override
    public void asPaintableImage(ImagePreview preview, LoadImageProgress progress) {
        progress.stepFail();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(url.toString());
        parcel.writeString(id);
        parcel.writeString(lastModified);
        parcel.writeParcelable(retrievalOptions, flags);
    }

    public static Creator CREATOR = new Creator() {
        @Override
        public ImageDB.Image createFromParcel(Parcel parcel) {
            String urlString = parcel.readString();
            String id = parcel.readString();
            String lastModified = parcel.readString();
            Parcelable retrievalOptions = parcel.readParcelable(RetrievalOptions.class.getClassLoader());
            URL url = null;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return new NullImage();
            }
            return new UrlImage(url, id, lastModified, (RetrievalOptions) retrievalOptions);
        }

        @Override
        public ImageDB.Image[] newArray(int i) {
            return new ImageDB.Image[0];
        }
    };

    protected Uri getUri() {
        // see https://stackoverflow.com/a/9662933/1320237
        return Uri.parse(url.toString());
    }
}
