/*
 *  Copyright (C) 2013 The OmniROM Project
 *  Copyright (C) 2014 EatHeat
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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tk.eatheat.omnisnitch.ui.SwitchLayout;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class SwitchManager {
    private static final String TAG = "SwitchManager";
    private static final boolean DEBUG = false;
    private List<TaskDescription> mLoadedTasks;
    private RecentTasksLoader mRecentTasksLoader;
    private SwitchLayout mLayout;
    private Context mContext;

    public SwitchManager(Context context) {
        mContext = context;
        init();
    }

    public void hide() {
        if (isShowing()) {
            if(DEBUG){
                Log.d(TAG, "hide");
            }
            mLayout.hide();
        }
    }

    public void show() {
        if (!isShowing()) {
            if(DEBUG){
                Log.d(TAG, "show");
            }

            // clear so that we dont get a reorder
            // during show
            mLoadedTasks.clear();
            mLayout.update(mLoadedTasks, false);

            // show immediately
            mLayout.show();

            // update task list
            reload();
        }
    }

    public boolean isShowing() {
        return mLayout.isShowing();
    }

    private void init() {
        if(DEBUG){
            Log.d(TAG, "init");
        }

        mLoadedTasks = new ArrayList<TaskDescription>();

        mLayout = new SwitchLayout(mContext);
        mLayout.setRecentsManager(this);
        mRecentTasksLoader = RecentTasksLoader.getInstance(mContext, this);
    }

    public void killManager() {
        RecentTasksLoader.killInstance();
    }

    public void update(List<TaskDescription> taskList) {
        if(DEBUG){
            Log.d(TAG, "update");
        }
        mLoadedTasks.clear();
        mLoadedTasks.addAll(taskList);
        mLayout.update(mLoadedTasks, true);
    }

    public void reload() {
        mRecentTasksLoader.cancelLoadingTasks();
        mRecentTasksLoader.loadTasksInBackground();
    }

    public void switchTask(TaskDescription ad) {
        if (ad.isKilled()) {
            return;
        }
        final ActivityManager am = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);

        if(DEBUG){
            Log.d(TAG, "switch to " + ad.getPackageName());
        }

        Intent hideRecent = new Intent(
                SwitchService.RecentsReceiver.ACTION_HIDE_OVERLAY);
        mContext.sendBroadcast(hideRecent);

        if (ad.getTaskId() >= 0) {
            // This is an active task; it should just go to the foreground.
            am.moveTaskToFront(ad.getTaskId(),
                    ActivityManager.MOVE_TASK_WITH_HOME);
        } else {
            Intent intent = ad.getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
                    | Intent.FLAG_ACTIVITY_TASK_ON_HOME
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            if (DEBUG)
                Log.v(TAG, "Starting activity " + intent);
            try {
                mContext.startActivity(intent);
            } catch (SecurityException e) {
                Log.e(TAG, "Recents does not have the permission to launch "
                        + intent, e);
            }
        }
    }

    public void killTask(TaskDescription ad) {
        final ActivityManager am = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);

//        am.removeTask(ad.getPersistentTaskId(),
//                ActivityManager.REMOVE_TASK_KILL_PROCESS);
        if(DEBUG){
            Log.d(TAG, "kill " + ad.getPackageName());
        }
        ad.setKilled();
        mLoadedTasks.remove(ad);
        mLayout.update(mLoadedTasks, true);
    }

    public void killAll() {
        final ActivityManager am = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);

        if (mLoadedTasks.size() == 0) {
            Intent hideRecent = new Intent(
                    SwitchService.RecentsReceiver.ACTION_HIDE_OVERLAY);
            mContext.sendBroadcast(hideRecent);
            return;
        }

        Iterator<TaskDescription> nextTask = mLoadedTasks.iterator();
        while (nextTask.hasNext()) {
            TaskDescription ad = nextTask.next();
//            am.removeTask(ad.getPersistentTaskId(),
//                    ActivityManager.REMOVE_TASK_KILL_PROCESS);
            if(DEBUG){
                Log.d(TAG, "kill " + ad.getPackageName());
            }
            ad.setKilled();
        }
        dismissAndGoHome();
    }

    public void killOther() {
        final ActivityManager am = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);

        if (mLoadedTasks.size() == 0) {
            Intent hideRecent = new Intent(
                    SwitchService.RecentsReceiver.ACTION_HIDE_OVERLAY);
            mContext.sendBroadcast(hideRecent);
            return;
        }
        Iterator<TaskDescription> nextTask = mLoadedTasks.iterator();
        // skip active task
        nextTask.next();
        while (nextTask.hasNext()) {
            TaskDescription ad = nextTask.next();
			// am.removeTask(ad.getPersistentTaskId(),
			// ActivityManager.REMOVE_TASK_KILL_PROCESS);
            if(DEBUG){
                Log.d(TAG, "kill " + ad.getPackageName());
            }
            ad.setKilled();
        }
        Intent hideRecent = new Intent(
                SwitchService.RecentsReceiver.ACTION_HIDE_OVERLAY);
        mContext.sendBroadcast(hideRecent);
    }

    public void dismissAndGoHome() {
        Intent hideRecent = new Intent(
                SwitchService.RecentsReceiver.ACTION_HIDE_OVERLAY);
        mContext.sendBroadcast(hideRecent);

        Intent homeIntent = new Intent(Intent.ACTION_MAIN, null);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        mContext.startActivity(homeIntent);
    }

    public void updatePrefs(SharedPreferences prefs, String key) {
        mLayout.updatePrefs(prefs, key);
    }

    public void toggleLastApp() {
        if (mLoadedTasks.size() < 2) {
            Intent hideRecent = new Intent(
                    SwitchService.RecentsReceiver.ACTION_HIDE_OVERLAY);
            mContext.sendBroadcast(hideRecent);
            return;
        }

        TaskDescription ad = mLoadedTasks.get(1);
        switchTask(ad);
    }
    
    public void startIntentFromtString(String intent) {
        try {
            Intent intentapp = Intent.parseUri(intent, 0);
            mContext.startActivity(intentapp);
        } catch (URISyntaxException e) {
            Log.e(TAG, "URISyntaxException: [" + intent + "]");
        } catch (ActivityNotFoundException e){
            Log.e(TAG, "ActivityNotFound: [" + intent + "]");
        }
    }
    
    public void updateLayout() {
        mLayout.updateLayout();
    }
}
