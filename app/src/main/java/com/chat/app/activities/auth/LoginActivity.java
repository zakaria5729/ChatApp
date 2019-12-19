package com.chat.app.activities.auth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.app.R;
import com.chat.app.activities.MainActivity;
import com.chat.app.viewmodels.AuthViewModel;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private EditText emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ParseUser user = ParseUser.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(this, MainActivity.class));
            finishAffinity();
        }

        Button loginButton = findViewById(R.id.login_button);
        TextView signUpHere = findViewById(R.id.sign_up_here_text_view);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);

        signUpHere.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));

        authViewModel = ViewModelProviders.of(this).get(AuthViewModel.class);

        loginButton.setOnClickListener(v ->
                authViewModel.loginUser(emailEditText.getText().toString(), passwordEditText.getText().toString()));

        authViewModel.getLoginResponse().observe(this, sessionToken -> {
            if (sessionToken != null) {
                startActivity(new Intent(this, MainActivity.class));
                finishAffinity();
            }
        });

        authViewModel.getErrorResponse().observe(this, errorResponse -> {
            if (errorResponse != null) {
                ParseUser.logOut();
                Toast.makeText(this, errorResponse, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
