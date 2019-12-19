package com.chat.app.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;

import com.chat.app.R;
import com.chat.app.activities.BaseActivity;
import com.chat.app.viewmodels.AuthViewModel;

public class SignUpActivity extends BaseActivity {

    private AuthViewModel authViewModel;
    private EditText nameEditText, emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Button signUpButton = findViewById(R.id.sign_up_button);
        TextView loginHere = findViewById(R.id.login_here_text_view);
        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);

        authViewModel = ViewModelProviders.of(this).get(AuthViewModel.class);

        loginHere.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        signUpButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            authViewModel.signUpUser(name, email, password);
        });

        authViewModel.getSignUpResponse().observe(this, signUpResponse -> {
            if (signUpResponse != null) {
                Toast.makeText(this, signUpResponse, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        authViewModel.getErrorResponse().observe(this, errorResponse -> {
            if (errorResponse != null) {
                Toast.makeText(this, errorResponse, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
