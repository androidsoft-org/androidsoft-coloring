package org.androidsoft.coloring.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import org.androidsoft.coloring.ui.activity.PaintActivity;

import eu.quelltext.coloring.R;

public class BitmapSharer extends BitmapSaver
{

    public BitmapSharer(Context context, Bitmap bitmap) {
        super(context, bitmap);
    }

    @Override
    protected Uri saveToURI()
    {
        Uri uri = super.saveToURI();

        if (uri != null)
        {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("image/png");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            context.startActivity(Intent.createChooser(sharingIntent, context.getString( R.string.dialog_share )));
        }
        return uri;
    }
}
