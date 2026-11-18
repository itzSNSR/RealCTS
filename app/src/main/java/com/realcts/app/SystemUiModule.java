package com.realcts.app;

import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import io.github.libxposed.api.XposedModule;

/** Optional user-enabled module. Android checks System UI's existing service permission. */
public final class SystemUiModule extends XposedModule {
    @Override public void onPackageReady(PackageReadyParam param) {
        if (Build.VERSION.SDK_INT < 34 || !param.isFirstPackage()
                || !ModuleLink.SYSTEM_UI.equals(param.getPackageName())
                || !ModuleLink.SYSTEM_UI.equals(Application.getProcessName())) return;
        try {
            hook(Application.class.getDeclaredMethod("onCreate")).intercept(chain -> {
                Object result = chain.proceed();
                Application app = (Application) chain.getThisObject();
                IntentFilter filter = new IntentFilter(ModuleLink.PROBE);
                filter.addAction(ModuleLink.START);
                filter.addAction(ModuleLink.CHECK_HELPER);
                filter.addAction(ModuleLink.START_HELPER);
                app.registerReceiver(new NativeRequest(), filter, ModuleLink.PERMISSION, null, Context.RECEIVER_EXPORTED);
                return result;
            });
        } catch (ReflectiveOperationException e) {
            android.util.Log.w("RealCTS", "System UI lifecycle unavailable", e);
        }
    }

    private static final class NativeRequest extends BroadcastReceiver {
        @Override public void onReceive(Context c, Intent intent) {
            try {
                if (SystemClock.elapsedRealtime() > intent.getLongExtra(ModuleLink.DEADLINE, 0))
                    throw new IllegalStateException("System UI request expired");
                String action = intent.getAction();
                if (ModuleLink.CHECK_HELPER.equals(action) || ModuleLink.START_HELPER.equals(action)) {
                    Capabilities caps = new Capabilities(c);
                    if (!caps.helper || Build.VERSION.SDK_INT > 36)
                        throw new IllegalStateException("No configured Google contextual helper");
                    if (ModuleLink.START_HELPER.equals(action)) {
                        if (c.getSystemService(KeyguardManager.class).isDeviceLocked())
                            throw new IllegalStateException("Unlock the screen to search");
                        if (!NativeCircleTrigger.invoke(c, caps, true))
                            throw new IllegalStateException("Android rejected the helper request");
                    }
                    setResultCode(Activity.RESULT_OK);
                    return;
                }
                if (!Capabilities.GOOGLE.equals(Capabilities.resource(c, "config_defaultContextualSearchPackageName"))
                        || !Capabilities.resolves(c.getPackageManager(), Capabilities.SERVICE_ACTION, PackageManager.MATCH_SYSTEM_ONLY))
                    throw new IllegalStateException("Firmware has no configured Google system search activity");
                if (c.checkSelfPermission("android.permission.ACCESS_CONTEXTUAL_SEARCH") != PackageManager.PERMISSION_GRANTED)
                    throw new IllegalStateException("System UI lacks contextual search permission");
                Object manager = c.getSystemService(Class.forName("android.app.contextualsearch.ContextualSearchManager"));
                if (manager == null) throw new IllegalStateException("Contextual search service unavailable");
                java.lang.reflect.Method start = manager.getClass().getMethod("startContextualSearch", int.class);
                if (ModuleLink.START.equals(intent.getAction())) {
                    if (c.getSystemService(KeyguardManager.class).isDeviceLocked())
                        throw new IllegalStateException("Unlock the screen to search");
                    start.invoke(manager, 1);
                }
                setResultCode(Activity.RESULT_OK);
            } catch (Exception | LinkageError e) {
                Throwable cause = e.getCause() == null ? e : e.getCause();
                setResult(Activity.RESULT_CANCELED, cause instanceof IllegalStateException ? cause.getMessage() :
                        "System UI rejected the request (" + cause.getClass().getSimpleName() + ")", null);
            }
        }
    }
}
