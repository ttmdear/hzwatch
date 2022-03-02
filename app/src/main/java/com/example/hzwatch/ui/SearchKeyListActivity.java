package com.example.hzwatch.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hzwatch.R;
import com.example.hzwatch.databinding.ActivitySearchKeyListBinding;
import com.example.hzwatch.databinding.StandardRecyclerItemBinding;
import com.example.hzwatch.domain.SearchKey;
import com.example.hzwatch.service.HzwatchService;
import com.example.hzwatch.service.Services;
import com.example.hzwatch.util.SortUtil;

import java.util.Collections;
import java.util.List;

public class SearchKeyListActivity extends AppCompatActivity {
    private final HzwatchService hzwatchService = Services.getHzwatchService();

    private ActivitySearchKeyListBinding binding;
    private StandardRecyclerAdapter<SearchKey, StandardRecyclerItemBinding> recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySearchKeyListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recyclerAdapter = new StandardRecyclerAdapter<>(R.layout.standard_recycler_item, Collections.emptyList(), new StandardRecyclerAdapter.Controller<SearchKey, StandardRecyclerItemBinding>() {
            @Override
            public void bind(StandardRecyclerItemBinding binding, SearchKey searchKey) {
                binding.sriName.setText(searchKey.getValue());
                binding.sriDescription.setVisibility(View.GONE);
                binding.sriDelete.setOnClickListener(v -> {
                    deleteSearchKey(searchKey);
                });
            }

            @Override
            public StandardRecyclerItemBinding create(View item) {
                return StandardRecyclerItemBinding.bind(item);
            }
        });

        binding.asklList.setAdapter(recyclerAdapter);
        binding.asklList.setLayoutManager(new LinearLayoutManager(this));

        binding.asklAdd.setOnClickListener(v -> {
            String searchKeyString = binding.asklName.getText().toString();

            if (!searchKeyString.isEmpty() && !hzwatchService.existsSearchKey(searchKeyString)) {
                hzwatchService.createSearchKey(searchKeyString);
                reloadList();
            }
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