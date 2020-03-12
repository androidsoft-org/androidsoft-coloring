package org.androidsoft.coloring.util.images;

import android.os.Parcel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UrlImageWithPreview extends UrlImage {

    private final List<ThumbNailImage> thumbs = new ArrayList<>();


    public UrlImageWithPreview(URL url, String id, String lastModified) {
        super(url, id, lastModified);
    }


    public void addPreviewImage(ThumbNailImage thumb) {
        thumbs.add(thumb);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(thumbs.size());
        for (ThumbNailImage thumb : thumbs) {
            parcel.writeParcelable(thumb, i);
        }
    }

    public static Creator CREATOR = new Creator() {
        @Override
        public ImageDB.Image createFromParcel(Parcel parcel) {
            String urlString = parcel.readString();
            String id = parcel.readString();
            String lastModified = parcel.readString();
            int size = parcel.readInt();
            URL url = null;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return new NullImage();
            }
            UrlImageWithPreview image = new UrlImageWithPreview(url, id, lastModified);
            for (int i = 0; i < size; i++) {
                ThumbNailImage thumb = parcel.readParcelable(ThumbNailImage.class.getClassLoader());
                image.addPreviewImage(thumb);
            }
            return image;
        }

        @Override
        public ImageDB.Image[] newArray(int i) {
            return new ImageDB.Image[0];
        }
    };
}
