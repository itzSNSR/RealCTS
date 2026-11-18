package com.realcts.app;

import android.content.*;
import android.graphics.Typeface;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.*;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Dashboard extends AppCompatActivity {
    private enum Page { Home, Setup, Permissions, Advanced, Diagnostics }
    private Page page = Page.Home;
    private int step;
    private LinearLayout body;
    private SharedPreferences prefs;
    private Capabilities caps;
    private ModuleLink.Status moduleStatus = ModuleLink.unavailable("Module checks disabled");
    private final ExecutorService probes = Executors.newSingleThreadExecutor();
    private int generation;
    private final OnBackPressedCallback back = new OnBackPressedCallback(false) {
        @Override public void handleOnBackPressed() {
            if (page == Page.Setup && step > 0) { step--; render(); }
            else go(Page.Home);
        }
    };
    private final SharedPreferences.OnSharedPreferenceChangeListener changed = (p, key) -> {
        if ("last".equals(key)) render();
    };
    private static final String GRANT = "adb shell pm grant com.realcts.app android.permission.WRITE_SECURE_SETTINGS";
    private static final String REVOKE = "adb shell pm revoke com.realcts.app android.permission.WRITE_SECURE_SETTINGS";
    private static final String[] STEPS = {"Check this device", "Check Google search", "Choose your assistant",
            "Choose a trigger", "Setup without a computer", "Test the Google overlay", "Test your gesture"};

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        getOnBackPressedDispatcher().addCallback(this, back);
        prefs = State.prefs(this);
        if (saved != null) { page = Page.valueOf(saved.getString("page", "Home")); step = saved.getInt("step"); }
        if (new VoiceInteractionSwitcher(this).pending()) recover(false);
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out); out.putString("page", page.name()); out.putInt("step", step);
    }
    @Override protected void onResume() {
        super.onResume();
        prefs.registerOnSharedPreferenceChangeListener(changed);
        refresh();
    }
    @Override protected void onPause() {
        prefs.unregisterOnSharedPreferenceChangeListener(changed);
        super.onPause();
    }
    @Override protected void onDestroy() { generation++; probes.shutdownNow(); super.onDestroy(); }
    private void refresh() {
        caps = new Capabilities(this);
        int token = ++generation;
        boolean enabled = prefs.getBoolean("module", false) && (State.mode(this) == TriggerStrategyManager.Mode.CSHelper || State.mode(this) == TriggerStrategyManager.Mode.CSService);
        moduleStatus = ModuleLink.unavailable(enabled ? "Checking System UI…" : "Module checks disabled");
        render();
        if (enabled) probes.execute(() -> {
            try {
                ModuleLink.Status result = ModuleLink.inspect(this);
                runOnUiThread(() -> { if (generation == token) { moduleStatus = result; render(); } });
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
    }
    private void go(Page next) { page = next; refresh(); }
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    private void render() {
        if (caps == null || isFinishing() || isDestroyed()) return;
        back.setEnabled(page != Page.Home);
        ScrollView scroll = new ScrollView(this);
        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(22), dp(18), dp(22), dp(24));
        scroll.addView(body);
        ViewCompat.setOnApplyWindowInsetsListener(scroll, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom); return insets;
        });
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(scroll);
        if (page != Page.Home) action("Back to dashboard", () -> go(Page.Home), false);
        switch (page) {
            case Home:
                title("RealCTS"); para("Native Google Circle to Search Assistant");
                section("Device status"); para(status());
                section("Last trigger"); para(last());
                action("Test Circle to Search", this::test, true);
                action("Setup without a computer", () -> { step = 4; go(Page.Setup); }, false);
                assistantChoice();
                action("Guided setup", () -> { step = 0; go(Page.Setup); }, false);
                action("Configure permissions", () -> go(Page.Permissions), false);
                action("Advanced settings", () -> go(Page.Advanced), false);
                action("Diagnostics", () -> go(Page.Diagnostics), false);
                break;
            case Setup: wizard(); break;
            case Permissions: permissions(); break;
            case Advanced: advanced(); break;
            case Diagnostics: diagnostics(); break;
        }
    }
    private void text(String s, int size, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s); v.setTextSize(size);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        v.setPadding(0, dp(bold ? 12 : 5), 0, dp(6));
        v.setTextIsSelectable(true); body.addView(v);
    }
    private void title(String s) { text(s, 30, true); }
    private void section(String s) { text(s, 19, true); }
    private void para(String s) { text(s, 16, false); }
    private void action(String label, Runnable task, boolean primary) {
        MaterialButton b = primary ? new MaterialButton(this) :
                new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        b.setText(label); b.setAllCaps(false); b.setOnClickListener(v -> task.run()); body.addView(b);
    }
    private void toggle(String label, String key) {
        MaterialSwitch control = new MaterialSwitch(this);
        control.setText(label); control.setMinHeight(dp(52)); control.setChecked(prefs.getBoolean(key, false));
        control.setOnCheckedChangeListener((b, checked) -> {
            prefs.edit().putBoolean(key, checked).apply(); refresh();
        });
        body.addView(control);
    }
    private String device() { return Build.MANUFACTURER + " " + Build.MODEL + " · Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")"; }
    private String route() {
        return TriggerStrategyManager.select(State.mode(this), caps, moduleStatus, prefs.getBoolean("switch", false)).name();
    }
    private String status() {
        return device() + "\nDefault assistant · " + (Registration.held(this) ? "Active" : "Inactive")
                + "\nGoogle app · " + (caps.googleInstalled ? "Detected" : "Missing or disabled")
                + "\nTrigger mode · " + State.mode(this) + "\nCandidate route · " + route()
                + "\nSecure settings · " + (caps.canWrite ? "Granted" : "No ADB grant")
                + "\nCompatibility · " + (prefs.getBoolean("paused", false) ? "Paused after visual test" :
                    "Unavailable".equals(route()) ? "No eligible route in this configuration" : "Requires visual test");
    }
    private String last() { return prefs.getString("last", "No request recorded"); }
    private void test() { startActivity(new Intent(this, LocalEntry.class)); }
    private void assistantChoice() {
        boolean independent = caps.helper || caps.googleTarget && SearchActionService.available();
        if (State.mode(this) != TriggerStrategyManager.Mode.Automatic) {
            para("A manual mode is selected. Direct VIS needs Google active. Choose RealCTS only if your selected route works with another assistant.");
            action("Set RealCTS as assistant", () -> Registration.open(this, true), false);
        } else if (!caps.googleActive) {
            para("For this setup, select Google in Digital assistant app. Return here to test, then use the RealCTS tile over another app.");
            action("Choose Google in Android settings", () -> Registration.open(this, false), true);
            if (independent) action("Set RealCTS as assistant", () -> Registration.open(this, true), false);
        } else {
            para("Google voice service is active. First test Circle to Search. If you can select text, turn on the separate assistant-panel shortcut below and enable RealCTS Search screen in Accessibility. A supported Google panel may flash briefly.");
            toggle("Open Circle to Search when Assistant opens", "assistantPanel");
            if (prefs.getBoolean("assistantPanel", false) && !SearchActionService.isConnected())
                action("Open Accessibility settings", this::openAccessibility, true);
            tileSetup();
        }
    }
    private void openAccessibility() {
        try { startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
        catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Open Android Settings, then Accessibility and RealCTS Search screen.", Toast.LENGTH_LONG).show();
        }
    }
    private void tileSetup() {
        if (Build.VERSION.SDK_INT < 33) {
            para("Open Quick Settings, tap Edit and add Circle to Search.");
            return;
        }
        action("Add Circle to Search tile", () -> {
            android.app.StatusBarManager bar = getSystemService(android.app.StatusBarManager.class);
            if (bar == null) return;
            bar.requestAddTileService(new ComponentName(this, SearchTile.class), "Circle to Search",
                    android.graphics.drawable.Icon.createWithResource(this, R.drawable.tile), getMainExecutor(),
                    result -> Toast.makeText(this, result == android.app.StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED
                            || result == android.app.StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED
                            ? "Circle to Search is in Quick Settings." : "Open Quick Settings, tap Edit and add Circle to Search.", Toast.LENGTH_LONG).show());
        }, false);
    }
    private void wizard() {
        para("Step " + (step + 1) + " of " + STEPS.length); title(STEPS[step]);
        switch (step) {
            case 0: para(device()); para("If holding Home or the navigation handle already opens Google’s selection overlay, you can keep your current assistant."); break;
            case 1: para(caps.googleInstalled ? "Google app is enabled." : "Install or enable the Google app in this profile.");
                para(caps.nativeActivity ? "A Google contextual search activity is visible." : "No matching Google contextual activity is visible."); break;
            case 2: assistantChoice();
                action("Open Android assistant settings", () -> Registration.open(this, false), false); break;
            case 3: para("Automatic mode checks the firmware helper, the native Search screen action, and Google’s active voice service. Current route · " + route());
                action("Choose mode", this::chooseMode, false); break;
            case 4: para("Recommended for most users. Keep Google as the digital assistant and choose Automatic. No ADB permission is needed. Continue to test the overlay, then return here to set up your gesture.");
                if (State.mode(this) != TriggerStrategyManager.Mode.Automatic)
                    action("Use Automatic mode", () -> { prefs.edit().putString("mode", "Automatic").apply(); refresh(); }, true);
                if (State.mode(this) == TriggerStrategyManager.Mode.Automatic) assistantChoice();
                action("Open assistant settings", () -> Registration.open(this, false), false);
                para("Candidate route · " + route());
                action("Recheck this phone", this::refresh, false); break;
            case 5: para("Run a test. Look for Google’s search bar and try circling text. Close the overlay with Back.");
                action("Test Circle to Search", this::test, true); break;
            case 6: para("Open a different app. Use the configured Home hold or assistant gesture, circle an item, then press Back. If the gesture is reserved by the phone, try the RealCTS Quick Settings tile.");
                action("View diagnostics", () -> go(Page.Diagnostics), false); break;
        }
        action(step == 6 ? "Finish setup" : "Continue", () -> { if (step == 6) go(Page.Home); else { step++; render(); } }, true);
        if (step > 0) action("Previous step", () -> { step--; render(); }, false);
    }
    private void permissionControls() {
        para("Only manual VIS with temporary switching needs this setup. For Automatic or an already-active Google voice service, skip the grant.");
        para("ADB permission on this phone · " + (caps.canWrite ? "Granted" : "Absent"));
        toggle("Allow temporary Google voice-service switching", "switch");
        para("To use switching, select VIS in Advanced settings, allow switching above, then run this command from an authorized computer. The grant applies to this installation on this phone.");
        para(GRANT);
    }
    private void permissions() {
        title("Permission setup"); permissionControls();
        para("RealCTS saves the exact voice_interaction_service value before switching. After the request it restores that value if the assistant and temporary service still match. A scheduled recovery job can retry after process death.");
        section("Revoke access"); para("Finish any pending recovery, turn off temporary switching, then run");
        para(REVOKE); action("Review Android assistant settings", () -> Registration.open(this, false), false);
    }
    private void advanced() {
        title("Advanced settings");
        action("Trigger mode · " + State.mode(this), this::chooseMode, false);
        para(modeHelp(State.mode(this)));
        if (State.mode(this) == TriggerStrategyManager.Mode.SearchScreen)
            action("Open Accessibility settings", this::openAccessibility, false);
        para("Current candidate · " + route() + ". An unavailable route needs additional setup or firmware support. Confirm an eligible route with the dashboard test.");
        action("Invocation delay · " + prefs.getInt("delay", 0) + " ms", () -> {
            int[] values = {0, 150, 300, 600, 1000};
            new MaterialAlertDialogBuilder(this).setTitle("Invocation delay")
                .setItems(new String[]{"0 ms", "150 ms", "300 ms", "600 ms", "1000 ms"}, (d, i) -> {
                    prefs.edit().putInt("delay", values[i]).apply(); render();
                }).show();
        }, false);
        toggle("Haptic feedback after accepted request", "haptic");
        toggle("Write local diagnostic messages to Logcat", "debug");
        toggle("Check optional LSPosed System UI integration", "module");
        para("On an eligible rooted build, enable RealCTS for System UI in an API 101-compatible LSPosed manager and restart System UI. The system must grant System UI access to contextual search.");
        para("Module helper · " + moduleStatus.helper + "\nModule service · " + moduleStatus.service);
        action("Permission setup", () -> go(Page.Permissions), false);
        action("Recover saved voice service", () -> recover(true), false);
        action("Choose assistant in Android settings", () -> Registration.open(this, false), false);
    }
    private void chooseMode() {
        TriggerStrategyManager.Mode[] modes = TriggerStrategyManager.Mode.values();
        String[] labels = new String[modes.length];
        for (int i = 0; i < modes.length; i++) labels[i] = modes[i].name() + "\n" + modeHelp(modes[i]);
        ArrayAdapter<String> choices = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, labels) {
            @Override public View getView(int position, View recycled, ViewGroup parent) {
                TextView row = (TextView) super.getView(position, recycled, parent);
                row.setSingleLine(false);
                row.setLayoutParams(new AbsListView.LayoutParams(-1, -2));
                row.setPaddingRelative(dp(16), dp(10), dp(16), dp(10));
                return row;
            }
        };
        new MaterialAlertDialogBuilder(this).setTitle("Trigger mode").setSingleChoiceItems(choices, State.mode(this).ordinal(), (d, i) -> {
            prefs.edit().putString("mode", modes[i].name()).apply(); d.dismiss(); refresh();
        }).show();
    }
    private static String modeHelp(TriggerStrategyManager.Mode mode) {
        switch (mode) {
            case Automatic: return "Recommended. Checks native routes without ADB or LSPosed.";
            case VIS: return "Uses active Google voice service. Optional switching requires ADB authorization.";
            case CSHelper: return "Requires a firmware helper configured for Google. Some builds can use an optional System UI module.";
            case CSService: return "Requires a system search service and compatible LSPosed integration.";
            case SearchScreen: return "Android 11+. Enable RealCTS Search screen in Accessibility. The launcher must offer the native search action.";
            default: throw new AssertionError(mode);
        }
    }
    private void diagnostics() {
        title("Diagnostics"); para(status()); section("Native capabilities");
        para("Google voice component · " + (caps.googleVoice == null ? "Unavailable" : caps.googleVoice.flattenToShortString())
                + "\nGoogle voice active · " + caps.googleActive + "\nGoogle contextual activity visible · " + caps.nativeActivity
                + "\nFirmware helper configured · " + caps.helper + "\nModule helper · " + moduleStatus.helper + "\nModule service · " + moduleStatus.service
                + "\nPending voice-service restoration · " + new VoiceInteractionSwitcher(this).pending());
        section("Last trigger"); para(last());
        if (prefs.getBoolean("awaiting", false)) {
            para("What appeared on screen?");
            action("I saw Google’s selection overlay", () -> visual(true), true);
            action("Different interface or no overlay", () -> visual(false), false);
        }
        if (prefs.getBoolean("paused", false)) action("Allow another manual test", () -> {
            prefs.edit().putBoolean("paused", false).apply(); render();
        }, false);
        para("For a gesture failure, check whether the last trigger time changed. If it stayed the same, verify the default assistant and the phone’s navigation gesture settings.");
        action("Test again", this::test, false);
    }
    private void visual(boolean ok) {
        prefs.edit().putBoolean("awaiting", false).putBoolean("paused", !ok).apply();
        State.result(this, ok ? "User confirmed Google’s selection overlay." : "User reported missing or different UI. Triggers paused.");
    }
    private void recover(boolean manual) {
        TriggerService.WORK.execute(() -> {
            VoiceInteractionSwitcher tx = new VoiceInteractionSwitcher(this);
            boolean pending = tx.pending();
            try {
                tx.recover();
                if (manual || pending) State.result(this, pending ? "Recovery check completed. Review assistant settings." : "No pending restoration.");
            } catch (RuntimeException e) { State.result(this, "Recovery pending. Check the ADB grant and assistant settings."); }
        });
    }
}
