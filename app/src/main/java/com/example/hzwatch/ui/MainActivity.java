package com.example.hzwatch.ui;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.example.hzwatch.databinding.ActivityMainBinding;
import com.example.hzwatch.service.Services;
import com.example.hzwatch.service.Storage;
import com.example.hzwatch.service.StorageSaverService;
import com.example.hzwatch.worker.WatcherWorker;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    private final Storage storage = Services.getStorage();

    private ActionChangeReceiver actionChangeReceiver;
    private ActionStatusChangeReceiver actionStatusChangeReceiver;

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

        // Init tabs
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(),
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            Arrays.asList(priceErrorListFragment, searchLogListFragment, priceErrorMovedListFragment));

        binding.viewPager.setAdapter(mainPagerAdapter);

        updateOkSeeButton();

        WatcherWorker.planWork(this);

        this.registerReceiver(actionChangeReceiver = this.new ActionChangeReceiver(), new IntentFilter(WatcherWorker.ACTION_CHANGE));
        this.registerReceiver(actionStatusChangeReceiver = this.new ActionStatusChangeReceiver(), new IntentFilter(WatcherWorker.ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");
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

        Log.d(TAG, "onResume");
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

        // LocalBroadcastManager.getInstance(this).unregisterReceiver(watcherReceiver);
        unregisterReceiver(actionChangeReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    private class ActionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateView();
        }
    }

    private class ActionStatusChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            binding.acStateMsg.setText(intent.getStringExtra("msg"));
        }
    }
}