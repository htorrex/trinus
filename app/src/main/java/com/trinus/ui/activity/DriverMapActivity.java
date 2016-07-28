package com.trinus.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.trinus.R;
import com.trinus.ui.fragment.DriverMapFragment;

/**
 * Class to handle the driver map
 *
 * @author hector
 */
public class DriverMapActivity extends BaseActivity {
    private final String TAG = DriverMapActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate DriverMapActivity");
        setContentView(R.layout.map_activity);

        Bundle params = null;
        if (getIntent().getExtras() != null) {
            params = getIntent().getExtras();
        }

        loadFragment(new DriverMapFragment(), params, "", R.id.mapFragment);
    }
}
