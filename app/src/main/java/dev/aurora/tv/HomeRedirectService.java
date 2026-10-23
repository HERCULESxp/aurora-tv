/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Adapted from Homestead HomeRedirectService.
 * Copyright (C) 2026 Homestead contributors.
 * Aurora adaptation (2026): original Aurora UI, no VPN/ADB/debloat, bounded overlay
 * lifetime, explicit HOME activity fallback, shared view state and cleanup.
 * Distributed WITHOUT ANY WARRANTY. See LICENSE and THIRD_PARTY_NOTICES.md.
 */
package dev.aurora.tv;

import android.accessibilityservice.AccessibilityService;
import android.content.*;
import android.graphics.PixelFormat;
import android.os.*;
import android.view.*;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

/** Show the usable launcher as a window, then hand off after the app-switch lock. */
public class HomeRedirectService extends AccessibilityService {
    private static final String AMAZON = "com.amazon.tv.launcher";
    private static final long HANDOFF_MS = 5200, REVEAL_MS = 400;
    private static HomeRedirectService instance;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final HomeSession session = new HomeSession();
    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private AuroraHome ui;
    private boolean bound, watcherReady, suspended, closing, dialogOpen, pickedLaunched;
    private long shownAt, lastShow = -1000;
    private String foreground;
    private Runnable pendingOpen, pendingHide;
    private android.content.SharedPreferences prefs;
    private final android.content.SharedPreferences.OnSharedPreferenceChangeListener changes = (p, key) -> {
        if ("homestead_home".equals(key)) configurationChanged();
    };
    private final Messenger messages = new Messenger(new Handler(Looper.getMainLooper()) {
        @Override public void handleMessage(Message message) {
            if (message.what != HomeKeyWatcher.MSG_HOME || !enabled()) return;
            int uid = message.arg1;
            if (uid != 0 && uid != android.os.Process.SYSTEM_UID && !isForegroundUid(uid)) return;
            // A verified HOME start, unlike a settings-window event, ends a temporary pause.
            suspended = false;
            showHome();
        }
    });
    private final ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder) {
            try {
                Message message = Message.obtain(null, HomeKeyWatcher.MSG_REGISTER);
                message.replyTo = messages; new Messenger(binder).send(message); watcherReady = true;
            } catch (RemoteException e) { watcherReady = false; }
        }
        public void onServiceDisconnected(ComponentName name) { watcherReady = false; }
    };
    private boolean enabled() { return prefs != null && prefs.getBoolean("homestead_home", true); }
    private boolean isForegroundUid(int uid) {
        if (foreground == null || AMAZON.equals(foreground) || getPackageName().equals(foreground)) return false;
        String[] packages = getPackageManager().getPackagesForUid(uid);
        if (packages != null) for (String pkg : packages) if (pkg.equals(foreground)) return true;
        return false;
    }
    @Override protected void onServiceConnected() {
        instance = this; wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        prefs = getSharedPreferences("aurora", MODE_PRIVATE);
        // The old redirect/boot/pause preferences must not drive this new implementation.
        prefs.edit().remove("redirect_home").remove("open_at_boot").remove("settings_pause")
            .remove("redirect_notice").remove("home_detected").apply();
        prefs.registerOnSharedPreferenceChangeListener(changes);
        prepareUi(); bindWatcher();
    }
    private void prepareUi() {
        if (ui != null) return;
        ui = new AuroraHome(this, new AuroraHome.Host() {
            public void open(Intent intent, boolean settings) { pick(intent, settings); }
            public void dialogVisible(boolean visible) {
                dialogOpen = visible;
                if (closing) return;
                cancelOpen();
                if (!visible && session.handoff(session.token())) scheduleHandoff();
            }
        }, true);
    }
    private void bindWatcher() {
        if (bound || !enabled() || !HomeKeyWatcher.permitted(this)) return;
        try { bound = bindService(new Intent(this, HomeKeyWatcher.class), connection, BIND_AUTO_CREATE); }
        catch (RuntimeException e) { watcherReady = false; }
    }
    private void unbindWatcher() {
        if (bound) { unbindService(connection); bound = false; }
        watcherReady = false;
    }
    static boolean connected() { return instance != null; }
    static boolean watcherConnected() { return instance != null && instance.watcherReady; }
    static boolean hasOverlay() { return instance != null && instance.session.active(); }
    static AuroraHome.State overlayState() { return hasOverlay() ? instance.ui.capture() : null; }
    static void configurationChanged() {
        HomeRedirectService s = instance;
        if (s == null) return;
        if (!s.enabled()) { s.close(); s.unbindWatcher(); }
        else s.bindWatcher();
    }
    static void returnedToActivity() {
        HomeRedirectService s = instance;
        if (s != null) { s.suspended = false; s.bindWatcher(); }
    }
    static void suspendForSettings() {
        HomeRedirectService s = instance;
        if (s != null) { s.suspended = true; s.close(); }
    }
    static void activityLeaving() {
        HomeRedirectService s = instance;
        if (s != null && s.pendingHide != null) { s.handler.removeCallbacks(s.pendingHide); s.pendingHide = null; }
    }
    static void activityReady(long token, AuroraHome activityUi) {
        HomeRedirectService s = instance;
        if (s == null || !s.session.handoff(token)) return;
        s.pendingHide = () -> {
            s.pendingHide = null;
            if (!s.session.handoff(token)) return;
            activityUi.restore(s.ui.capture());
            s.close();
        };
        s.handler.postDelayed(s.pendingHide, 160);
    }
    private void showHome() {
        if (!enabled() || session.active()) return;
        long now = SystemClock.elapsedRealtime();
        if (now - lastShow < 350) return;
        lastShow = now;
        try {
            prepareUi(); ui.home();
            params = new WindowManager.LayoutParams(-1, -1,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.OPAQUE);
            ui.root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            wm.addView(ui.root, params);
            session.begin(); pickedLaunched = false; shownAt = now;
            ui.root.requestFocus();
            scheduleHandoff();
            long token = session.token();
            // Never trap the user under a focused overlay if the activity fails to open.
            handler.postDelayed(() -> { if (session.valid(token)) { close(); Toast.makeText(this, "Não foi possível concluir a abertura do Aurora", Toast.LENGTH_LONG).show(); } }, 15000);
        } catch (RuntimeException e) {
            close(); openActivity(-1);
        }
    }
    private void scheduleHandoff() {
        if (!session.active() || session.picked() || dialogOpen) return;
        cancelOpen();
        long token = session.token();
        pendingOpen = () -> { pendingOpen = null; if (session.handoff(token) && !dialogOpen) openActivity(token); };
        handler.postDelayed(pendingOpen, Math.max(0, HANDOFF_MS - (SystemClock.elapsedRealtime() - shownAt)));
    }
    private void openActivity(long token) {
        try { startActivity(new Intent(this, MainActivity.class).putExtra("home_epoch", token)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION)); }
        catch (RuntimeException e) { close(); }
    }
    private void pick(Intent intent, boolean settings) {
        if (!session.active()) return;
        session.pick(); cancelOpen();
        if (pendingHide != null) { handler.removeCallbacks(pendingHide); pendingHide = null; }
        suspended = settings;
        long token = session.token();
        // Queue only our callback, not an Android activity start that cannot be cancelled.
        long wait = Math.max(0, HANDOFF_MS - (SystemClock.elapsedRealtime() - shownAt));
        handler.postDelayed(() -> {
            if (!session.valid(token)) return;
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            try {
                wm.updateViewLayout(ui.root, params);
                pickedLaunched = true;
                startActivity(new Intent(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (RuntimeException e) { close(); Toast.makeText(this, "Não foi possível abrir esta opção", Toast.LENGTH_LONG).show(); }
            handler.postDelayed(() -> { if (session.valid(token)) close(); }, 8000);
        }, wait);
    }
    private void cancelOpen() { if (pendingOpen != null) { handler.removeCallbacks(pendingOpen); pendingOpen = null; } }
    private void close() {
        closing = true; session.close(); handler.removeCallbacksAndMessages(null);
        pendingOpen = null; pendingHide = null;
        if (ui != null) {
            ui.closeDialogs();
            if (ui.root.getParent() != null) try { wm.removeViewImmediate(ui.root); } catch (RuntimeException ignored) { }
        }
        dialogOpen = false; closing = false;
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.getPackageName() == null) return;
        String pkg = event.getPackageName().toString();
        String cls = event.getClassName() == null ? "" : event.getClassName().toString();
        // Overlay/dialog container events are not foreground activity changes.
        if (cls.startsWith("android.widget.") || cls.startsWith("android.view.")) return;
        foreground = pkg;
        if (getPackageName().equals(pkg)) return;
        if (session.active()) {
            if (!AMAZON.equals(pkg) || (pickedLaunched && (suspended || !cls.equals(AMAZON + ".ui.HomeActivity_vNext")))) {
                long token = session.token();
                handler.postDelayed(() -> { if (session.valid(token)) close(); }, REVEAL_MS);
            }
            return;
        }
        if (enabled() && !suspended && AMAZON.equals(pkg) && cls.equals(AMAZON + ".ui.HomeActivity_vNext")) showHome();
    }
    @Override public void onInterrupt() { close(); }
    @Override public void onDestroy() {
        close(); unbindWatcher();
        if (prefs != null) prefs.unregisterOnSharedPreferenceChangeListener(changes);
        if (instance == this) instance = null;
        super.onDestroy();
    }
}
