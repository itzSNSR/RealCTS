package com.realcts.app;

import android.Manifest;
import android.content.*;
import android.content.pm.*;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import android.service.voice.VoiceInteractionService;

final class Capabilities {
    static final String GOOGLE = "com.google.android.googlequicksearchbox";
    static final String VIS = "voice_interaction_service";
    static final String LEGACY_ACTION = "com.android.contextualsearch.LAUNCH";
    static final String SERVICE_ACTION = "android.app.contextualsearch.action.LAUNCH_CONTEXTUAL_SEARCH";

    final ComponentName googleVoice;
    final boolean googleInstalled, googleActive, canWrite, helper, googleTarget, nativeActivity;
    final String key, legacyKey, helperEnabledKey, configuredPackage;

    Capabilities(Context c) {
        PackageManager pm = c.getPackageManager();
        boolean installed;
        try { installed = pm.getApplicationInfo(GOOGLE, 0).enabled; }
        catch (PackageManager.NameNotFoundException e) { installed = false; }
        googleInstalled = installed;
        googleVoice = findVoice(pm);
        ComponentName active = component(readableSetting(c, VIS));
        googleActive = active != null && GOOGLE.equals(active.getPackageName());
        canWrite = c.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        key = resource(c, "config_defaultContextualSearchKey");
        configuredPackage = resource(c, "config_defaultContextualSearchPackageName");
        legacyKey = resource(c, "config_defaultContextualSearchLegacyEnabled");
        helperEnabledKey = resource(c, "config_defaultContextualSearchEnabled");
        googleTarget = GOOGLE.equals(configuredPackage);
        nativeActivity = resolves(pm, LEGACY_ACTION, 0) || resolves(pm, SERVICE_ACTION, 0)
                || hasOmnientActivity(pm);
        helper = googleTarget && !key.isEmpty() && !legacyKey.isEmpty()
                && resolves(pm, LEGACY_ACTION, PackageManager.MATCH_SYSTEM_ONLY);
    }

    static boolean resolves(PackageManager pm, String action, int flags) {
        return pm.resolveActivity(new Intent(action).setPackage(GOOGLE), flags) != null;
    }

    // Older Google builds can expose the Omnient component only inside the Google package.
    private static boolean hasOmnientActivity(PackageManager pm) {
        try {
            ActivityInfo[] activities = pm.getPackageInfo(GOOGLE, PackageManager.GET_ACTIVITIES).activities;
            if (activities != null) for (ActivityInfo activity : activities)
                if (activity.enabled && activity.name.endsWith(".OmnientActivity")) return true;
        } catch (PackageManager.NameNotFoundException ignored) { }
        return false;
    }

    // Adapted from CTSLauncher AssistantSwitcher, Copyright 2025 Andrew Gunnerson.
    private static ComponentName findVoice(PackageManager pm) {
        Intent query = new Intent(VoiceInteractionService.SERVICE_INTERFACE).setPackage(GOOGLE);
        for (ResolveInfo result : pm.queryIntentServices(query, PackageManager.GET_META_DATA
                | PackageManager.MATCH_DIRECT_BOOT_AWARE | PackageManager.MATCH_DIRECT_BOOT_UNAWARE)) {
            ServiceInfo s = result.serviceInfo;
            if (s != null && Manifest.permission.BIND_VOICE_INTERACTION.equals(s.permission)
                    && s.metaData != null && s.metaData.containsKey(VoiceInteractionService.SERVICE_META_DATA))
                return new ComponentName(s.packageName, s.name);
        }
        return null;
    }

    static String setting(Context c, String key) {
        return Settings.Secure.getString(c.getContentResolver(), key);
    }
    static String readableSetting(Context c, String key) {
        try { return setting(c, key); }
        catch (SecurityException denied) { return null; }
    }
    static ComponentName component(String value) {
        return value == null ? null : ComponentName.unflattenFromString(value);
    }
    static String resource(Context c, String name) {
        Resources r = c.getResources();
        int id = r.getIdentifier(name, "string", "android");
        return id == 0 ? "" : r.getString(id);
    }
    boolean visCandidate() {
        return supportsVoiceRequest(Build.VERSION.SDK_INT, googleActive, nativeActivity, configuredPackage);
    }
    static boolean supportsVoiceRequest(int sdk, boolean googleActive, boolean visibleActivity, String provider) {
        // A private Google handler may be invisible to package queries. CTSV and CircleKey
        // use the active Google voice service as their precondition in that case.
        return sdk <= 36 && (googleActive || visibleActivity) && (provider.isEmpty() || GOOGLE.equals(provider));
    }
}
