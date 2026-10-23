// SPDX-License-Identifier: GPL-3.0-or-later
// Adapted from Homestead BootReceiver, Copyright (C) 2026 Homestead contributors.
package dev.aurora.tv;

import android.content.*;
import android.os.*;
import android.view.accessibility.AccessibilityManager;
import android.accessibilityservice.AccessibilityServiceInfo;

/** Wait for the home stack at boot. No legacy redirect/boot preferences. */
public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        if (!context.getSharedPreferences("aurora", Context.MODE_PRIVATE).getBoolean("homestead_home", true)) return;
        AccessibilityManager manager = (AccessibilityManager)context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        boolean enabled = false;
        for (AccessibilityServiceInfo info : manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
            if (info.getResolveInfo() != null && HomeRedirectService.class.getName().equals(info.getResolveInfo().serviceInfo.name)
                && context.getPackageName().equals(info.getResolveInfo().serviceInfo.packageName)) enabled = true;
        }
        if (!enabled) return;
        PendingResult result = goAsync();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try { context.startActivity(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
            catch (RuntimeException ignored) { /* The accessibility path remains available. */ }
            finally { result.finish(); }
        }, 6000);
    }
}
