package com.example.hzwatch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.hzwatch.databinding.ActivityMainBinding;
import com.example.hzwatch.service.Storage;
import com.example.hzwatch.service.WatcherService;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private Storage storage = Storage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        startService(new Intent(this, WatcherService.class));
    }
}