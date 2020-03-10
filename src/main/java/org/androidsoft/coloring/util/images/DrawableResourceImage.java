package org.androidsoft.coloring.util.images;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;

import org.androidsoft.coloring.util.FloodFill;

public class DrawableResourceImage implements ImageDB.Image {
    private final int resourceId;
    private Bitmap previewImage = null;

    public DrawableResourceImage(int resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public Bitmap asPreviewImage(Context context, int maxWidth) {
        if (previewImage == null) {
            previewImage = decodeSampledBitmapFromResource(context.getResources(), resourceId, maxWidth);
        }
        return previewImage;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public Bitmap getImage(Context context) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        return FloodFill.asBlackAndWhite(bitmap);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(resourceId);
    }

    public static Creator CREATOR = new Creator() {
        @Override
        public ImageDB.Image createFromParcel(Parcel parcel) {
            int resourceId = parcel.readInt();
            return new DrawableResourceImage(resourceId);
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

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth) {
        // from https://developer.android.com/topic/performance/graphics/load-bitmap#java
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

}
