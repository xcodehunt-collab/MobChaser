package com.zyvix.metrodenim;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.role.RoleManager;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.text.Collator;
import java.util.*;

public class MainActivity extends Activity {
    private final ArrayList<AppInfo> apps = new ArrayList<>();
    private LinearLayout root, tileGrid;
    private ScrollView page;
    private SharedPreferences prefs;
    private int accent;
    private boolean light;
    private static final int[] ACCENTS = {0xff0078d7,0xff00a4ef,0xff744da9,0xffd80073,0xffe81123,0xffff8c00,0xff107c10,0xff008272};

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("launcher", MODE_PRIVATE);
        accent = prefs.getInt("accent", ACCENTS[0]); light = prefs.getBoolean("light", false);
        getWindow().setStatusBarColor(Color.BLACK); getWindow().setNavigationBarColor(Color.BLACK);
        loadApps(); showStart();
    }

    private void loadApps() {
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(query, PackageManager.MATCH_ALL);
        Collator collator = Collator.getInstance();
        for (ResolveInfo r : list) {
            ActivityInfo a = r.activityInfo;
            if (a.packageName.equals(getPackageName())) continue;
            apps.add(new AppInfo(r.loadLabel(getPackageManager()).toString(), new ComponentName(a.packageName, a.name), r.loadIcon(getPackageManager())));
        }
        apps.sort((a,b)->collator.compare(a.label,b.label));
    }

    private TextView text(String value, float size) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(light ? Color.BLACK : Color.WHITE); t.setTypeface(Typeface.create("sans", Typeface.NORMAL)); t.setPadding(0,8,0,8); return t;
    }
    private void base() {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(18),0,dp(12),0); root.setBackgroundColor(light ? 0xfff4f4f4 : Color.BLACK);
        setContentView(root);
    }
    private void header(String title, boolean drawer) {
        LinearLayout bar = new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView h = text(title, 35); h.setAllCaps(false); h.setTypeface(Typeface.create("sans", Typeface.create(Typeface.DEFAULT, Typeface.NORMAL).getStyle()));
        bar.addView(h, new LinearLayout.LayoutParams(0, dp(66),1));
        Button b = new Button(this); b.setText(drawer ? "→" : "⌕"); b.setTextSize(24); b.setTextColor(light?Color.BLACK:Color.WHITE); b.setBackgroundColor(Color.TRANSPARENT); b.setOnClickListener(v -> drawer ? showDrawer() : showSearch());
        bar.addView(b,new LinearLayout.LayoutParams(dp(56),dp(56))); root.addView(bar);
    }

    private void showStart() {
        base(); header("start", true);
        page = new ScrollView(this); tileGrid = new LinearLayout(this); tileGrid.setOrientation(LinearLayout.VERTICAL); page.addView(tileGrid); root.addView(page,new LinearLayout.LayoutParams(-1,0,1));
        ArrayList<AppInfo> pinned = pinnedApps();
        LinearLayout row = null;
        for (int i=0;i<pinned.size();i++) {
            if (i%2==0) { row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); tileGrid.addView(row,new LinearLayout.LayoutParams(-1,dp(118))); }
            addTile(row,pinned.get(i));
        }
        addBottomBar();
    }
    private ArrayList<AppInfo> pinnedApps() {
        Set<String> saved=prefs.getStringSet("pinned",null); ArrayList<AppInfo> out=new ArrayList<>();
        if(saved!=null) for(AppInfo a:apps) if(saved.contains(a.component.flattenToString())) out.add(a);
        if(out.isEmpty()) for(int i=0;i<Math.min(12,apps.size());i++) out.add(apps.get(i)); return out;
    }
    private void savePinned(ArrayList<AppInfo> list) { Set<String>s=new LinkedHashSet<>(); for(AppInfo a:list)s.add(a.component.flattenToString()); prefs.edit().putStringSet("pinned",s).apply(); }
    private void addTile(LinearLayout row, AppInfo app) {
        TileView t=new TileView(this,app,accent); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1); lp.setMargins(0,0,dp(5),dp(5)); row.addView(t,lp);
        t.setOnClickListener(v->launch(app)); t.setOnLongClickListener(v->{ tileMenu(t); return true; });
    }
    private void tileMenu(TileView tile) {
        final String[] choices={"Open","Unpin from Start","App info","Cancel"};
        new AlertDialog.Builder(this).setTitle(tile.app.label).setItems(choices,(d,w)->{
            if(w==0) launch(tile.app); else if(w==1){ArrayList<AppInfo> p=pinnedApps(); p.removeIf(a->a.component.equals(tile.app.component));savePinned(p);showStart();}
            else if(w==2) startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+tile.app.component.getPackageName())));
        }).show();
    }
    private void launch(AppInfo a) { try { startActivity(new Intent().setComponent(a.component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); } catch(Exception e){Toast.makeText(this,"App could not be opened",Toast.LENGTH_SHORT).show();} }

    private void showDrawer() {
        base(); header("apps", false); EditText search=new EditText(this); search.setHint("search apps"); search.setTextColor(light?Color.BLACK:Color.WHITE); search.setHintTextColor(0xff888888); root.addView(search,new LinearLayout.LayoutParams(-1,dp(54)));
        ScrollView sv=new ScrollView(this); LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); sv.addView(list); root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        Runnable render=()->{String q=search.getText().toString().toLowerCase(Locale.ROOT);list.removeAllViews();for(AppInfo a:apps)if(a.label.toLowerCase(Locale.ROOT).contains(q))addAppRow(list,a);}; render.run();
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){render.run();}public void afterTextChanged(android.text.Editable e){}});
        addBottomBar();
    }
    private void addAppRow(LinearLayout list, AppInfo a) {
        LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(0,5,0,5);ImageView i=new ImageView(this);i.setImageDrawable(a.icon);r.addView(i,new LinearLayout.LayoutParams(dp(44),dp(44)));TextView n=text(a.label,18);LinearLayout.LayoutParams nl=new LinearLayout.LayoutParams(0,dp(56),1);nl.leftMargin=dp(14);r.addView(n,nl);r.setOnClickListener(v->launch(a));r.setOnLongClickListener(v->{ArrayList<AppInfo> p=pinnedApps();if(p.stream().noneMatch(x->x.component.equals(a.component))){p.add(a);savePinned(p);Toast.makeText(this,"Pinned to Start",Toast.LENGTH_SHORT).show();}return true;});list.addView(r);
    }
    private void showSearch() { showDrawer(); }

    private void showSettings() {
        base(); header("settings", false); LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);root.addView(list,new LinearLayout.LayoutParams(-1,0,1));
        settingRow(list,"make default Home app","Choose Metro Denim as your Home screen",v->requestHomeRole());
        settingRow(list,"start + theme","Change accent and light/dark appearance",v->themeDialog());
        settingRow(list,"wallpaper","Open Android wallpaper picker",v->{try{startActivity(new Intent(Intent.ACTION_SET_WALLPAPER));}catch(Exception e){}});
        settingRow(list,"system settings","Open Android device settings",v->startActivity(new Intent(Settings.ACTION_SETTINGS)));
        settingRow(list,"about","Metro Denim Launcher 1.0\nAndroid 8–16",null); addBottomBar();
    }
    private void settingRow(LinearLayout list,String title,String sub,View.OnClickListener click){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);TextView a=text(title,21),b=text(sub,13);b.setTextColor(0xff888888);r.addView(a);r.addView(b);r.setPadding(0,9,0,9);if(click!=null){r.setOnClickListener(click);r.setClickable(true);}list.addView(r,new LinearLayout.LayoutParams(-1,-2));}
    private void requestHomeRole(){
        if(android.os.Build.VERSION.SDK_INT>=29){RoleManager rm=getSystemService(RoleManager.class);if(rm!=null&&rm.isRoleAvailable(RoleManager.ROLE_HOME))startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_HOME),42);}
        else startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
    }
    private void themeDialog(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(22),dp(8),dp(22),0);Switch mode=new Switch(this);mode.setText("Light theme");mode.setChecked(light);box.addView(mode);RadioGroup colors=new RadioGroup(this);for(int c:ACCENTS){RadioButton r=new RadioButton(this);r.setText(String.format("  #%06X",0xffffff&c));r.setTextColor(c);r.setTag(c);r.setChecked(c==accent);colors.addView(r);}box.addView(colors);
        new AlertDialog.Builder(this).setTitle("start + theme").setView(box).setPositiveButton("apply",(d,w)->{light=mode.isChecked();int id=colors.getCheckedRadioButtonId();RadioButton r=colors.findViewById(id);if(r!=null)accent=(int)r.getTag();prefs.edit().putBoolean("light",light).putInt("accent",accent).apply();showStart();}).setNegativeButton("cancel",null).show();
    }
    private void addBottomBar(){LinearLayout b=new LinearLayout(this);b.setGravity(Gravity.CENTER);String[] x={"‹","⊞","⌕","⚙"};for(String s:x){Button k=new Button(this);k.setText(s);k.setTextSize(25);k.setTextColor(light?Color.BLACK:Color.WHITE);k.setBackgroundColor(Color.TRANSPARENT);b.addView(k,new LinearLayout.LayoutParams(0,dp(54),1));if(s.equals("‹"))k.setOnClickListener(v->showStart());else if(s.equals("⊞"))k.setOnClickListener(v->showStart());else if(s.equals("⌕"))k.setOnClickListener(v->showSearch());else k.setOnClickListener(v->showSettings());}root.addView(b);}
    @Override public void onBackPressed(){showStart();}
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
}
