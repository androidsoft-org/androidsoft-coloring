package org.androidsoft.coloring.util.images;

import android.os.Parcel;

import java.net.MalformedURLException;
import java.net.URL;

class ThumbNailImage extends UrlImage {
    private final int maxWidth;

    public ThumbNailImage(URL thumbUrl, String thumbId, String thumbLastModified, int maxWidth) {
        super(thumbUrl, thumbId, thumbLastModified);
        this.maxWidth = maxWidth;
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
            int maxWidth = parcel.readInt();
            URL url = null;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return new NullImage();
            }
            return new ThumbNailImage(url, id, lastModified, maxWidth);
        }

        @Override
        public ImageDB.Image[] newArray(int i) {
            return new ImageDB.Image[0];
        }
    };
}
