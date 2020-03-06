package org.androidsoft.coloring.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

// Class needed to work-around gallery crash bug. If we do not have this
// scanner then the save succeeds but the Pictures app will crash when
// trying to open.
public class MediaScannerNotifier implements MediaScannerConnection.MediaScannerConnectionClient
{

    public MediaScannerNotifier(Context context, String path, String mimeType)
    {
        _path = path;
        _mimeType = mimeType;
        _connection = new MediaScannerConnection(context, this);
        _connection.connect();
    }

    public void onMediaScannerConnected()
    {
        _connection.scanFile(_path, _mimeType);
    }

    public void onScanCompleted(String path, final Uri uri)
    {
        _connection.disconnect();
    }
    private MediaScannerConnection _connection;
    private String _path;
    private String _mimeType;
}
