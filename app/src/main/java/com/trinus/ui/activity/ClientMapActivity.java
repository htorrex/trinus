package com.trinus.ui.activity;

import android.os.Bundle;
import android.util.Log;

import com.trinus.R;
import com.trinus.ui.fragment.ClientMapFragment;

/**
 * Class to handle the main screen
 *
 * @author hector
 */
public class ClientMapActivity extends BaseActivity {
    private final String TAG = ClientMapActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate ClientMapActivity");
        setContentView(R.layout.map_activity);

        Bundle params = null;
        if (getIntent().getExtras() != null) {
            params = getIntent().getExtras();
        }

        loadFragment(new ClientMapFragment(), params, "", R.id.mapFragment);
    }
}
