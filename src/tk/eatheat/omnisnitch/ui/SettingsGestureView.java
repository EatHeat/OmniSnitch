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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SettingsGestureView {
    private WindowManager mWindowManager;
    private ImageView mDragButton;
    private ImageView mDragButtonStart;
    private ImageView mDragButtonEnd;

    private Button mOkButton;
    private Button mCancelButton;
    private Button mLocationButton;
    private Button mResetButton;
    private LinearLayout mView;
    private LinearLayout mDragHandleViewLeft;
    private LinearLayout mDragHandleViewRight;
    private Context mContext;

    private int mLocation = 0; // 0 = right 1 = left
    private boolean mShowing;
    private float mDensity;
    private int mStartY;
    private int mStartYRelative;
    private int mHandleHeight;
    private int mEndY;
    private int mColor;
    private Drawable mDragHandle;
    private Drawable mDragHandleStart;
    private Drawable mDragHandleEnd;
    private SharedPreferences mPrefs;
    private float mDownY;
    private float mDeltaY;
    private int mSlop;
    private int mDragHandleMinHeight;
    private int mDragHandleLimiterHeight;

    public SettingsGestureView(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mDensity = mContext.getResources().getDisplayMetrics().density;
        Point size = new Point();
        mWindowManager.getDefaultDisplay().getSize(size);
        ViewConfiguration vc = ViewConfiguration.get(mContext);
        mSlop = vc.getScaledTouchSlop();

        mDragHandleLimiterHeight = (int) (40 * mDensity + 0.5);
        mDragHandleMinHeight = (int) (60 * mDensity + 0.5);

        mDragHandle = mContext.getResources().getDrawable(
                R.drawable.drag_handle);
        mDragHandleStart = mContext.getResources().getDrawable(
                R.drawable.drag_handle_start);
        mDragHandleEnd = mContext.getResources().getDrawable(
                R.drawable.drag_handle_end);

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mView = (LinearLayout) inflater.inflate(R.layout.settings_gesture_view, null, false);

        mDragHandleViewLeft = (LinearLayout)mView.findViewById(R.id.drag_handle_view_left);
        mDragHandleViewRight = (LinearLayout)mView.findViewById(R.id.drag_handle_view_right);

        mOkButton = (Button) mView.findViewById(R.id.ok_button);
        mCancelButton = (Button) mView.findViewById(R.id.cancel_button);
        mLocationButton = (Button) mView.findViewById(R.id.location_button);
        mResetButton = (Button) mView.findViewById(R.id.reset_button);

        mDragButton = new ImageView(mContext);
        mDragButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownY = event.getRawY();
                    mDeltaY = 0;
                    break;
                case MotionEvent.ACTION_UP:
                    if(mDeltaY != 0){
                        mStartY += mDeltaY;
                        mEndY += mDeltaY;
                        updateDragHandleLayoutParams();
                    }
                    mDragButton.setTranslationY(0);
                    mDragButtonStart.setTranslationY(0);
                    mDragButtonEnd.setTranslationY(0);
                    mDownY = 0;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mDownY = 0;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float deltaY = event.getRawY() - mDownY;
                    if(Math.abs(deltaY) > mSlop){
                        if(((mEndY + deltaY) < getCurrentDisplayHeight())
                                && (mStartY + deltaY > 0)){
                            mDeltaY = deltaY;
                            mDragButton.setTranslationY(mDeltaY);
                            mDragButtonStart.setTranslationY(mDeltaY);
                            mDragButtonEnd.setTranslationY(mDeltaY);
                        }
                    }
                    break;
                }
                return true;
            }
        });

        mDragButtonStart = new ImageView(mContext);
        mDragButtonStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownY = event.getRawY();
                    mDeltaY = 0;
                    break;
                case MotionEvent.ACTION_UP:
                    if(mDeltaY != 0){
                        mStartY += mDeltaY;
                        updateDragHandleLayoutParams();
                    }
                    mDragButtonStart.setTranslationY(0);
                    mDownY = 0;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mDownY = 0;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float deltaY = event.getRawY() - mDownY;
                    if(Math.abs(deltaY) > mSlop){
                        if(((mStartY + deltaY) < (mEndY - mDragHandleMinHeight))
                                && (mStartY + deltaY - mDragHandleLimiterHeight > 0)){
                            mDeltaY = deltaY;
                            mDragButtonStart.setTranslationY(mDeltaY);
                        }
                    }
                    break;
                }
                return true;
            }
        });
        mDragButtonEnd = new ImageView(mContext);
        mDragButtonEnd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownY = event.getRawY();
                    mDeltaY = 0;
                    break;
                case MotionEvent.ACTION_UP:
                    if(mDeltaY != 0){
                        mEndY += mDeltaY;
                        updateDragHandleLayoutParams();
                    }
                    mDragButtonEnd.setTranslationY(0);
                    mDownY = 0;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mDownY = 0;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float deltaY = event.getRawY() - mDownY;
                    if(Math.abs(deltaY) > mSlop){
                        if(((mEndY + deltaY) > (mStartY + mDragHandleMinHeight))
                                && (mEndY + deltaY + mDragHandleLimiterHeight < getCurrentDisplayHeight())){
                            mDeltaY = deltaY;
                            mDragButtonEnd.setTranslationY(mDeltaY);
                        }
                    }
                    break;
                }
                return true;
            }
        });

        mOkButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                switch (action) {
                case MotionEvent.ACTION_DOWN:
                    Editor edit = mPrefs.edit();
                    edit.putInt(SettingsActivity.PREF_DRAG_HANDLE_LOCATION, mLocation);
                    int relHeight = (int)(mStartY / (getCurrentDisplayHeight() /100));
                    edit.putInt(SettingsActivity.PREF_HANDLE_POS_START_RELATIVE, relHeight);
                    edit.putInt(SettingsActivity.PREF_HANDLE_HEIGHT, mEndY - mStartY);
                    edit.commit();
                    hide();
                }
                return true;
            }
        });

        mCancelButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                switch (action) {
                case MotionEvent.ACTION_DOWN:
                    hide();
                }
                return true;
            }
        });
        
        mLocationButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (mLocation == 1){
                        mLocation = 0;
                        mLocationButton.setText(mContext.getResources().getString(R.string.location_left));
                    } else {
                        mLocation = 1;
                        mLocationButton.setText(mContext.getResources().getString(R.string.location_right));
                    }
                    updateLayout();
                }
                return true;
            }
        });
        
        mResetButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                switch (action) {
                case MotionEvent.ACTION_DOWN:
                    resetPosition();
                }
                return true;
            }
        });
        
        mView.setFocusableInTouchMode(true);
        mView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction()==KeyEvent.ACTION_DOWN){
                    hide();
                    return true;
                }
                return false;
            }
        });
    }

    public WindowManager.LayoutParams getGesturePanelLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.CENTER;
        lp.dimAmount = 0.8f;
        return lp;
    }

    private void updateLayout() {
        mDragHandleViewLeft.removeAllViews();
        mDragHandleViewRight.removeAllViews();

        updateDragHandleImage();
        updateDragHandleLayoutParams();

        getDragHandleContainer().addView(mDragButtonStart);
        getDragHandleContainer().addView(mDragButton);
        getDragHandleContainer().addView(mDragButtonEnd);
    }
    
    private LinearLayout getDragHandleContainer() {
        if(mLocation == 1){
            return mDragHandleViewLeft;
        } else {
            return mDragHandleViewRight;
        }
    }
    private void updateDragHandleLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) (20 * mDensity + 0.5), (int) (mEndY - mStartY));
        params.gravity = mLocation == 1 ? Gravity.LEFT : Gravity.RIGHT;
        mDragButton.setLayoutParams(params);

        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                mDragHandleLimiterHeight );
        params.topMargin = mStartY - mDragHandleLimiterHeight;
        params.gravity = mLocation == 1 ? Gravity.LEFT : Gravity.RIGHT;
        mDragButtonStart.setLayoutParams(params);

        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                mDragHandleLimiterHeight);
        params.gravity = mLocation == 1 ? Gravity.LEFT : Gravity.RIGHT;
        mDragButtonEnd.setLayoutParams(params);
        
        mStartYRelative = (int)(mStartY / (getCurrentDisplayHeight() /100));
        mHandleHeight = mEndY - mStartY;
    }
    
    private void updateDragHandleImage() {
        Drawable d = mDragHandle;
        Drawable d1 = mDragHandleStart;
        Drawable d2 = mDragHandleEnd;

        if (mLocation == 1) {
            d = Utils.rotate(mContext.getResources(), d, 180);
            d1 = Utils.rotate(mContext.getResources(), mDragHandleEnd, 180);
            d2 = Utils.rotate(mContext.getResources(), mDragHandleStart, 180);
        }

        mDragButton.setScaleType(ImageView.ScaleType.FIT_XY);
        mDragButton.setImageDrawable(Utils.colorize(mContext.getResources(), mColor, d));
        mDragButton.getDrawable().setColorFilter(mColor, Mode.SRC_ATOP);
        
        mDragButtonStart.setScaleType(ImageView.ScaleType.FIT_XY);
        mDragButtonStart.setImageDrawable(d1);

        mDragButtonEnd.setScaleType(ImageView.ScaleType.FIT_XY);
        mDragButtonEnd.setImageDrawable(d2);
    }

    // cannot use SwitchConfiguration since service must not
    // be running at this point
    private void updateFromPrefs() {
        mStartY = SwitchConfiguration.getInstance(mContext).getCurrentOffsetStart();
        mEndY = SwitchConfiguration.getInstance(mContext).getCurrentOffsetEnd();

        mLocation = mPrefs.getInt(
                SettingsActivity.PREF_DRAG_HANDLE_LOCATION, 0);
        if (mLocation == 1){
            mLocationButton.setText(mContext.getResources().getString(R.string.location_right));
        } else {
            mLocationButton.setText(mContext.getResources().getString(R.string.location_left));
        }
        mColor = mPrefs.getInt(SettingsActivity.PREF_DRAG_HANDLE_COLOR,
                mContext.getResources().getColor(R.color.holo_blue_light));
    }

    public void show() {
        if (mShowing) {
            return;
        }
        updateFromPrefs();
        updateLayout();

        mWindowManager.addView(mView, getGesturePanelLayoutParams());
        mShowing = true;

        Intent intent = new Intent(
                SwitchService.RecentsReceiver.ACTION_HANDLE_HIDE);
        mContext.sendBroadcast(intent);
    }

    public void hide() {
        if (!mShowing) {
            return;
        }

        mWindowManager.removeView(mView);
        mShowing = false;

        Intent intent = new Intent(
                SwitchService.RecentsReceiver.ACTION_HANDLE_SHOW);
        mContext.sendBroadcast(intent);
    }
    
    public void resetPosition() {
        mStartY = SwitchConfiguration.getInstance(mContext).getDefaultOffsetStart();
        mEndY = SwitchConfiguration.getInstance(mContext).getDefaultOffsetEnd();
        updateLayout();
    }
    
    // includes rotation                
    private int getCurrentDisplayHeight(){
        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        return height;
    }
    
    public boolean isShowing() {
        return mShowing;
    }
    
    public void handleRotation(){
        mStartY = SwitchConfiguration.getInstance(mContext).getCustomOffsetStart(mStartYRelative);
        mEndY = SwitchConfiguration.getInstance(mContext).getCustomOffsetEnd(mStartYRelative, mHandleHeight);
        updateLayout();
    }
}
