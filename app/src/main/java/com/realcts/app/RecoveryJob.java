package com.realcts.app;

import android.app.job.*;

public final class RecoveryJob extends JobService {
    @Override public boolean onStartJob(JobParameters p) {
        TriggerService.WORK.execute(() -> {
            boolean retry = false;
            try { new VoiceInteractionSwitcher(this).recover(); }
            catch (RuntimeException e) {
                retry = true;
                State.result(this, "Restoration pending. Check the ADB grant and Digital assistant settings.");
            }
            jobFinished(p, retry);
        });
        return true;
    }
    @Override public boolean onStopJob(JobParameters p) { return true; }
}
