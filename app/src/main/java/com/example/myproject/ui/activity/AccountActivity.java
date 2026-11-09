package com.example.myproject.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.myproject.R;
import com.example.myproject.databinding.ActivityHomeBinding;
import com.example.myproject.ui.fragment.AccountFragment;
import com.example.myproject.ui.fragment.FavouriteFragment;
import com.example.myproject.ui.fragment.HomeFragment;
import com.google.android.material.navigation.NavigationBarView;

public class AccountActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.fragmentContainer.getId(), new AccountFragment())
                .commit();

        Intent intent = new Intent();
        long userId = intent.getLongExtra("userId", -1L);
        String username = intent.getStringExtra("username");
        String password = intent.getStringExtra("password");

        binding.mainBottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                Bundle bundle = new Bundle();
                bundle.putLong("userId", userId);
                bundle.putString("username", username);
                bundle.putString("password", password);
                if (item.getItemId() == R.id.nav_find) {
                    fragment = new HomeFragment();
                    fragment.setArguments(bundle);
                } else if (item.getItemId() == R.id.nav_collect) {
                    fragment = new FavouriteFragment();
                    fragment.setArguments(bundle);
                } else if (item.getItemId() == R.id.nav_account) {
                    fragment = new AccountFragment();
                    fragment.setArguments(bundle);
                }
                if (fragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(binding.fragmentContainer.getId(), fragment)
                            .commit();
                }
                return true;
            }
        });
    }
}