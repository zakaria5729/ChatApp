package com.chat.app.activities;

import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseActivity extends AppCompatActivity {

    public static boolean isValidEmail(String email) {
        boolean isValid = false;
        String expression = "^[\\w.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            isValid = true;
        }
        return !isValid;
    }

    public int checkUserPermission(String permission, int requestCode) {
        int permissionGranted = PackageManager.PERMISSION_DENIED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!TextUtils.isEmpty(permission)) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{permission}, requestCode);
                } else {
                    permissionGranted = PackageManager.PERMISSION_GRANTED;
                }
            }
        }

        return permissionGranted;
    }
}
