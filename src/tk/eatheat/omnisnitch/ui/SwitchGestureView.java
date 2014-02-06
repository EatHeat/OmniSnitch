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
package tk.eatheat.omnisnitch.ui;

import tk.eatheat.omnisnitch.R;
import tk.eatheat.omnisnitch.SettingsActivity;
import tk.eatheat.omnisnitch.SwitchConfiguration;
import tk.eatheat.omnisnitch.SwitchService;
import tk.eatheat.omnisnitch.Utils;
import tk.eatheat.omnisnitch.showcase.ShowcaseView;
import tk.eatheat.omnisnitch.showcase.ShowcaseView.OnShowcaseEventListener;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SwitchGestureView implements OnShowcaseEventListener {
    private final static String TAG = "SwitchGestureView";
    private static final boolean DEBUG = false;

    private final static String KEY_SHOWCASE_HANDLE = "showcase_handle_done";

    private Context mContext;
    private WindowManager mWindowManager;
    private ImageView mDragButton;
    private LinearLayout mView;
    private int mTriggerThreshholdX = 20;
    private float[] mDownPoint = new float[2];
    private boolean mSwipeStarted;
    private boolean mShowStarted;
    private boolean mShowing;
    private boolean mEnabled = true;
    private Drawable mDragHandleImage;
    private Drawable mDragHandleHiddenImage;
    private Drawable mCurrentDragHandleImage;
    private ShowcaseView mShowcaseView;
    private SharedPreferences mPrefs;
    private boolean mShowcaseDone;
    private SwitchConfiguration mConfiguration;
    private boolean mHidden;
    private boolean mLongpressEnable;
    private Handler mHandler;
    private Runnable mAutoHideRunnable = new Runnable(){
        @Override
        public void run() {
            if(!mHidden){
                updateDragHandleImage(false);
                mView.invalidate();
            }
        }};

    public SwitchGestureView(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mConfiguration = SwitchConfiguration.getInstance(mContext);
        mHandler = new Handler();
        
        mDragHandleImage = mContext.getResources().getDrawable(
                R.drawable.drag_handle);
        mDragHandleHiddenImage = mContext.getResources().getDrawable(
                R.drawable.drag_handle_overlay);

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mView = (LinearLayout) inflater.inflate(R.layout.gesture_view, null, false);

        mDragButton= new ImageView(mContext);
        mDragButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if(DEBUG){
                    Log.d(TAG, "button onTouch");
                }
                if (!mEnabled){
                    return false;
                }
                boolean defaultResult = v.onTouchEvent(event);

                switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if(mHidden && mConfiguration.mAutoHide){
                        updateDragHandleImage(true);
                        mView.invalidate();
                        mHandler.postDelayed(mAutoHideRunnable, SwitchConfiguration.AUTO_HIDE_DEFAULT);
                        mLongpressEnable = false;
                        return true;
                    }
                    if (!mHidden && !mSwipeStarted) {
                        mDownPoint[0] = event.getX();
                        mDownPoint[1] = event.getY();
                        mSwipeStarted = true;
                        mShowStarted = false;
                        if(DEBUG){
                            Log.d(TAG, "button down " + mDownPoint[0] + " "
                                    + mDownPoint[1]);
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mSwipeStarted = false;
                    mShowStarted = false;
                    mLongpressEnable = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mSwipeStarted) {
                        final int historySize = event.getHistorySize();
                        for (int k = 0; k < historySize + 1; k++) {
                            float x = k < historySize ? event.getHistoricalX(k)
                                    : event.getX();
                            float y = k < historySize ? event.getHistoricalY(k)
                                    : event.getY();
                            float distanceY = Math.abs(mDownPoint[1] - y);
                            float distanceX = Math.abs(mDownPoint[0] - x);
                            if(DEBUG){
                                Log.d(TAG, ""+distanceX + " " + distanceY + " " + mShowStarted);
                            }
                            if (distanceX > mTriggerThreshholdX
                                    //&& distanceY < mTriggerThreshholdY
                                    && !mShowStarted) {
                                Intent showIntent = new Intent(
                                        SwitchService.RecentsReceiver.ACTION_SHOW_OVERLAY);
                                mContext.sendBroadcast(showIntent);
                                mShowStarted = true;
                                mSwipeStarted = false;
                                break;
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (!mShowStarted){
                        mSwipeStarted = false;
                    }
                    mLongpressEnable = true;
                    break;
                default:
                    return defaultResult;
                }
                return true;
            }
        });
        
        mDragButton.setOnLongClickListener(new OnLongClickListener(){
            @Override
            public boolean onLongClick(View arg0) {
                if (!mShowStarted){
                    if(mConfiguration.mAutoHide && !mLongpressEnable){
                        return true;
                    }

                    if(DEBUG){
                        Log.d(TAG, "button long down");
                    }
                    Intent showIntent = new Intent(
                            SwitchService.RecentsReceiver.ACTION_SHOW_OVERLAY);
                    mContext.sendBroadcast(showIntent);
                }
                return true;
            }});
        updateButton();
    }

    private int getGravity() {
        if (mConfiguration.mLocation == 0) {
            return Gravity.RIGHT | Gravity.TOP;
        }
        if (mConfiguration.mLocation == 1) {
            return Gravity.LEFT | Gravity.TOP;
        }

        return Gravity.RIGHT | Gravity.TOP;
    }

    public WindowManager.LayoutParams getParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        lp.gravity = getGravity();
        lp.y = mConfiguration.getCurrentOffsetStart();
        lp.height = mConfiguration.mHandleHeight;
        lp.width = (int) (20 * mConfiguration.mDensity + 0.5f);
        
        return lp;
    }

    private void updateButton() {
        mView.removeView(mDragButton);
        if(mConfiguration.mAutoHide){
            updateDragHandleImage(false);
        } else {
            updateDragHandleImage(true);
            mHidden = false;
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mView.addView(mDragButton, params);
        mView.invalidate();
    }
    
    private void updateDragHandleImage(boolean shown){
        mCurrentDragHandleImage = mDragHandleImage;

        if(mConfiguration.mAutoHide){
            mHidden = !shown;
            if(mHidden){
                mCurrentDragHandleImage = mDragHandleHiddenImage;
            }
        } else {
            if(!shown){
                mCurrentDragHandleImage = mDragHandleHiddenImage;
            }
        }
        
        if (mConfiguration.mLocation == 1) {
            mCurrentDragHandleImage = Utils.rotate(mContext.getResources(), mCurrentDragHandleImage, 180);
        }
        mCurrentDragHandleImage=Utils.colorize(mContext.getResources(), mConfiguration.mDragHandleColor, mCurrentDragHandleImage);
        mCurrentDragHandleImage.setAlpha((int) (255 * mConfiguration.mDragHandleOpacity));
        mDragButton.setScaleType(ImageView.ScaleType.FIT_XY);

        if(shown){
            mDragButton.startAnimation(getShowAnimation());
        } else {
            mDragButton.startAnimation(getHideAnimation());
        }
    }

    public void updatePrefs(SharedPreferences prefs, String key) {
        if(DEBUG){
            Log.d(TAG, "updatePrefs");
        }
        updateButton();
        
        if(key == null || key.equals(SettingsActivity.PREF_DRAG_HANDLE_ENABLE)){
            if(mConfiguration.mDragHandleShow){
                show();
            } else {
                hide();
            }
        }
    }

    public synchronized void show() {
        if (mShowing) {
            return;
        }
        if(DEBUG){
            Log.d(TAG, "show");
        }
        mWindowManager.addView(mView, getParams());
        if(!mShowcaseDone){
            mView.postDelayed(new Runnable(){
                @Override
                public void run() {
                    startShowcaseDragHandle();
                }}, 200);
        }
        mShowing = true;
        mEnabled = true;
    }

    public synchronized void hide() {
        if (!mShowing) {
            return;
        }

        if(DEBUG){
            Log.d(TAG, "hide");
        }
        mWindowManager.removeView(mView);
        mShowing = false;
        mEnabled = false;
    }

    public void overlayShown() {
        if(DEBUG){
            Log.d(TAG, "overlayShown");
        }
        mShowStarted = false;
        mSwipeStarted = false;
        mHandler.removeCallbacks(mAutoHideRunnable);
        updateDragHandleImage(false);
        mView.invalidate();
        mEnabled = false;
    }

    public void overlayHidden() {
        if(DEBUG){
            Log.d(TAG, "overlayHidden");
        }
        if(mConfiguration.mAutoHide){
            updateDragHandleImage(false);
        } else {
            updateDragHandleImage(true);
        }
        mView.invalidate();
        mEnabled = true;
    }
    
    private boolean startShowcaseDragHandle() {
        if (!mPrefs.getBoolean(KEY_SHOWCASE_HANDLE, false)) {
            mPrefs.edit().putBoolean(KEY_SHOWCASE_HANDLE, true).commit();
            ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
            co.hideOnClickOutside = true;

            Point size = new Point();
            mWindowManager.getDefaultDisplay().getSize(size);

            mShowcaseView = ShowcaseView.insertShowcaseView(mConfiguration.mLocation == 1 ? 0 : size.x, 
                    mConfiguration.getCurrentOffsetStart() + (mConfiguration.getCurrentOffsetEnd() - mConfiguration.getCurrentOffsetStart())/2, mWindowManager, mContext,
                    R.string.sc_drag_handle_title, R.string.sc_drag_handle_body, co);

            mShowcaseView.animateGesture(size.x / 2, size.y * 2.0f / 3.0f,
                    size.x / 2, size.y / 2.0f);
            mShowcaseView.setOnShowcaseEventListener(this);
            mShowcaseDone = true;
            return true;
        }
        mShowcaseDone = true;
        return false;
    }

    @Override
    public void onShowcaseViewHide(ShowcaseView showcaseView) {
    }

    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {
    }
    
    public boolean isShowing() {
        return mShowing;
    }
    
    public void updateLayout() {
        if (mShowing){
            mWindowManager.updateViewLayout(mView, getParams());
        }
    }
    
    private Animation getShowAnimation() {
        int animId = R.anim.slide_right_in;

        if (mConfiguration.mLocation == 1) {
            animId = R.anim.slide_left_in;
        }
        Animation animation = AnimationUtils.loadAnimation(mContext, animId);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
                mDragButton.setImageDrawable(mCurrentDragHandleImage);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        return animation;
    }

    private Animation getHideAnimation() {
        int animId = R.anim.slide_right_out;

        if (mConfiguration.mLocation == 1) {
            animId = R.anim.slide_left_out;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, animId);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mDragButton.setImageDrawable(mCurrentDragHandleImage);
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        return animation;
    }
}
