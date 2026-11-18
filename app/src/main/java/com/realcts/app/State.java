package com.realcts.app;

import android.content.*;
import android.util.Log;
import java.text.DateFormat;
import java.util.Date;

final class State {
    static SharedPreferences prefs(Context c) { return c.getSharedPreferences("realcts", 0); }
    static TriggerStrategyManager.Mode mode(Context c) {
        try { return TriggerStrategyManager.Mode.valueOf(prefs(c).getString("mode", "Automatic")); }
        catch (IllegalArgumentException e) { return TriggerStrategyManager.Mode.Automatic; }
    }
    static void result(Context c, String text) {
        SharedPreferences p = prefs(c);
        p.edit().putString("last", DateFormat.getDateTimeInstance().format(new Date()) + "\n" + text).apply();
        if (p.getBoolean("debug", false)) Log.i("RealCTS", text);
    }
}
