package com.example.hzwatch.ui;

import static java.lang.String.format;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hzwatch.R;
import com.example.hzwatch.databinding.ActivityDevelopBinding;
import com.example.hzwatch.databinding.StandardRecyclerItemBinding;
import com.example.hzwatch.domain.LogEntry;
import com.example.hzwatch.service.LoggerService;
import com.example.hzwatch.service.Services;
import com.example.hzwatch.service.Storage;
import com.example.hzwatch.service.WatcherService;
import com.example.hzwatch.util.SortUtil;

import java.util.List;

public class DevelopActivity extends AppCompatActivity {
    private ActivityDevelopBinding binding;
    private final LoggerService loggerService = Services.getLoggerService();
    private final Storage storage = Services.getStorage();

    private StandardRecyclerAdapter<LogEntry, StandardRecyclerItemBinding> recyclerAdapter;
    private BroadcastReceiver watcherReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDevelopBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        watcherReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateView();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(watcherReceiver, new IntentFilter(WatcherService.ACTION_CHANGE));

        binding.adCleanHzData.setOnClickListener(v -> {
            storage.cleanHzData();
        });

        binding.adCleanLogData.setOnClickListener(v -> {
            storage.cleanLogData();
            updateView();
        });

        recyclerAdapter = new StandardRecyclerAdapter<>(R.layout.standard_recycler_item, prepareList(), new StandardRecyclerAdapter.Controller<LogEntry, StandardRecyclerItemBinding>() {
            @Override
            public void bind(StandardRecyclerItemBinding binding, LogEntry logEntry) {
                binding.sriName.setText(format("%s - %s", logEntry.getAt(), logEntry.getMsg()));
                binding.sriDescription.setVisibility(View.GONE);
                binding.sriDelete.setVisibility(View.GONE);
            }

            @Override
            public StandardRecyclerItemBinding create(View item) {
                return StandardRecyclerItemBinding.bind(item);
            }
        });

        binding.adLogList.setAdapter(recyclerAdapter);
        binding.adLogList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(watcherReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateView();
    }

    private List<LogEntry> prepareList() {
        return SortUtil.sortByDateDesc(loggerService.getLogEntryAll(), LogEntry::getAt);
    }

    public void updateView() {
        if (recyclerAdapter != null) {
            recyclerAdapter.setItems(prepareList());
        }
    }
}