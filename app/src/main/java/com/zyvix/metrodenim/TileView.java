package com.zyvix.metrodenim;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

final class TileView extends LinearLayout {
    final AppInfo app;
    final ImageView icon;
    final TextView label;
    int span = 1;

    TileView(Context context, AppInfo app, int accent) {
        super(context); this.app = app;
        setOrientation(VERTICAL); setGravity(Gravity.CENTER); setPadding(10, 12, 10, 8);
        GradientDrawable bg = new GradientDrawable(); bg.setColor(accent); bg.setCornerRadius(0); setBackground(bg);
        icon = new ImageView(context); icon.setImageDrawable(app.icon); icon.setColorFilter(Color.WHITE);
        addView(icon, new LayoutParams(dp(36), dp(36)));
        label = new TextView(context); label.setText(app.label); label.setTextColor(Color.WHITE); label.setTextSize(12); label.setGravity(Gravity.START); label.setSingleLine(true);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT); lp.topMargin = 8; addView(label, lp);
        setContentDescription(app.label + " tile"); setClickable(true); setFocusable(true);
    }
    void setAccent(int color) { GradientDrawable bg = (GradientDrawable)getBackground(); bg.setColor(color); }
    int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
}
