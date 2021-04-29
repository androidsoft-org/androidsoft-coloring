package org.androidsoft.coloring.util.errors;

import android.util.Log;

public class LoggingErrorReporter implements ErrorReporter {
    @Override
    public void report(Exception e) {
        Log.e("Error:", e.toString(), e);
    }
}
