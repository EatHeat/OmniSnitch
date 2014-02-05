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

import tk.eatheat.omnisnitch.ui.SwitchGestureView;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class SwitchService extends Service {
    private final static String TAG = "SwitchService";
    private static boolean DEBUG = false;

    private SwitchGestureView mGesturePanel;
    private RecentsReceiver mReceiver;
    private SwitchManager mManager;
    private SharedPreferences mPrefs;
    private SharedPreferences.OnSharedPreferenceChangeListener mPrefsListener;
    private SwitchConfiguration mConfiguration;

    private static boolean mIsRunning;

    public static boolean isRunning() {
        return mIsRunning;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mGesturePanel = new SwitchGestureView(this);
        Log.d(TAG, "started SwitchService");

        mManager = new SwitchManager(this);
        mConfiguration = SwitchConfiguration.getInstance(this);

        mReceiver = new RecentsReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RecentsReceiver.ACTION_SHOW_OVERLAY);
        filter.addAction(RecentsReceiver.ACTION_SHOW_OVERLAY2);
        filter.addAction(RecentsReceiver.ACTION_HIDE_OVERLAY);
        filter.addAction(RecentsReceiver.ACTION_KILL_ACTIVITY);
        filter.addAction(RecentsReceiver.ACTION_OVERLAY_SHOWN);
        filter.addAction(RecentsReceiver.ACTION_OVERLAY_HIDDEN);

        registerReceiver(mReceiver, filter);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        updatePrefs(mPrefs, null);

        mPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs,
                    String key) {
                updatePrefs(prefs, key);
            }
        };

        mPrefs.registerOnSharedPreferenceChangeListener(mPrefsListener);

        mGesturePanel.show();

        mIsRunning = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "stopped SwitchService");

        mGesturePanel.hide();
        mGesturePanel = null;

        unregisterReceiver(mReceiver);
        mManager.killManager();
        mPrefs.unregisterOnSharedPreferenceChangeListener(mPrefsListener);

        mIsRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class RecentsReceiver extends BroadcastReceiver {
        public static final String ACTION_SHOW_OVERLAY = "tk.eatheat.omnisnitch.ACTION_SHOW_OVERLAY";
        public static final String ACTION_SHOW_OVERLAY2 = "tk.eatheat.omnisnitch.ACTION_SHOW_OVERLAY2";
        public static final String ACTION_HIDE_OVERLAY = "tk.eatheat.omnisnitch.ACTION_HIDE_OVERLAY";
        public static final String ACTION_KILL_ACTIVITY = "tk.eatheat.omnisnitch.ACTION_KILL_ACTIVITY";
        public static final String ACTION_OVERLAY_SHOWN = "tk.eatheat.omnisnitch.ACTION_OVERLAY_SHOWN";
        public static final String ACTION_OVERLAY_HIDDEN = "tk.eatheat.omnisnitch.ACTION_OVERLAY_HIDDEN";

        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            if(DEBUG){
                Log.d(TAG, "onReceive " + action);
            }
            if (ACTION_SHOW_OVERLAY.equals(action)) {
                if (!mManager.isShowing()) {
                    Intent mainActivity = new Intent(context,
                            MainActivity.class);
                    mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

                    startActivity(mainActivity);
                }
            } else if (ACTION_SHOW_OVERLAY2.equals(action)) {
                if (!mManager.isShowing()) {
                    mManager.show();
                }
            } else if (ACTION_HIDE_OVERLAY.equals(action)) {
                if (mManager.isShowing()) {
                    Intent finishActivity = new Intent(
                            MainActivity.ActivityReceiver.ACTION_FINISH);
                    sendBroadcast(finishActivity);
                    mManager.hide();
                }
            } else if (ACTION_KILL_ACTIVITY.equals(action)) {
                Intent finishActivity = new Intent(
                        MainActivity.ActivityReceiver.ACTION_FINISH);
                sendBroadcast(finishActivity);
                Intent svc = new Intent(context, SwitchService.class);
                context.stopService(svc);
            } else if (ACTION_OVERLAY_SHOWN.equals(action)){
                mGesturePanel.overlayShown();
            } else if (ACTION_OVERLAY_HIDDEN.equals(action)){
                mGesturePanel.overlayHidden();
            }
        }
    }

    public void updatePrefs(SharedPreferences prefs, String key) {
        // MUST be before the rest
        mConfiguration.updatePrefs(prefs, key);
        mManager.updatePrefs(prefs, key);
        mGesturePanel.updatePrefs(prefs, key);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // recalc drag handle location
        if (mIsRunning && mGesturePanel.isShowing()){
            mGesturePanel.updateLayout();
        }
        // recalc overlay location
        if (mIsRunning && mManager.isShowing()) {
            mManager.updateLayout();
        }
    }
}
