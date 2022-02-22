package com.example.hzwatch.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.hzwatch.databinding.ActivityMainBinding;
import com.example.hzwatch.service.Storage;
import com.example.hzwatch.service.WatcherService;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private Storage storage = Storage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage.load();

        // Init text
        binding.searchKeyList.setText(storage.getSearchKeyList() == null ? "" : storage.getSearchKeyList());
        binding.searchKeyList.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                storage.setSearchKeyList(s.toString());
            }
        });

        // Init tabs
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        MainTabAdapter mainTabAdapter = new MainTabAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, Arrays.asList(new PriceErrorList(), new SearchLogList()));
        binding.viewPager.setAdapter(mainTabAdapter);

        startService(new Intent(this, WatcherService.class));
    }
}