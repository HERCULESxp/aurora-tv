package dev.aurora.tv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Best effort only: Fire OS may prevent activities from opening at boot. */
public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        if (context.getSharedPreferences("aurora", Context.MODE_PRIVATE).getBoolean("settings_pause", false)) return;
        if (!context.getSharedPreferences("aurora", Context.MODE_PRIVATE).getBoolean("open_at_boot", false)) return;
        try {
            context.startActivity(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (RuntimeException ex) {
            context.getSharedPreferences("aurora", Context.MODE_PRIVATE).edit()
                .putString("redirect_notice", "O Fire OS bloqueou a abertura na inicialização. Abra o Aurora pela lista de apps.").apply();
        }
    }
}
