package com.chat.app.viewmodels;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.chat.app.activities.BaseActivity;
import com.chat.app.services.AuthService;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class AuthViewModel extends ViewModel {
    private AuthService authService = new AuthService();
    private MutableLiveData<String> signUpResponse = new MutableLiveData<>();
    private MutableLiveData<String> loginResponse = new MutableLiveData<>();
    private MutableLiveData<String> errorResponse = new MutableLiveData<>();

    public void signUpUser(String name, String email, String password) {
        if (TextUtils.isEmpty(name) && (TextUtils.isEmpty(email) || !BaseActivity.isValidEmail(email)) || (password.length() < 6 && TextUtils.isEmpty(password))) {
            return;
        } else {
            if (TextUtils.isEmpty(name)) {
                return;
            } else if (TextUtils.isEmpty(email) || !BaseActivity.isValidEmail(email)) {
                return;
            } else if (password.length() < 6 || TextUtils.isEmpty(password)) {
                return;
            }
        }

        authService.signUp(name, email, password, (success, error) -> {
            if (success != null) {
                signUpResponse.setValue(success);
            } else if (error != null) {
                errorResponse.setValue(error);
            }
        });
    }

    public void loginUser(String email, String password) {
        if ((TextUtils.isEmpty(email) || !BaseActivity.isValidEmail(email)) || (password.length() < 6 && TextUtils.isEmpty(password))) {
            return;
        } else {
            if (TextUtils.isEmpty(email) || !BaseActivity.isValidEmail(email)) {
                return;
            } else if (password.length() < 6 || TextUtils.isEmpty(password)) {
                return;
            }
        }

        authService.userVerification(email, (users, error) -> {
            if (users != null && users.size() > 0) {
                boolean isVerified = false;
                for (ParseUser user : users) {
                    isVerified = user.getBoolean("emailVerified");
                }

                if (!isVerified) {
                    errorResponse.setValue("Please check your inbox and complete email verification first");
                } else {
                    authService.login(email, password, (loginSuccess, loginError) -> {
                        if (loginSuccess != null) {
                            loginResponse.setValue(loginSuccess);
                        } else if (loginError != null) {
                            errorResponse.setValue(loginError);
                        }
                    });
                }
            } else if (error != null) {
                errorResponse.setValue(error);
            }
        });
    }

    public LiveData<String> getSignUpResponse() {
        return signUpResponse;
    }

    public LiveData<String> getLoginResponse() {
        return loginResponse;
    }

    public LiveData<String> getErrorResponse() {
        return errorResponse;
    }
}
