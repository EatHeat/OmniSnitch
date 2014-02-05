/*
 *  Copyright (C) 2013 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package tk.eatheat.omnisnitch;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import tk.eatheat.omnisnitch.R;

public class SwitchConfiguration {
    public float mBackgroundOpacity = 0.8f;
    public int mLocation = 0; // 0 = right 1 = left
    public boolean mAnimate = true;
    public int mIconSize = 60; // in dip
    public int mIconBorder = 10; // in dip
    public float mDensity;
    public int mHorizontalMaxWidth = mIconSize;
    public int mHorizontalScrollerHeight = mIconSize * 2;
    public boolean mShowRambar;
    public int mStartYRelative;
    public int mHandleHeight;
    public boolean mShowLabels = true;
    public int mDragHandleColor;
    public int mGlowColor;
    public int mDefaultColor;
    public int mIconDpi;
    public boolean mAutoHide;
    public int mHorizontalMargin;
    public static final int AUTO_HIDE_DEFAULT = 3000; // 3s

    public static SwitchConfiguration mInstance;
    private WindowManager mWindowManager;
    private int mDefaultHeight;

    public static SwitchConfiguration getInstance(Context context) {
        if(mInstance==null){
            mInstance = new SwitchConfiguration(context);
        }
        return mInstance;
    }
    
    private SwitchConfiguration(Context context){
        mDensity = context.getResources().getDisplayMetrics().density;

        mWindowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        Point size = new Point();
        mWindowManager.getDefaultDisplay().getSize(size);
        mDefaultColor = context.getResources().getColor(R.color.holo_blue_light);
        mGlowColor = mDefaultColor;
        mDefaultHeight = (int) (100 * mDensity + 0.5);
        mHorizontalMargin = (int) (5 * mDensity + 0.5);
        updatePrefs(PreferenceManager.getDefaultSharedPreferences(context), "");
    }

    public void updatePrefs(SharedPreferences prefs, String key) {
        mLocation = prefs.getInt(
                SettingsActivity.PREF_DRAG_HANDLE_LOCATION, 0);
        int opacity = prefs.getInt(SettingsActivity.PREF_OPACITY, 80);
        mBackgroundOpacity = (float) opacity / 100.0f;
        mAnimate = prefs.getBoolean(SettingsActivity.PREF_ANIMATE, true);
        String iconSize = prefs
                .getString(SettingsActivity.PREF_ICON_SIZE, "60");
        mIconSize = Integer.valueOf(iconSize);
        mShowRambar = prefs
                .getBoolean(SettingsActivity.PREF_SHOW_RAMBAR, false);
        mShowLabels = prefs.getBoolean(SettingsActivity.PREF_SHOW_LABELS, true);

        int relHeightStart = (int)(getDefaultOffsetStart() / (getCurrentDisplayHeight() /100));

        mStartYRelative = prefs.getInt(SettingsActivity.PREF_HANDLE_POS_START_RELATIVE, relHeightStart);
        mHandleHeight = prefs.getInt(SettingsActivity.PREF_HANDLE_HEIGHT, mDefaultHeight);

        mHorizontalMaxWidth = (int) ((mIconSize + mIconBorder) * mDensity + 0.5f);
        mHorizontalScrollerHeight = (int) ((mIconSize + mIconBorder + (mShowLabels ? 40 : 0))
                * mDensity + 0.5f);
        mDragHandleColor = prefs
                .getInt(SettingsActivity.PREF_DRAG_HANDLE_COLOR, mDefaultColor);
        mAutoHide= prefs.getBoolean(SettingsActivity.PREF_AUDIO_HIDE_HANDLE, false);
    }
    
    // includes rotation                
    public int getCurrentDisplayHeight(){
        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        return height;
    }
    
    public int getCurrentDisplayWidth(){
        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        return width;
    }
    
    public int getCurrentOffsetStart(){
        return (getCurrentDisplayHeight() / 100) * mStartYRelative;
    }

    public int getCustomOffsetStart(int startYRelative){
        return (getCurrentDisplayHeight() / 100) * startYRelative;
    }
    
    public int getDefaultOffsetStart(){
        return ((getCurrentDisplayHeight() / 2) - mDefaultHeight /2);
    }

    public int getDefaultHeightRelative(){
        return mDefaultHeight / (getCurrentDisplayHeight() / 100);
    }

    public int getCurrentOffsetEnd(){
        return getCurrentOffsetStart() + mHandleHeight;
    }

    public int getCustomOffsetEnd(int startYRelative, int handleHeight){
        return getCustomOffsetStart(startYRelative) + handleHeight;
    }
    
    public int getDefaultOffsetEnd(){
        return getDefaultOffsetStart() + mDefaultHeight;
    }
}
