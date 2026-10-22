package dev.aurora.tv;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import java.util.HashSet;
import java.util.Set;

/** Observes window metadata only; never reads text, view trees or key events. */
public class HomeRedirectService extends AccessibilityService {
    static final String AMAZON = "com.amazon.tv.launcher";
    private final Set<String> homeActivities = new HashSet<>();
    private final RedirectGuard guard = new RedirectGuard();
    @Override protected void onServiceConnected() {
        homeActivities.clear();
        // Actual HOME window confirmed on the user's Fire OS 7.7.1.6.
        try {
            android.content.pm.ActivityInfo known = getPackageManager().getActivityInfo(
                new android.content.ComponentName(AMAZON, AMAZON + ".ui.HomeActivity_vNext"), 0);
            if (known.enabled && known.exported) homeActivities.add(known.name);
        } catch (android.content.pm.PackageManager.NameNotFoundException ignored) {
            // Other firmware versions use the HOME activities resolved below.
        }
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setPackage(AMAZON);
        for (ResolveInfo info : getPackageManager().queryIntentActivities(home, 0)) {
            if (info.activityInfo != null && AMAZON.equals(info.activityInfo.packageName)) {
                homeActivities.add(info.activityInfo.name);
            }
        }
        getSharedPreferences("aurora", MODE_PRIVATE).edit()
            .putBoolean("home_detected", !homeActivities.isEmpty()).apply();
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        String pkg = event.getPackageName() == null ? "" : event.getPackageName().toString();
        String activity = event.getClassName() == null ? "" : event.getClassName().toString();
        SharedPreferences prefs = getSharedPreferences("aurora", MODE_PRIVATE);
        if (prefs.getBoolean("settings_pause", false)) return;
        boolean home = AMAZON.equals(pkg) && homeActivities.contains(activity);
        RedirectGuard.Decision decision = guard.decide(SystemClock.elapsedRealtime(), prefs.getBoolean("redirect_home", false), home);
        if (decision == RedirectGuard.Decision.PAUSE) {
            prefs.edit().putBoolean("redirect_home", false).putString("redirect_notice", "Redirecionamento pausado: o sistema voltou repetidamente ao launcher Amazon. Você pode reativar nas configurações.").apply();
        } else if (decision == RedirectGuard.Decision.OPEN) {
            try {
                startActivity(new Intent(this, MainActivity.class)
                    .putExtra("aurora_home_redirect", true)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION));
            } catch (RuntimeException ex) {
                prefs.edit().putBoolean("redirect_home", false).putString("redirect_notice", "O sistema recusou a abertura do Aurora em segundo plano. Redirecionamento desativado.").apply();
            }
        }
    }
    @Override public void onInterrupt() { }
}
