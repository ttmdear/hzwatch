package com.example.hzwatch.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hzwatch.databinding.ActivityDevelopBinding;
import com.example.hzwatch.service.Services;
import com.example.hzwatch.service.Storage;

public class DevelopActivity extends AppCompatActivity {
    private ActivityDevelopBinding binding;
    private Storage storage = Services.getStorage();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDevelopBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.adClean.setOnClickListener(v -> {
            storage.clean();
        });

        binding.adLoadTestData.setOnClickListener(v -> {
            storage.loadTestData();
        });
    }
}