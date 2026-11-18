package com.realcts.app;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.KeyguardManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;

public final class SearchActionService extends AccessibilityService {
    // AOSP Launcher3 SystemActionConstants.SYSTEM_ACTION_ID_SEARCH_SCREEN.
    private static final int SEARCH_SCREEN = 500;
    private static volatile SearchActionService connected;

    private final Handler main = new Handler(Looper.getMainLooper());
    private long lastPanel = -10000;
    private final SharedPreferences.OnSharedPreferenceChangeListener changes = (prefs, key) -> {
        if ("assistantPanel".equals(key)) configureEvents();
    };
    private void configureEvents() {
        AccessibilityServiceInfo info = getServiceInfo();
        if (info == null) return;
        info.packageNames = new String[]{Capabilities.GOOGLE};
        info.eventTypes = State.prefs(this).getBoolean("assistantPanel", false)
                ? AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED : 0;
        setServiceInfo(info);
    }
    @Override protected void onServiceConnected() {
        connected = this;
        State.prefs(this).registerOnSharedPreferenceChangeListener(changes);
        configureEvents();
    }
    @Override public boolean onUnbind(Intent intent) { connected = null; return super.onUnbind(intent); }
    @Override public void onDestroy() {
        if (connected == this) connected = null;
        main.removeCallbacksAndMessages(null);
        State.prefs(this).unregisterOnSharedPreferenceChangeListener(changes);
        super.onDestroy();
    }
    static boolean isConnected() { return connected != null; }
    static boolean isAssistantPanel(CharSequence pkg, CharSequence cls) {
        return Capabilities.GOOGLE.contentEquals(pkg == null ? "" : pkg)
                && "com.google.android.apps.search.assistant.surfaces.voice.robin.ui.floaty.activity.FloatyActivity"
                    .contentEquals(cls == null ? "" : cls);
    }
    private boolean panelModeEnabled() {
        return State.prefs(this).getBoolean("assistantPanel", false)
                && !State.prefs(this).getBoolean("paused", false)
                && State.mode(this) == TriggerStrategyManager.Mode.Automatic
                && !getSystemService(KeyguardManager.class).isKeyguardLocked();
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                || !isAssistantPanel(event.getPackageName(), event.getClassName())
                || SystemClock.uptimeMillis() - event.getEventTime() > 1000
                || !panelModeEnabled()) return;
        long now = SystemClock.elapsedRealtime();
        if (now - lastPanel < 4000 || !new Capabilities(this).googleActive) return;
        lastPanel = now;
        if (!performGlobalAction(GLOBAL_ACTION_BACK)) {
            State.result(this, "Assistant panel detected. Android declined dismissal.");
            return;
        }
        main.postDelayed(() -> {
            if (connected != this || !panelModeEnabled()) return;
            try {
                startActivity(new Intent(this, LocalEntry.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
            } catch (RuntimeException e) { State.result(this, "Android blocked the assistant-panel handoff."); }
        }, 350);
    }
    @Override public void onInterrupt() { }

    private boolean advertisesSearch() {
        if (Build.VERSION.SDK_INT < 30) return false;
        for (AccessibilityAction a : getSystemActions()) {
            if (a.getId() == SEARCH_SCREEN) return true;
        }
        return false;
    }
    static boolean available() {
        SearchActionService service = connected;
        return service != null && service.advertisesSearch();
    }
    static boolean request() {
        SearchActionService service = connected;
        return service != null && service.advertisesSearch()
                && Capabilities.GOOGLE.equals(Capabilities.resource(service, "config_defaultContextualSearchPackageName"))
                && service.performGlobalAction(SEARCH_SCREEN);
    }
}
