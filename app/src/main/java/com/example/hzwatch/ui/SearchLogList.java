package com.example.hzwatch.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hzwatch.R;
import com.example.hzwatch.databinding.SearchLogListBinding;
import com.example.hzwatch.databinding.StandardRecyclerItemBinding;
import com.example.hzwatch.domain.SearchLog;
import com.example.hzwatch.service.Storage;
import com.example.hzwatch.ui.StandardRecyclerAdapter.Controller;

import java.util.List;

public class SearchLogList extends Fragment {
    private SearchLogListBinding binding;
    private Storage storage = Storage.getInstance();
    private StandardRecyclerAdapter<SearchLog, StandardRecyclerItemBinding> recyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SearchLogListBinding.inflate(inflater, container, false);

        List<SearchLog> logList = storage.findSearchLog();

        recyclerAdapter = new StandardRecyclerAdapter<>(R.layout.standard_recycler_item, logList, new Controller<SearchLog, StandardRecyclerItemBinding>() {
            @Override
            public void bind(StandardRecyclerItemBinding binding, SearchLog item) {
                binding.sriName.setText(item.getSearchKey());
            }

            @Override
            public StandardRecyclerItemBinding create(View item) {
                return StandardRecyclerItemBinding.bind(item);
            }
        });

        binding.sllList.setAdapter(recyclerAdapter);
        binding.sllList.setLayoutManager(new LinearLayoutManager(getContext()));

        return binding.getRoot();
    }
}