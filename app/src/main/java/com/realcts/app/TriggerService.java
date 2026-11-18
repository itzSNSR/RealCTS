package com.realcts.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.Toast;
import java.util.concurrent.*;

public final class TriggerService extends Service {
    static final ExecutorService WORK = Executors.newSingleThreadExecutor();
    private static final InvocationGate GATE = new InvocationGate();
    private final Handler main = new Handler(Looper.getMainLooper());
    private boolean running;
    private int latestId;

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        latestId = startId;
        if (running) return START_NOT_STICKY;
        if (intent == null || !GATE.acquire(SystemClock.elapsedRealtime())) {
            stopSelf(startId);
            return START_NOT_STICKY;
        }
        running = true;
        boolean tile = intent.getBooleanExtra("tile", false);
        WORK.execute(() -> {
            runTrigger(tile);
            main.post(() -> {
                GATE.release(SystemClock.elapsedRealtime());
                running = false;
                stopSelf(latestId);
            });
        });
        return START_NOT_STICKY;
    }

    private void runTrigger(boolean tile) {
        VoiceInteractionSwitcher switcher = new VoiceInteractionSwitcher(this);
        String report = "Native trigger interrupted";
        boolean accepted = false;
        try {
            switcher.recover();
            if (getSystemService(KeyguardManager.class).isDeviceLocked())
                throw new IllegalStateException("Unlock the screen to search");
            SharedPreferences p = State.prefs(this);
            if (p.getBoolean("paused", false))
                throw new IllegalStateException("Triggers paused after a failed visual test. Re-enable in Diagnostics.");
            int delay = p.getInt("delay", 0);
            Thread.sleep(tile ? Math.max(600, delay) : delay);
            Capabilities caps = new Capabilities(this);
            TriggerStrategyManager.Mode mode = State.mode(this);
            boolean probe = p.getBoolean("module", false) && (mode == TriggerStrategyManager.Mode.CSService || mode == TriggerStrategyManager.Mode.CSHelper);
            ModuleLink.Status module = probe ? ModuleLink.inspect(this) : ModuleLink.unavailable("Module use is disabled");
            TriggerStrategyManager.Route route = TriggerStrategyManager.select(mode, caps, module,
                    p.getBoolean("switch", false));
            if (route == TriggerStrategyManager.Route.Unavailable)
                throw new IllegalStateException(reason(caps, mode, module));
            if (route == TriggerStrategyManager.Route.Switch) switcher.acquire(caps.googleVoice);
            boolean useHelperModule = route == TriggerStrategyManager.Route.CSHelper && module.helperReady();
            if (route == TriggerStrategyManager.Route.SearchScreen) {
                accepted = SearchActionService.request();
            } else if (route == TriggerStrategyManager.Route.CSService || useHelperModule) {
                String result = ModuleLink.request(this, useHelperModule ? ModuleLink.START_HELPER : ModuleLink.START);
                if (!"ready".equals(result)) throw new IllegalStateException(result);
                accepted = true;
            } else {
                if ((route == TriggerStrategyManager.Route.Direct || route == TriggerStrategyManager.Route.Switch)
                        && !new Capabilities(this).googleActive)
                    throw new IllegalStateException("Google voice service changed before launch");
                accepted = NativeCircleTrigger.invoke(this, caps, route == TriggerStrategyManager.Route.CSHelper);
            }
            report = route + (accepted ? " request accepted. Confirm the selection overlay in Diagnostics."
                    : " request rejected by Android. Check Google app and assistant configuration.");
            if (accepted && p.getBoolean("haptic", false)) {
                Vibrator v = getSystemService(Vibrator.class);
                if (v != null && v.hasVibrator()) v.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            if (accepted && switcher.pending()) Thread.sleep(1000); // CTSLauncher hand-off window
        } catch (Exception | LinkageError e) {
            report = e instanceof IllegalStateException ? e.getMessage() :
                    "Native request failed (" + e.getClass().getSimpleName() + "). Check Diagnostics and permission settings.";
        } finally {
            try { switcher.recover(); }
            catch (RuntimeException e) {
                accepted = false;
                report += "\nVoice service recovery is pending. Open Permissions and check the ADB grant.";
            }
            String message = report;
            State.prefs(this).edit().putBoolean("awaiting", accepted).apply();
            State.result(this, message);
            if (!accepted) main.post(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
        }
    }

    private String reason(Capabilities c, TriggerStrategyManager.Mode mode, ModuleLink.Status module) {
        if (!c.googleInstalled) return "Google app missing or disabled in this Android profile.";
        if (mode == TriggerStrategyManager.Mode.SearchScreen) return "Enable RealCTS Search screen in Accessibility. This route needs an advertised Search screen action and Google as the firmware search provider.";
        if (mode == TriggerStrategyManager.Mode.Automatic) return "No native route available now. Open RealCTS → Setup without a computer, or select Google as digital assistant and try the RealCTS tile.";
        if (mode == TriggerStrategyManager.Mode.CSService) return module.service;
        if (mode == TriggerStrategyManager.Mode.CSHelper) return "Firmware lacks the configured Google contextual helper.";
        if (!c.visCandidate()) return "No eligible Google contextual search handler for this configuration.";
        if (c.googleVoice == null) return "Google has no eligible voice interaction service.";
        return "VIS needs Google active, or temporary switching enabled with the ADB grant.";
    }
}
