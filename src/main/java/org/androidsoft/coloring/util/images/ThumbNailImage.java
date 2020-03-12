package org.androidsoft.coloring.util.images;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;
import org.androidsoft.coloring.util.imports.ImagePreview;
import org.androidsoft.coloring.util.imports.UriImageImport;

import java.net.MalformedURLException;
import java.net.URL;

class ThumbNailImage extends UrlImage {
    private final int maxWidth;

    public ThumbNailImage(URL thumbUrl, String thumbId, String thumbLastModified, int maxWidth, RetrievalOptions retrievalOptions) {
        super(thumbUrl, thumbId, thumbLastModified, retrievalOptions);
        this.maxWidth = maxWidth;
    }

    @Override
    public void asPreviewImage(ImagePreview preview, LoadImageProgress progress) {
        new UriImageImport(getUri(), progress, preview).start();
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
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(maxWidth);
    }

    public static Creator CREATOR = new Creator() {
        @Override
        public ImageDB.Image createFromParcel(Parcel parcel) {
            String urlString = parcel.readString();
            String id = parcel.readString();
            String lastModified = parcel.readString();
            Parcelable retrievalOptions = parcel.readParcelable(RetrievalOptions.class.getClassLoader());
            int maxWidth = parcel.readInt();
            URL url = null;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return new NullImage();
            }
            return new ThumbNailImage(url, id, lastModified, maxWidth, (RetrievalOptions) retrievalOptions);
        }

        @Override
        public ImageDB.Image[] newArray(int i) {
            return new ImageDB.Image[0];
        }
    };

    public int getWidth() {
        return maxWidth;
    }
}
