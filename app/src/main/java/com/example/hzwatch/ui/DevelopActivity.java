package com.example.hzwatch.ui;

import static java.lang.String.format;

import android.app.Activity;
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
import com.example.hzwatch.databinding.LogRecyclerItemBinding;
import com.example.hzwatch.domain.LogEntry;
import com.example.hzwatch.service.Logger;
import com.example.hzwatch.service.Services;
import com.example.hzwatch.service.Storage;
import com.example.hzwatch.service.WatcherService;
import com.example.hzwatch.util.SortUtil;

import java.util.List;

public class DevelopActivity extends AppCompatActivity {
    private ActivityDevelopBinding binding;
    private final Logger logger = Services.getLogger();
    private final Storage storage = Services.getStorage();

    private StandardRecyclerAdapter<LogEntry, LogRecyclerItemBinding> recyclerAdapter;
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

        final Activity activity = this;

        recyclerAdapter = new StandardRecyclerAdapter<>(R.layout.log_recycler_item, prepareList(), new StandardRecyclerAdapter.Controller<LogEntry, LogRecyclerItemBinding>() {
            @Override
            public void bind(LogRecyclerItemBinding binding, LogEntry logEntry) {
                String message = logEntry.getMsg();

                if (message.length() > 300) {
                    message = message.substring(0, 100);
                }

                binding.lriLog.setText(format("%s - %s", logEntry.getAt(), message));
            }

            @Override
            public boolean onClickAction(LogEntry entity) {
                Intent intent = new Intent(activity, DevelopLogEntryActivity.class);
                intent.putExtra("logEntryId", entity.getId());
                startActivity(intent);

                return false;
            }

            @Override
            public LogRecyclerItemBinding create(View item) {
                return LogRecyclerItemBinding.bind(item);
            }
        });

        binding.adLogList.setAdapter(recyclerAdapter);
        binding.adLogList.setLayoutManager(new LinearLayoutManager(this));

        binding.adReloadLogs.setOnClickListener(v -> {
            recyclerAdapter.setItems(prepareList());
        });
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
        return SortUtil.sortByDateDesc(logger.getLogEntryAll(), LogEntry::getAt);
    }

    public void updateView() {
        if (recyclerAdapter != null) {
            recyclerAdapter.setItems(prepareList());
        }
    }
}