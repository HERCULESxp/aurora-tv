package dev.aurora.tv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Offline TV home. Uses only Android framework APIs: no ads, tracking or network. */
public class MainActivity extends Activity {
    private final List<App> apps = new ArrayList<>();
    private SharedPreferences prefs;
    private LinearLayout page, menu;
    private String section = "Início", query = "";
    private View firstCard;
    private boolean reuseHomeOnResume, externalAppOpened;
    private int accent = Color.rgb(139, 197, 255);
    private static final int WHITE = 0xfff3f6fc, MUTED = 0xff9aa8bc;

    private static class App {
        String pkg, name;
        Intent intent;
        ResolveInfo info;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("aurora", MODE_PRIVATE);
        if (state != null) section = state.getString("section", "Início");
    }
    @Override protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        boolean reuse = reuseHomeOnResume && page != null;
        reuseHomeOnResume = false;
        externalAppOpened = false;
        if (!reuse) {
            discoverApps();
            render();
        }
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("section", section);
        super.onSaveInstanceState(state);
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        reuseHomeOnResume = intent.getBooleanExtra("aurora_home_redirect", false)
            && "Início".equals(section) && page != null && !externalAppOpened;
        section = "Início"; query = "";
        if (intent.getBooleanExtra("aurora_home_redirect", false)) overridePendingTransition(0, 0);
    }
    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private void discoverApps() {
        apps.clear();
        Set<String> seen = new HashSet<>();
        for (String category : new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER}) {
            Intent filter = new Intent(Intent.ACTION_MAIN).addCategory(category);
            for (ResolveInfo ri : getPackageManager().queryIntentActivities(filter, 0)) {
                String pkg = ri.activityInfo.packageName;
                if (!ri.activityInfo.exported || pkg.equals(getPackageName()) || !seen.add(pkg)) continue;
                App a = new App(); a.pkg = pkg; a.name = ri.loadLabel(getPackageManager()).toString(); a.info = ri;
                a.intent = new Intent(Intent.ACTION_MAIN).addCategory(category)
                    .setClassName(pkg, ri.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                apps.add(a);
            }
        }
        java.text.Collator collator = java.text.Collator.getInstance(new Locale("pt", "BR"));
        Collections.sort(apps, (a, b) -> collator.compare(a.name, b.name));
    }
    private GradientDrawable bg(int color, int stroke) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(10));
        if (stroke != 0) d.setStroke(dp(2), stroke);
        return d;
    }
    private TextView text(String s, int size, int color) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setFontFeatureSettings("kern"); return t;
    }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private void focusStyle(View v, int normal) {
        v.setFocusable(true); v.setFocusableInTouchMode(true); v.setBackground(bg(normal, 0));
        v.setOnFocusChangeListener((view, focused) -> {
            view.setBackground(bg(focused ? 0xff263a53 : normal, focused ? accent : 0));
            view.animate().scaleX(focused ? 1.035f : 1f).scaleY(focused ? 1.035f : 1f).setDuration(120).start();
            view.setElevation(focused ? dp(8) : 0);
        });
    }
    private TextView button(String title, Runnable action) {
        TextView t = text(title, 14, WHITE); t.setGravity(Gravity.CENTER);
        t.setPadding(dp(18), dp(12), dp(18), dp(12)); focusStyle(t, 0xff1b2534);
        t.setOnClickListener(v -> action.run()); return t;
    }
    private void render() {
        firstCard = null;
        FrameLayout root = new FrameLayout(this); root.setBackgroundColor(0xff080d15);
        LinearLayout layout = new LinearLayout(this); layout.setPadding(dp(22), dp(18), dp(22), dp(18));
        root.addView(layout, new FrameLayout.LayoutParams(-1, -1));
        menu = column(); menu.setPadding(0, dp(10), dp(18), 0);
        menu.setBackgroundColor(0xff0b111b);
        layout.addView(menu, new LinearLayout.LayoutParams(dp(148), -1));
        TextView logo = text("Λ  AURORA", 21, accent); logo.setPadding(dp(12), 0, 0, dp(26)); menu.addView(logo);
        for (String label : new String[]{"Início", "Apps", "Favoritos", "Recentes", "Buscar"}) {
            String icon = label.equals("Início") ? "⌂" : label.equals("Favoritos") ? "☆" : label.equals("Recentes") ? "◷" : label.equals("Buscar") ? "⌕" : "▦";
            TextView item = button(icon + "   " + label, () -> { section = label; query = ""; render(); });
            item.setGravity(Gravity.CENTER_VERTICAL); item.setTextSize(13);
            if (section.equals(label)) { item.setTextColor(accent); item.setBackground(bg(0xff202c3c, 0)); }
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(44)); p.bottomMargin = dp(7); menu.addView(item, p);
        }
        menu.addView(new View(this), new LinearLayout.LayoutParams(1, 0, 1));
        menu.addView(button("⚙  Configurações", this::settings));
        LinearLayout body = column(); body.setPadding(dp(24), 0, 0, 0); layout.addView(body, new LinearLayout.LayoutParams(0, -1, 1));
        LinearLayout status = new LinearLayout(this); status.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = text("SEU ESPAÇO. SEU RITMO.", 10, MUTED); brand.setLetterSpacing(.16f);
        status.addView(brand, new LinearLayout.LayoutParams(0, dp(27), 1));
        TextClock clock = new TextClock(this); clock.setFormat24Hour("HH:mm"); clock.setFormat12Hour("HH:mm"); clock.setTextColor(WHITE); clock.setTextSize(14); status.addView(clock);
        body.addView(status);
        if (section.equals("Buscar")) {
            EditText input = new EditText(this); input.setSingleLine(true); input.setTextColor(WHITE); input.setHintTextColor(MUTED);
            input.setHint("Buscar aplicativos instalados"); input.setText(query); body.addView(input);
            input.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) { query = s.toString(); populate(); }
                public void afterTextChanged(Editable s) {}
            });
        }
        ScrollView scroll = new ScrollView(this); scroll.setClipToPadding(false); scroll.setFillViewport(true);
        page = column(); page.setPadding(dp(4), dp(8), dp(8), dp(16)); scroll.addView(page); body.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        TextView hint = text("OK  Abrir     •     Segure OK / ☰  Opções     •     Voltar  Início", 10, MUTED);
        hint.setPadding(0, dp(7), 0, 0); body.addView(hint);
        populate(); setContentView(root);
        if (firstCard != null) firstCard.requestFocus(); else menu.getChildAt(1).requestFocus();
    }
    private List<App> favorites() {
        Set<String> favorite = prefs.getStringSet("favorites", Collections.emptySet());
        List<App> result = new ArrayList<>(); for (App a : apps) if (favorite.contains(a.pkg)) result.add(a); return result;
    }
    private List<App> recent() {
        List<App> result = new ArrayList<>();
        for (String pkg : prefs.getString("recent", "").split(",")) for (App a : apps) if (a.pkg.equals(pkg)) result.add(a);
        return result;
    }
    private void populate() {
        page.removeAllViews(); firstCard = null;
        switch (section) {
            case "Início":
                hero();
                List<App> fav = favorites();
                heading(fav.isEmpty() ? "Seus apps" : "Seus favoritos"); row(fav.isEmpty() ? apps : fav);
                heading("Acessados recentemente");
                List<App> history = recent();
                if (history.isEmpty()) empty("Seus últimos apps abertos pelo Aurora aparecerão aqui."); else row(history);
                break;
            case "Favoritos": heading("Sua seleção"); grid(favorites(), "Segure OK em um app para adicioná-lo aos favoritos."); break;
            case "Recentes": heading("Acessados recentemente"); grid(recent(), "Abra um aplicativo pelo Aurora para começar."); break;
            default:
                heading(section.equals("Buscar") ? "Resultados" : "Todos os aplicativos");
                List<App> filtered = new ArrayList<>();
                for (App a : apps) if (a.name.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) filtered.add(a);
                grid(filtered, "Nenhum aplicativo encontrado.");
        }
    }
    private void hero() {
        FrameLayout hero = new FrameLayout(this);
        hero.addView(new Landscape(this), new FrameLayout.LayoutParams(-1, -1));
        LinearLayout copy = column(); copy.setPadding(dp(24), dp(18), dp(24), dp(16));
        TextView eyebrow = text("BEM-VINDO À SUA TV", 10, accent); eyebrow.setLetterSpacing(.18f); copy.addView(eyebrow);
        TextView title = text("Menos ruído.\nMais entretenimento.", 29, WHITE); title.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, -2); titleParams.topMargin = dp(10); copy.addView(title, titleParams);
        TextView subtitle = text("Seus aplicativos favoritos, em um só lugar.\nSem anúncios. Do seu jeito.", 12, 0xffb8c5d6); subtitle.setPadding(0, dp(8), 0, dp(14)); copy.addView(subtitle);
        TextView open = button("▦   Explorar aplicativos", () -> { section = "Apps"; render(); }); copy.addView(open, new LinearLayout.LayoutParams(-2, -2)); firstCard = open;
        hero.addView(copy); page.addView(hero, new LinearLayout.LayoutParams(-1, dp(226)));
    }
    private void heading(String label) { TextView t = text(label, 16, WHITE); t.setPadding(0, dp(16), 0, dp(10)); page.addView(t); }
    private void empty(String label) { TextView t = text(label, 13, MUTED); t.setPadding(dp(14), dp(16), dp(14), dp(16)); t.setBackground(bg(0xff101925, 0)); page.addView(t); }
    private void row(List<App> items) {
        if (items.isEmpty()) { empty("Nenhum app disponível neste dispositivo."); return; }
        HorizontalScrollView scroll = new HorizontalScrollView(this); scroll.setClipToPadding(false); scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout line = new LinearLayout(this); line.setPadding(dp(4), dp(5), dp(8), dp(6));
        for (App a : items) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(135), dp(88)); p.rightMargin = dp(12); line.addView(card(a), p); }
        scroll.addView(line); page.addView(scroll);
    }
    private void grid(List<App> items, String message) {
        if (items.isEmpty()) { empty(message); return; }
        int available = (int)(getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().density) - 224;
        int columns = Math.max(2, Math.min(6, available / 145));
        for (int offset = 0; offset < items.size(); offset += columns) {
            LinearLayout line = new LinearLayout(this); line.setPadding(dp(4), dp(5), 0, dp(7));
            for (int i = 0; i < columns; i++) {
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(96), 1); p.rightMargin = dp(10);
                line.addView(offset + i < items.size() ? card(items.get(offset + i)) : new View(this), p);
            }
            page.addView(line);
        }
    }
    private View card(App a) {
        LinearLayout tile = column(); tile.setGravity(Gravity.CENTER); tile.setPadding(dp(8), dp(7), dp(8), dp(6));
        ImageView icon = new ImageView(this); icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        try { icon.setImageDrawable(a.info.loadIcon(getPackageManager())); } catch (RuntimeException e) { icon.setImageResource(R.drawable.ic_aurora); }
        tile.addView(icon, new LinearLayout.LayoutParams(-1, 0, 1));
        boolean favorite = prefs.getStringSet("favorites", Collections.emptySet()).contains(a.pkg);
        TextView name = text((favorite ? "★  " : "") + a.name, 12, WHITE); name.setMaxLines(1); name.setEllipsize(android.text.TextUtils.TruncateAt.END); name.setGravity(Gravity.CENTER); name.setPadding(0, dp(6), 0, 0); tile.addView(name);
        tile.setContentDescription(a.name + (favorite ? ", favorito" : "")); focusStyle(tile, 0xff162131);
        tile.setOnClickListener(v -> launch(a)); tile.setOnLongClickListener(v -> { options(a); return true; });
        tile.setOnKeyListener((v, key, event) -> { if (key == KeyEvent.KEYCODE_MENU) { if (event.getAction() == KeyEvent.ACTION_UP) options(a); return true; } return false; });
        if (firstCard == null) firstCard = tile; return tile;
    }
    private void launch(App a) {
        try {
            startActivity(a.intent);
            externalAppOpened = true;
            prefs.edit().putString("recent", RecentApps.record(prefs.getString("recent", ""), a.pkg)).apply();
        } catch (RuntimeException e) { Toast.makeText(this, "Não foi possível abrir " + a.name, Toast.LENGTH_LONG).show(); }
    }
    private void options(App a) {
        Set<String> favorites = new HashSet<>(prefs.getStringSet("favorites", Collections.emptySet()));
        boolean saved = favorites.contains(a.pkg);
        new AlertDialog.Builder(this).setTitle(a.name).setItems(new String[]{"Abrir", saved ? "Remover dos favoritos" : "Adicionar aos favoritos", "Informações do aplicativo"}, (d, which) -> {
            if (which == 0) launch(a);
            else if (which == 1) { if (saved) favorites.remove(a.pkg); else favorites.add(a.pkg); prefs.edit().putStringSet("favorites", favorites).apply(); render(); }
            else safeStart(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:" + a.pkg)));
        }).show();
    }
    private void safeStart(Intent intent) {
        boolean wasPaused = prefs.getBoolean("settings_pause", false);
        // Fire TV settings can share the launcher's HOME activity.
        prefs.edit().putBoolean("settings_pause", true).apply();
        try {
            startActivity(intent); externalAppOpened = true;
            Toast.makeText(this, "Home pausado para usar as configurações. Retome no menu do Aurora ao terminar.", Toast.LENGTH_LONG).show();
        } catch (RuntimeException e) {
            prefs.edit().putBoolean("settings_pause", wasPaused).apply();
            Toast.makeText(this, "Esta opção não está disponível nesta versão do sistema.", Toast.LENGTH_LONG).show();
        }
    }
    private void settings() {
        new AlertDialog.Builder(this).setTitle("Aurora TV").setItems(new String[]{"Configurações do dispositivo", "Escolher app de início", "Limpar apps recentes", "Botão Home e inicialização", prefs.getBoolean("settings_pause", false) ? "Retomar redirecionamento Home" : "Pausar Home para usar a Amazon", "Sobre"}, (d, which) -> {
            if (which == 0) safeStart(new Intent(Settings.ACTION_SETTINGS));
            else if (which == 1) safeStart(new Intent(Settings.ACTION_HOME_SETTINGS));
            else if (which == 2) { prefs.edit().remove("recent").apply(); render(); }
            else if (which == 3) redirectSettings();
            else if (which == 4) {
                boolean pause = !prefs.getBoolean("settings_pause", false);
                prefs.edit().putBoolean("settings_pause", pause).apply();
                Toast.makeText(this, pause ? "Home pausado; configurações Amazon liberadas" : "Pausa removida; confira se Redirecionar Home está ligado", Toast.LENGTH_LONG).show();
            }
            else new AlertDialog.Builder(this).setTitle("Aurora TV · 0.2.2")
                .setMessage("Uma tela inicial leve, privada e sem anúncios.\n\nFavoritos e apps recentes ficam somente neste dispositivo.\n\nAlgumas versões do Fire OS bloqueiam a troca do launcher e o botão Home. O Aurora não altera nem desativa o launcher da Amazon.")
                .setPositiveButton("Entendi", null).show();
        }).show();
    }
    private void redirectSettings() {
        boolean redirect = prefs.getBoolean("redirect_home", false);
        boolean boot = prefs.getBoolean("open_at_boot", false);
        android.view.accessibility.AccessibilityManager manager =
            (android.view.accessibility.AccessibilityManager)getSystemService(ACCESSIBILITY_SERVICE);
        boolean serviceEnabled = false;
        for (android.accessibilityservice.AccessibilityServiceInfo info : manager.getEnabledAccessibilityServiceList(
                android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
            android.content.pm.ServiceInfo service = info.getResolveInfo().serviceInfo;
            if (getPackageName().equals(service.packageName) && HomeRedirectService.class.getName().equals(service.name)) serviceEnabled = true;
        }
        String status = "Redirecionar Home: " + (redirect ? "ligado" : "desligado")
            + "\nPausa para configurações: " + (prefs.getBoolean("settings_pause", false) ? "sim — retome no menu do Aurora" : "não")
            + "\nServiço de acessibilidade: " + (serviceEnabled ? "habilitado" : "não habilitado")
            + "\nInício da Amazon identificado: " + (prefs.getBoolean("home_detected", false) ? "sim" : "ainda não")
            + "\nAbrir ao ligar: " + (boot ? "ligado" : "desligado")
            + "\n\n" + prefs.getString("redirect_notice", "Algumas versões do Fire OS bloqueiam este recurso.");
        new AlertDialog.Builder(this).setTitle("Home e inicialização")
            .setItems(new String[]{redirect ? "Desligar redirecionamento Home" : "Ativar redirecionamento Home",
                "Abrir acessibilidade do sistema", boot ? "Desligar abertura ao ligar" : "Abrir Aurora ao ligar", "Ver estado e diagnóstico"}, (d, which) -> {
                if (which == 0 && redirect) prefs.edit().putBoolean("redirect_home", false).apply();
                else if (which == 0) new AlertDialog.Builder(this).setTitle("Redirecionar a tela inicial?")
                    .setMessage("O serviço detecta apenas o nome da janela inicial da Amazon e abre o Aurora. Não lê conteúdo da tela, não captura teclas e não envia dados. Você também precisa habilitar Aurora — redirecionar início na acessibilidade do sistema.\n\nA tela Amazon pode aparecer brevemente. Se o sistema insistir em voltar para ela, o redirecionamento será pausado automaticamente. Você pode desligar aqui a qualquer momento.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Ativar", (dialog, w) -> { prefs.edit().putBoolean("redirect_home", true).remove("redirect_notice").apply(); safeStart(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }).show();
                else if (which == 1) safeStart(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                else if (which == 2) { prefs.edit().putBoolean("open_at_boot", !boot).apply(); Toast.makeText(this, boot ? "Abertura ao ligar desligada" : "Abertura ao ligar ativada; depende do Fire OS", Toast.LENGTH_LONG).show(); }
                else new AlertDialog.Builder(this).setTitle("Estado do redirecionamento").setMessage(status).setPositiveButton("OK", null).show();
            }).setNegativeButton("Fechar", null).show();
    }
    @Override public void onBackPressed() {
        if (!section.equals("Início")) { section = "Início"; query = ""; render(); }
        else menu.getChildAt(1).requestFocus();
    }

    /** Original procedural scenery, drawn locally; no licensed film artwork. */
    private class Landscape extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Landscape(android.content.Context c) { super(c); }
        @Override protected void onDraw(Canvas c) {
            float w = getWidth(), h = getHeight();
            paint.setShader(new LinearGradient(0, 0, w, h, new int[]{0xff142130, 0xff284f65, 0xff101928}, null, Shader.TileMode.CLAMP)); c.drawRect(0, 0, w, h, paint);
            paint.setShader(new RadialGradient(w * .78f, h * .26f, h * .65f, 0xffaac5ba, 0x002b6179, Shader.TileMode.CLAMP)); c.drawRect(0, 0, w, h, paint); paint.setShader(null);
            paint.setColor(0xffc3d4cd); c.drawCircle(w * .79f, h * .27f, h * .115f, paint);
            for (int layer = 0; layer < 4; layer++) {
                Path p = new Path(); p.moveTo(0, h);
                for (int i = 0; i <= 16; i++) {
                    float x = w * i / 16f;
                    float y = h * (.58f + layer * .105f) + (float)Math.sin(i * 1.7 + layer * 2) * h * .14f;
                    p.lineTo(x, y);
                }
                p.lineTo(w, h); p.close(); paint.setColor(new int[]{0xff294758, 0xff203544, 0xff152635, 0xff101c29}[layer]); c.drawPath(p, paint);
            }
            paint.setShader(new LinearGradient(0, 0, w, 0, new int[]{0xff0b121e, 0xf00b121e, 0x100b121e}, new float[]{0,.36f,1}, Shader.TileMode.CLAMP)); c.drawRect(0, 0, w, h, paint);
            paint.setShader(new LinearGradient(0, h * .7f, 0, h, 0x00080d15, 0xff080d15, Shader.TileMode.CLAMP)); c.drawRect(0, 0, w, h, paint); paint.setShader(null);
        }
    }
}
