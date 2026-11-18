/* Adapted from CTSLauncher. Copyright 2025 Andrew Gunnerson. GPL-3.0-only */
package com.realcts.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class AssistantEntryActivity extends Activity {
    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        boolean local = getClass() == LocalEntry.class;
        boolean assistant = Intent.ACTION_ASSIST.equals(getIntent().getAction()) && Registration.held(this);
        if (saved == null && (local || assistant)) {
            Intent job = new Intent(this, TriggerService.class);
            // The exported ASSIST activity accepts no caller-supplied options.
            if (getClass() == LocalEntry.class)
                job.putExtra("tile", getIntent().getBooleanExtra("tile", false));
            try { startService(job); }
            catch (RuntimeException e) {
                String message = "Android blocked background invocation. Open RealCTS and run Test Circle to Search.";
                State.result(this, message);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }
}
