package com.realcts.app;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.*;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

final class Registration {
    static boolean held(Context c) {
        if (Build.VERSION.SDK_INT >= 29) {
            RoleManager r = c.getSystemService(RoleManager.class);
            if (r != null && r.isRoleAvailable(RoleManager.ROLE_ASSISTANT))
                return r.isRoleHeld(RoleManager.ROLE_ASSISTANT);
        }
        ComponentName a = Capabilities.component(Capabilities.readableSetting(c, "assistant"));
        return a != null && c.getPackageName().equals(a.getPackageName());
    }
    static void open(Activity a, boolean request) {
        if (request && Build.VERSION.SDK_INT >= 29) {
            RoleManager r = a.getSystemService(RoleManager.class);
            if (r != null && r.isRoleAvailable(RoleManager.ROLE_ASSISTANT) && !held(a)) {
                try { a.startActivityForResult(r.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT), 1); return; }
                catch (ActivityNotFoundException | SecurityException ignored) { }
            }
        }
        for (String action : new String[]{Settings.ACTION_VOICE_INPUT_SETTINGS,
                Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS, Settings.ACTION_SETTINGS}) {
            try { a.startActivity(new Intent(action)); return; }
            catch (ActivityNotFoundException | SecurityException ignored) { }
        }
        Toast.makeText(a, "Open Settings and search for Default apps or Digital assistant.", Toast.LENGTH_LONG).show();
    }
}
