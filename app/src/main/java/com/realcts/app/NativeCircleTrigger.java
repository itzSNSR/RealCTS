/*
 * Copyright 2025 Andrew Gunnerson (CTSLauncher)
 * Adapted 2026 from CTSLauncher and parallelcc/MiCTS. SPDX-License-Identifier: GPL-3.0-only
 */
package com.realcts.app;

import android.content.Context;
import android.os.*;

final class NativeCircleTrigger {
    private static final String DESCRIPTOR = "com.android.internal.app.IVoiceInteractionManagerService";

    static IBinder voiceManager() throws ReflectiveOperationException {
        Class<?> manager = Class.forName("android.os.ServiceManager");
        return (IBinder) manager.getMethod("getService", String.class).invoke(null, "voiceinteraction");
    }

    static boolean invoke(Context c, Capabilities caps, boolean helper) throws Exception {
        IBinder service = voiceManager();
        if (service == null || !DESCRIPTOR.equals(service.getInterfaceDescriptor()))
            throw new IllegalStateException("Voice interaction manager unavailable");
        Bundle args = new Bundle();
        args.putLong("invocation_time_ms", SystemClock.elapsedRealtime());
        args.putInt("omni.entry_point", 1);
        // Direct Google requests use the upstream payload. Let the firmware select its handler.
        if (helper) {
            args.putInt(caps.key, 1);
            args.putBoolean(caps.legacyKey, false);
            if (!caps.helperEnabledKey.isEmpty()) args.putBoolean(caps.helperEnabledKey, true);
        }

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeStrongBinder(null);
            data.writeInt(1);
            args.writeToParcel(data, 0);
            data.writeInt(7); // MiCTS triggerCircleToSearch flags
            if (Build.VERSION.SDK_INT >= 34) data.writeString(c.getAttributionTag());
            if (!service.transact(IBinder.FIRST_CALL_TRANSACTION + 2, data, reply, 0))
                throw new IllegalStateException("Voice manager rejected the interface transaction");
            reply.readException();
            return reply.readInt() != 0;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}
