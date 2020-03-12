package org.androidsoft.coloring.util.images;

import android.os.Parcel;
import android.os.Parcelable;

/* This object represents the options of how remote urls should be retrieved.
 *
 */
public class RetrievalOptions implements Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    public static Creator CREATOR = new Creator() {
        @Override
        public RetrievalOptions createFromParcel(Parcel parcel) {
            return new RetrievalOptions();
        }

        @Override
        public RetrievalOptions[] newArray(int i) {
            return new RetrievalOptions[0];
        }
    };
}
