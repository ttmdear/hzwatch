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
import android.text.Editable;
import android.text.TextWatcher;

import com.example.hzwatch.databinding.ActivityMainBinding;
import com.example.hzwatch.service.Services;
import com.example.hzwatch.service.Storage;
import com.example.hzwatch.service.StorageSaverService;
import com.example.hzwatch.service.WatcherService;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private Storage storage = Services.getStorage();
    private BroadcastReceiver watcherReceiver;
    private PriceErrorListFragment priceErrorListFragment;
    private SearchLogListFragment searchLogListFragment;
    private PriceErrorDeletedListFragment priceErrorDeletedListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage.setContext(this);
        // storage.load();
        storage.loadTestData();

        priceErrorListFragment = new PriceErrorListFragment();
        searchLogListFragment = new SearchLogListFragment();
        priceErrorDeletedListFragment = new PriceErrorDeletedListFragment();

        watcherReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifyChange();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(watcherReceiver, new IntentFilter(WatcherService.ACTION_CHANGE));

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

        binding.amOkSee.setOnClickListener(v -> {
            storage.setPriceError(false);
            notifyChange();
        });

        binding.amSearchKeyList.setOnLongClickListener(v -> {
            startActivity(new Intent(this, DevelopActivity.class));
            return false;
        });

        // Init tabs
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(),
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            Arrays.asList(priceErrorListFragment, searchLogListFragment, priceErrorDeletedListFragment));

        binding.viewPager.setAdapter(mainPagerAdapter);

        updateOkSeeButton();

        startService(new Intent(this, WatcherService.class));
        startService(new Intent(this, StorageSaverService.class));
    }

    public void notifyChange() {
        priceErrorListFragment.notifyChange();
        searchLogListFragment.notifyChange();
        priceErrorListFragment.notifyChange();
        updateOkSeeButton();
    }

    @Override
    protected void onResume() {
        super.onResume();

        priceErrorListFragment.notifyChange();
        searchLogListFragment.notifyChange();
        priceErrorDeletedListFragment.notifyChange();
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