package com.trinus.util;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Wrapper to handle touch events on the map in order to fire a listener to get location
 *
 * @author hetorres
 */
public class MapTouchableWrapper extends FrameLayout {

    public interface MapTouchable {
        void OnMapTouched(float x, float y);
    }

    private static final String TAG = MapTouchableWrapper.class.getSimpleName();

    private MapTouchable mapTouchable;

    public MapTouchableWrapper(Fragment fragment) {
        super(fragment.getContext());
        try {
            mapTouchable = (MapTouchable) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement MapTouchable.");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                Log.d("TOUCH", "ACTION_UP");
                Log.d(TAG, "ev.getY():" + ev.getY());
                Log.d(TAG, "ev.getX():" + ev.getX());
                // use this for api 23 or above
                mapTouchable.OnMapTouched(ev.getX(), ev.getY());
                break;
        }

        return super.dispatchTouchEvent(ev);
    }
}
