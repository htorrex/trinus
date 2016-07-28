package com.trinus.ui.activity;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by hetorres on 7/24/16.
 */
public class BaseActivity extends FragmentActivity {

    /**
     * Method that Helps to launch a Fragment
     */
    protected void loadFragment(Fragment frag, Bundle bundle, String tag, @IdRes int layout) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (bundle != null)
            frag.setArguments(bundle);
        transaction.replace(layout, frag, tag);
        transaction.commit();
    }
}
