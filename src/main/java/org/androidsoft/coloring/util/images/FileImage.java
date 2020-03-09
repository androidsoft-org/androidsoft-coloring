package org.androidsoft.coloring.util.images;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;

import java.io.File;

class FileImage implements ImageDB.Image {
    private final File file;
    private Bitmap previewImage = null;

    public FileImage(File file) {
        this.file = file;
    }

    @Override
    public Bitmap asPreviewImage(Context context, int maxWidth) {
        if (previewImage == null) {
            previewImage = decodeSampledBitmapFromFile(file.toString(), maxWidth);
        }
        return previewImage;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public Bitmap getImage(Context context) {
        // read bitmap from file
        // see https://stackoverflow.com/a/8710690/1320237
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(file.toString(), options);
        // todo: check if black and white and other operations need to be perfomed
        return bitmap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(file.toString());
    }

    public static Creator CREATOR = new Creator() {
        @Override
        public ImageDB.Image createFromParcel(Parcel parcel) {
            String path = parcel.readString();
            return new FileImage(new File(path));
        }

        @Override
        public ImageDB.Image[] newArray(int i) {
            return new ImageDB.Image[0];
        }
    };

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth) {
        // from https://developer.android.com/topic/performance/graphics/load-bitmap#java
        // Raw width of image
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (width > reqWidth) {

            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth) {
        // from https://developer.android.com/topic/performance/graphics/load-bitmap#java
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }
}
