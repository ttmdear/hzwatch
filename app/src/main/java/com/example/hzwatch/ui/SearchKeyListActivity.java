package com.example.hzwatch.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hzwatch.R;
import com.example.hzwatch.databinding.ActivitySearchKeyListBinding;
import com.example.hzwatch.databinding.StandardRecyclerNarrowItemBinding;
import com.example.hzwatch.domain.SearchKey;
import com.example.hzwatch.service.HzwatchService;
import com.example.hzwatch.service.Services;
import com.example.hzwatch.util.SortUtil;

import java.util.Collections;
import java.util.List;

public class SearchKeyListActivity extends AppCompatActivity {
    private final HzwatchService hzwatchService = Services.getHzwatchService();

    private ActivitySearchKeyListBinding binding;
    private StandardRecyclerAdapter<SearchKey, StandardRecyclerNarrowItemBinding> recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySearchKeyListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recyclerAdapter = new StandardRecyclerAdapter<>(R.layout.standard_recycler_narrow_item, Collections.emptyList(), new StandardRecyclerAdapter.Controller<SearchKey, StandardRecyclerNarrowItemBinding>() {
            @Override
            public void bind(StandardRecyclerNarrowItemBinding binding, SearchKey searchKey) {
                binding.sriName.setText(searchKey.getValue());
                binding.sriDescription.setVisibility(View.GONE);
                binding.sriDelete.setOnClickListener(v -> {
                    deleteSearchKey(searchKey);
                });
            }

            @Override
            public StandardRecyclerNarrowItemBinding create(View item) {
                return StandardRecyclerNarrowItemBinding.bind(item);
            }
        });

        binding.asklList.setAdapter(recyclerAdapter);
        binding.asklList.setLayoutManager(new LinearLayoutManager(this));

        binding.asklAdd.setOnClickListener(v -> {
            String searchKeyString = binding.asklName.getText().toString();

            if (!searchKeyString.isEmpty() && hzwatchService.notExistsSearchKey(searchKeyString)) {
                hzwatchService.createSearchKey(searchKeyString);
                reloadList();
            }
        });

        binding.asklCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("hz", hzwatchService.getSearchKeyString()));
        });

        binding.asklPaste.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData abc = clipboard.getPrimaryClip();
            ClipData.Item item = abc.getItemAt(0);

            hzwatchService.importSearchKey(item.getText().toString());
            reloadList();
        });

        reloadList();
    }

    private void deleteSearchKey(SearchKey searchKey) {
        hzwatchService.deleteSearchKey(searchKey.getId());
        reloadList();
    }

    @Override
    protected void onStart() {
        super.onStart();

        reloadList();
    }

    private void reloadList() {
        if (recyclerAdapter != null) recyclerAdapter.setItems(prepareList());
    }

    private List<SearchKey> prepareList() {
        return SortUtil.sortByStringAsc(hzwatchService.getSearchKeyAll(), SearchKey::getValue);
    }
}