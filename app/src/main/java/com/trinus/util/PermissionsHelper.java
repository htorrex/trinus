package com.trinus.util;


import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Utility class to verify if we have permissions or not to an specific permission.
 *
 * @author hector.torres
 */
public final class PermissionsHelper {

    private final Context context;

    public PermissionsHelper(Context context) {
        this.context = context;
    }

    public boolean permissionsCheck(String... permissions) {
        for (String permission : permissions) {
            if (needPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    private boolean needPermission(String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED;
    }
}
