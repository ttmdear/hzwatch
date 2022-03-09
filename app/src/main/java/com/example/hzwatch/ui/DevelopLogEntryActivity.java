package com.example.hzwatch.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hzwatch.databinding.ActivityDevelopLogEntryBinding;
import com.example.hzwatch.service.Logger;
import com.example.hzwatch.service.Services;

public class DevelopLogEntryActivity extends AppCompatActivity {
    private final Logger logger = Services.getLogger();
    private ActivityDevelopLogEntryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDevelopLogEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.adleContent.setText(logger.getLogEntry(getIntent().getIntExtra("logEntryId", 0)).getMsg());
    }
}