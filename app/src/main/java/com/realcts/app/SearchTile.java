/* Adapted from CTSLauncher. Copyright 2025 Andrew Gunnerson. GPL-3.0-only */
package com.realcts.app;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

public final class SearchTile extends TileService {
    // The PendingIntent overload was introduced in API 34.
    @android.annotation.SuppressLint("StartActivityAndCollapseDeprecated")
    @Override public void onClick() {
        Intent intent = new Intent(this, LocalEntry.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("tile", true);
        if (Build.VERSION.SDK_INT >= 34) startActivityAndCollapse(PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        else startActivityAndCollapse(intent);
    }
}
