/*
 * Derived from CTSLauncher AssistantSwitcher, Copyright 2025 Andrew Gunnerson.
 * RealCTS modifications 2026. SPDX-License-Identifier: GPL-3.0-only
 */
package com.realcts.app;

import android.app.job.*;
import android.content.*;
import android.os.*;
import android.provider.Settings;
import java.util.Objects;

final class VoiceInteractionSwitcher {
    private static final int JOB = 1734;
    private final Context c;
    private final SharedPreferences journal;
    VoiceInteractionSwitcher(Context context) {
        c = context;
        journal = c.getSharedPreferences("vis-transaction", 0);
    }

    boolean pending() { return journal.contains("temporary"); }

    void acquire(ComponentName google) throws Exception {
        String original = Capabilities.setting(c, Capabilities.VIS);
        String assistant = Capabilities.setting(c, "assistant");
        String target = google.flattenToString();
        if (google.equals(Capabilities.component(original))) return;
        if (pending()) throw new IllegalStateException("An assistant restoration is pending");
        JobInfo job = new JobInfo.Builder(JOB, new ComponentName(c, RecoveryJob.class))
                .setMinimumLatency(8000).setOverrideDeadline(30000).setPersisted(true).build();
        if (c.getSystemService(JobScheduler.class).schedule(job) != JobScheduler.RESULT_SUCCESS)
            throw new IllegalStateException("Could not schedule assistant recovery");
        if (!journal.edit().putString("original", original).putString("assistant", assistant)
                .putString("temporary", target).commit())
            throw new IllegalStateException("Could not save the original assistant");
        if (!Objects.equals(original, Capabilities.setting(c, Capabilities.VIS))
                || !Objects.equals(assistant, Capabilities.setting(c, "assistant"))) {
            journal.edit().clear().commit();
            throw new IllegalStateException("Assistant changed during setup");
        }
        write(target);
        // Some builds expose the active bound component. Only use a settling interval if
        // that read-only probe is inaccessible. Secure setting readback alone is insufficient.
        Object proxy = null;
        java.lang.reflect.Method active = null;
        try {
            Class<?> api = Class.forName("com.android.internal.app.IVoiceInteractionManagerService");
            proxy = Class.forName(api.getName() + "$Stub").getMethod("asInterface", IBinder.class)
                    .invoke(null, NativeCircleTrigger.voiceManager());
            if (proxy != null) active = api.getMethod("getActiveServiceComponentName");
        } catch (ReflectiveOperationException ignored) { }
        long until = SystemClock.elapsedRealtime() + 1500;
        while (active != null) {
            try { if (google.equals(active.invoke(proxy))) break; }
            catch (ReflectiveOperationException e) { active = null; break; }
            if (SystemClock.elapsedRealtime() >= until) throw new IllegalStateException("Google voice service did not become active");
            Thread.sleep(35);
        }
        if (active == null) Thread.sleep(250);
        if (!owns()) throw new IllegalStateException("Assistant changed before launch");
    }

    private boolean owns() {
        return RestorePolicy.owns(Capabilities.setting(c, Capabilities.VIS), journal.getString("temporary", null),
                Capabilities.setting(c, "assistant"), journal.getString("assistant", null));
    }

    void recover() {
        if (!pending()) return;
        if (owns()) {
            String original = journal.getString("original", null);
            write(original);
            if (!Objects.equals(original, Capabilities.setting(c, Capabilities.VIS)))
                throw new IllegalStateException("Could not verify restoration. Check Digital assistant settings.");
        }
        if (!journal.edit().clear().commit()) throw new IllegalStateException("Could not clear recovery record");
        c.getSystemService(JobScheduler.class).cancel(JOB);
    }
    private void write(String value) {
        if (!Settings.Secure.putString(c.getContentResolver(), Capabilities.VIS, value))
            throw new IllegalStateException("Android rejected the voice service setting");
    }
}
