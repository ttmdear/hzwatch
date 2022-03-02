package com.example.hzwatch.ui;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.example.hzwatch.databinding.ActivityMainBinding;
import com.example.hzwatch.service.HzwatchService;
import com.example.hzwatch.service.Services;
import com.example.hzwatch.service.Storage;
import com.example.hzwatch.service.StorageSaverService;
import com.example.hzwatch.service.WatcherService;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private Storage storage = Services.getStorage();
    private HzwatchService hzwatchService = Services.getHzwatchService();

    private BroadcastReceiver watcherReceiver;

    private PriceErrorListFragment priceErrorListFragment;
    private SearchLogListFragment searchLogListFragment;
    private PriceErrorMovedListFragment priceErrorMovedListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage.setContext(this);
        storage.load();

        priceErrorListFragment = new PriceErrorListFragment();
        searchLogListFragment = new SearchLogListFragment();
        priceErrorMovedListFragment = new PriceErrorMovedListFragment();

        watcherReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateView();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(watcherReceiver, new IntentFilter(WatcherService.ACTION_CHANGE));

        binding.amOkSee.setOnClickListener(v -> {
            storage.setPriceError(false);
            updateView();
        });

        binding.amProducts.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchKeyListActivity.class));
        });

        binding.amLogo.setOnLongClickListener(v -> {
            startActivity(new Intent(this, DevelopActivity.class));
            return false;
        });

        // binding.amSearchKeyList.setOnLongClickListener(v -> {
        //     startActivity(new Intent(this, DevelopActivity.class));
        //     return false;
        // });

        // Init tabs
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(),
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            Arrays.asList(priceErrorListFragment, searchLogListFragment, priceErrorMovedListFragment));

        binding.viewPager.setAdapter(mainPagerAdapter);

        updateOkSeeButton();

        startService(new Intent(this, WatcherService.class));
        startService(new Intent(this, StorageSaverService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateView();
    }

    public void updateView() {
        priceErrorListFragment.updateView();
        searchLogListFragment.updateView();
        priceErrorMovedListFragment.updateView();

        updateOkSeeButton();
    }

    @Override
    protected void onResume() {
        super.onResume();

        priceErrorListFragment.updateView();
        searchLogListFragment.updateView();
        priceErrorMovedListFragment.updateView();
    }

    private void updateOkSeeButton() {
        if (storage.getPriceError()) {
            binding.amOkSee.setVisibility(VISIBLE);
        } else {
            binding.amOkSee.setVisibility(GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(watcherReceiver);
    }
}