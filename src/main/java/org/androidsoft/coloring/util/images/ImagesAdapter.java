package org.androidsoft.coloring.util.images;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import eu.quelltext.coloring.R;

public class ImagesAdapter extends RecyclerView.Adapter {
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
                if (image.isVisible()) {
                    int width = imageView.getWidth();
                    width = width == 0 ? getScreenWidth() / numberOfImagesPerRow : width;
                    if (width > maxWidth) {
                        width = maxWidth;
                    }
                    Bitmap bitmap = image.asPreviewImage(root.getContext(), width);
                    imageView.setImageBitmap(bitmap);
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

}
