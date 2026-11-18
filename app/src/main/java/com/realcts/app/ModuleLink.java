package com.realcts.app;

import android.app.Activity;
import android.content.*;
import android.os.SystemClock;
import java.util.concurrent.*;

final class ModuleLink {
    static final String SYSTEM_UI = "com.android.systemui";
    static final String PERMISSION = "com.realcts.app.permission.NATIVE_REQUEST";
    static final String PROBE = "com.realcts.app.CHECK_SERVICE";
    static final String START = "com.realcts.app.START_SERVICE";
    static final String CHECK_HELPER = "com.realcts.app.CHECK_HELPER";
    static final String START_HELPER = "com.realcts.app.START_HELPER";
    static final String DEADLINE = "deadline";

    static final class Status {
        final String helper, service;
        Status(String helper, String service) { this.helper = helper; this.service = service; }
        boolean helperReady() { return "ready".equals(helper); }
        boolean serviceReady() { return "ready".equals(service); }
    }
    static Status unavailable(String reason) { return new Status(reason, reason); }
    static Status inspect(Context c) throws InterruptedException {
        return new Status(request(c, CHECK_HELPER), request(c, PROBE));
    }

    // Runs on a worker. Final receiver is delivered on the application's main looper.
    static String request(Context c, String action) throws InterruptedException {
        CompletableFuture<String> answer = new CompletableFuture<>();
        Intent request = new Intent(action).setPackage(SYSTEM_UI)
                .addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY | Intent.FLAG_RECEIVER_FOREGROUND)
                .putExtra(DEADLINE, SystemClock.elapsedRealtime() + 1600);
        c.sendOrderedBroadcast(request, null, new BroadcastReceiver() {
            public void onReceive(Context ignored, Intent result) {
                answer.complete(getResultCode() == Activity.RESULT_OK ? "ready" :
                        getResultData() == null ? "Module inactive or outside System UI scope" : getResultData());
            }
        }, null, Activity.RESULT_CANCELED, null, null);
        try { return answer.get(1900, TimeUnit.MILLISECONDS); }
        catch (ExecutionException | TimeoutException e) { return "No timely response from System UI"; }
    }
}
