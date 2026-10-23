// SPDX-License-Identifier: GPL-3.0-or-later
package dev.aurora.tv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/** The normal activity and the accessibility window share AuroraHome's original UI. */
public class MainActivity extends Activity {
    private AuroraHome home;
    private long handoff = -1;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        handoff = getIntent().getLongExtra("home_epoch", -1);
        home = new AuroraHome(this, new AuroraHome.Host() {
            public void open(Intent intent, boolean settings) {
                if (settings) HomeRedirectService.suspendForSettings();
                startActivity(intent);
            }
            public void dialogVisible(boolean visible) { }
        }, false);
        setContentView(home.root);
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent); setIntent(intent);
        handoff = intent.getLongExtra("home_epoch", -1);
        if (handoff < 0) home.home();
    }
    @Override protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        HomeRedirectService.returnedToActivity();
        if (!HomeRedirectService.hasOverlay()) home.refresh();
        AuroraHome.State state = HomeRedirectService.overlayState();
        if (state != null) home.restore(state);
        final long token = handoff;
        home.root.getViewTreeObserver().addOnPreDrawListener(new android.view.ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                if (home.root.getViewTreeObserver().isAlive()) home.root.getViewTreeObserver().removeOnPreDrawListener(this);
                home.root.post(() -> HomeRedirectService.activityReady(token, home));
                return true;
            }
        });
    }
    @Override protected void onPause() { HomeRedirectService.activityLeaving(); super.onPause(); }
    @Override protected void onDestroy() { home.closeDialogs(); super.onDestroy(); }
    @Override public void onBackPressed() { home.back(); }
}
