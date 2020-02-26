package org.androidsoft.coloring.ui.widget;

import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.androidsoft.coloring.R;

public class LoadImageProgress {

    private static final int STEPS = 7; // do not forget to change when you add steps

    private final ProgressBar progressBar;
    private TextView textView;
    private final Handler handler;

    public LoadImageProgress(ProgressBar progressBar, TextView textView) {
        this.progressBar = progressBar;
        this.textView = textView;
        progressBar.setIndeterminate(false);
        progressBar.setMax(STEPS);
        handler = new Handler();
        stepStart();

    }

    private void step(final int step, final int textId) {
        // stepping should be thread save
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(step);
                textView.setText(textId);
            }
        });
    }

    public void stepStart() {
        step(0, R.string.progress_starting);
    }

    public void stepDone() {
        step(STEPS, R.string.progress_done);
    }

    public void stepFail() {
        step(STEPS, R.string.progress_error);
    }

    public void stepInputPreview() {
        step(1, R.string.progress_input_preview);
    }

    public void stepPreparingClustering() {
        step(2, R.string.progress_preparing_clustering);
    }

    public void stepSampleDataForClassification() {
        step(3, R.string.progress_sample_data);
    }

    public void stepClusteringData() {
        step(4, R.string.progress_clustering_data);
    }

    public void stepCreateClusterPreview() {
        step(5, R.string.progress_create_cluster_preview);
    }
}
