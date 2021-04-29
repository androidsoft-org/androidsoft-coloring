package org.androidsoft.coloring.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import org.androidsoft.coloring.ui.activity.PaintActivity;
import org.androidsoft.coloring.ui.widget.Progress;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.quelltext.coloring.R;

import static android.os.Environment.DIRECTORY_DCIM;

public class BitmapSaver implements Runnable
{
    public static final String MIME_PNG = "image/png";

    protected final Context context;
    private File file;
    private Bitmap bitmap;
    private Thread thread = null;
    private Uri imageUri;

    public BitmapSaver(Context context, Bitmap bitmap)
    {
        this.context = context;
        this.bitmap = bitmap;
        file = newFileName();
        thread = new Thread(this);
    }

    public void start() {
        thread.start();
    }

    public File newFileName() {
        // Get a filename.
        String filename = newImageFileName();
        File directory = getSavedImagesDirectory(context);
        return new File(directory, filename);
    }

    public static File getSavedImagesDirectory(Context context) {
        File directory = new File(
                Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM),
                context.getString(R.string.app_name));
        directory.mkdirs();
        return directory;
    }

    public File getFile() {
        return file;
    }

    public void run() {
        File file = getFile();
        // save the bitmap, see https://stackoverflow.com/a/673014
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
        } catch (NullPointerException e) { // if the directory is removed
            e.printStackTrace();
            return;
        }
        saveToURI();
    }

    protected void saveToURI() {
        File file = getFile();
        String filename = file.getName();
        // Save it to the MediaStore.
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, MIME_PNG);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATA, file.toString());
        File parentFile = file.getParentFile();
        values.put(MediaStore.Images.Media.BUCKET_ID,
                parentFile.toString().toLowerCase().hashCode());
        values.put(MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                parentFile.getName().toLowerCase());
        imageUri = context.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Scan the file so that it appears in the system as it should.
        if (imageUri != null)
        {
            new MediaScannerNotifier(context, file.toString(), MIME_PNG);
        }
    }

    public Uri getImageUri() {
        return imageUri;
    }

    private String newImageFileName()
    {
        final DateFormat fmt = new SimpleDateFormat("yyyyMMdd-HHmmss");
        return fmt.format(new Date()) + ".png";
    }

    public boolean isRunning() {
        return thread.isAlive();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void alreadySaved(BitmapSaver bitmapSaver) {

    }
}
