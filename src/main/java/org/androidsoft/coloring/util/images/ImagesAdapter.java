package org.androidsoft.coloring.util.images;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;
import org.androidsoft.coloring.util.imports.ImagePreview;

import java.io.FileNotFoundException;
import java.io.InputStream;

import eu.quelltext.coloring.R;

public class ImagesAdapter extends RecyclerView.Adapter {
    private static final int MAX_WIDTH_HEIGHT_MULTIPLIER = 3;
    private final ImageDB imageDB;
    private final int layoutId;
    private final int[] imageViewIds;
    private final int numberOfImagesPerRow;
    private ImageListener imageListener = new NullImageListener();

    public ImagesAdapter(ImageDB imageDB, int layoutId, int[] imageViewIds) {
        this.imageDB = imageDB;
        imageDB.attachObserver(new Subject.Observer() {
            @Override
            public void update() {
                notifyDataSetChanged();
            }
        });
        this.layoutId = layoutId;
        this.imageViewIds = imageViewIds;
        this.numberOfImagesPerRow = imageViewIds.length;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(layoutId, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int index) {
        ViewHolder holder = (ViewHolder)viewHolder;
        ImageDB.Image[] images = new ImageDB.Image[numberOfImagesPerRow];
        int start = index * numberOfImagesPerRow;
        for (int i = 0; i < numberOfImagesPerRow; i++) {
            images[i] = imageDB.get(start + i);
        }
        holder.display(images);
    }

    @Override
    public int getItemCount() {
        int numberOfImages = imageDB.size();
        int numberOfRows = numberOfImages / numberOfImagesPerRow;
        return numberOfRows + (numberOfImages % numberOfImagesPerRow == 0 ? 0 : 1);
    }

    public void setImageListener(ImageListener listener) {
        imageListener = listener;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private final View root;
        private final int maxWidth;

        public ViewHolder(@NonNull View root) {
            super(root);
            this.root = root;
            maxWidth = root.getContext().getResources().getDimensionPixelSize(R.dimen.maximum_image_preview_size);
        }

        public void display(ImageDB.Image[] images) {
            for (int i = 0; i < images.length; i++) {
                ImageView imageView = root.findViewById(imageViewIds[i]);
                final ImageDB.Image image = images[i];
                if (image.canBePainted()) {
                    int width = imageView.getWidth();
                    width = width == 0 ? getScreenWidth() / numberOfImagesPerRow : width;
                    if (width > maxWidth) {
                        width = maxWidth;
                    }
                    // TODO: add loading animation for the time the image is loading
                    image.asPreviewImage(new ThumbPreview(imageView, width), new LoadImageProgress(null, null));
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            imageListener.onImageChosen(image);
                        }
                    });
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    imageView.setVisibility(View.INVISIBLE);
                }
            }
        }

        private int getScreenWidth() {
            // from https://stackoverflow.com/a/4744499
            Context context = root.getContext();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            try {
                ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            } catch (ClassCastException e) {
                e.printStackTrace();
                return 400; // default width
            }
            return displayMetrics.widthPixels;
        }
    }

    class ThumbPreview implements ImagePreview {

        private final ImageView imageView;
        private final int maxWidth;

        public ThumbPreview(ImageView imageView, int maxWidth) {
            this.imageView = imageView;
            imageView.setImageResource(R.drawable.ic_logo);
            this.maxWidth = maxWidth;
        }

        @Override
        public void setImage(final Bitmap image) {
            imageView.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(image);
                }
            });
        }

        @Override
        public int getWidth() {
            return maxWidth;
        }

        @Override
        public int getHeight() {
            return maxWidth * MAX_WIDTH_HEIGHT_MULTIPLIER;
        }

        @Override
        public InputStream openInputStream(Uri uri) throws FileNotFoundException {
            return imageView.getContext().getContentResolver().openInputStream(uri);
        }

        @Override
        public void done(Bitmap bitmap) {
            setImage(bitmap);
        }
    }
}
