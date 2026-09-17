package com.zyvix.metrodenim;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;

final class AppInfo {
    final String label;
    final ComponentName component;
    final Drawable icon;
    AppInfo(String label, ComponentName component, Drawable icon) {
        this.label = label; this.component = component; this.icon = icon;
    }
}
