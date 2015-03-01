package com.t1.mihai.t1;

import com.t1.mihai.t1.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import com.t1.mihai.t1.FragmentOptions;
import com.t1.mihai.t1.FragmentOptions.OnFragmentInteractionListener;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class ActivityMain extends Activity implements OnFragmentInteractionListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;

    private boolean _bMenuVisible = false;
    FragmentOptions _fragMenu;

    float _fSizeX = 0;

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            if(_bMenuVisible){
                if ((e1.getX() > _fSizeX * 0.55f) && (e2.getX() - e1.getX() > _fSizeX * 0.2f)){

                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.setCustomAnimations(R.anim.enter, R.anim.exit);
                    ft.remove(_fragMenu);
                    ft.commit();

                    _bMenuVisible = false;
                }
            }
            else {
                if ((e1.getX() > _fSizeX * 0.95f) && (e1.getX() - e2.getX() > _fSizeX * 0.1f)) {
                    final View menuView = findViewById(R.id.menu_holder);
                    ViewGroup.LayoutParams layoutParams = menuView.getLayoutParams();
                    layoutParams.width = (int) (_fSizeX * 0.2f);
                    menuView.setLayoutParams(layoutParams);

                    if(null == _fragMenu)
                        _fragMenu = new FragmentOptions();

                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.setCustomAnimations(R.anim.enter, R.anim.exit);
                    ft.add(R.id.menu_holder, _fragMenu, "fo");
                    ft.commit();

                    _bMenuVisible = true;
                }
            }

            //Log.d("dist", "dist: " + (e1.getX() - e2.getX()));
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);
        final View anchorView = findViewById(R.id.menu_holder);

        controlsView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.

        mSystemUiHider = SystemUiHider.getInstance(this, anchorView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                // Cached values.
                int mControlsHeight;
                int mShortAnimTime;

                @Override
                @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                public void onVisibilityChange(boolean visible) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                        // If the ViewPropertyAnimator API is available
                        // (Honeycomb MR2 and later), use it to animate the
                        // in-layout UI controls at the bottom of the
                        // screen.
                        if (mControlsHeight == 0) {
                            mControlsHeight = controlsView.getHeight();
                        }
                        if (mShortAnimTime == 0) {
                            mShortAnimTime = getResources().getInteger(
                                    android.R.integer.config_shortAnimTime);
                        }
                        controlsView.animate()
                                .translationY(visible ? 0 : mControlsHeight)
                                .setDuration(mShortAnimTime);
                    } else {
                        // If the ViewPropertyAnimator APIs aren't
                        // available, simply show or hide the in-layout UI
                        // controls.
                        controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                    }

                    if (visible && AUTO_HIDE) {
                        // Schedule a hide().
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                }
            });

        // Set up the user interaction to manually show or hide the system UI.

        /*
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });*/

        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener =  new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            };

        contentView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                _fSizeX = right - left;
            }
        });

        contentView.setOnTouchListener(gestureListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }
}
