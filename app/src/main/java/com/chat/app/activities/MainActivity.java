package com.chat.app.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chat.app.R;
import com.chat.app.activities.auth.LoginActivity;
import com.chat.app.adapters.UserAdapter;
import com.chat.app.viewmodels.UserViewModel;
import com.parse.ParseUser;

public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private UserViewModel userViewModel;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProgressBar progressBar = findViewById(R.id.progressbar);
        TextView noUserFound = findViewById(R.id.no_user_text_view);
        SearchView searchView = findViewById(R.id.user_search_view);
        RecyclerView recyclerView = findViewById(R.id.user_recycler_view);
        swipeRefreshLayout = findViewById(R.id.user_swipe_refresh_layout);
        recyclerView.setHasFixedSize(true);
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        userViewModel.allUsers();

        userViewModel.getAllUsersResponse().observe(this, users -> {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);

            if (users.size() > 0) {
                noUserFound.setVisibility(View.GONE);
                userAdapter = new UserAdapter(this, users);
                recyclerView.setAdapter(userAdapter);
            } else {
                String noUser = "No users found";
                noUserFound.setText(noUser);
                noUserFound.setVisibility(View.VISIBLE);
            }
        });

        userViewModel.getErrorResponse().observe(this, errorResponse -> {
            userAdapter = new UserAdapter();
            recyclerView.setAdapter(userAdapter);
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, errorResponse, Toast.LENGTH_SHORT).show();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
                    userViewModel.searchedUser(newText);
                } else {
                    userViewModel.allUsers();
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout_menu) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Logout");
            builder.setIcon(android.R.drawable.ic_lock_power_off);
            builder.setMessage("Are you sure you want to logout?");
            builder.setPositiveButton("Logout", (dialog, which) -> {
                ParseUser.logOutInBackground(e -> {
                    if (e == null) {
                        dialog.dismiss();
                        startActivity(new Intent(this, LoginActivity.class));
                        finishAffinity();
                    } else {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }).setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
            });
            builder.show();
        }

        return true;
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        userViewModel.allUsers();
    }
}
