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
import com.example.hzwatch.service.Services;
import com.example.hzwatch.service.Storage;
import com.example.hzwatch.service.UiService;
import com.example.hzwatch.ui.StandardRecyclerAdapter.Controller;
import com.example.hzwatch.util.SortUtil;

import java.util.List;

public class SearchLogListFragment extends Fragment {
    private SearchLogListBinding binding;
    private final Storage storage = Services.getStorage();
    private final UiService uiService = Services.getUiService();
    private StandardRecyclerAdapter<SearchLog, StandardRecyclerItemBinding> recyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SearchLogListBinding.inflate(inflater, container, false);

        recyclerAdapter = new StandardRecyclerAdapter<>(R.layout.standard_recycler_item, prepareList(), new Controller<SearchLog, StandardRecyclerItemBinding>() {
            @Override
            public void bind(StandardRecyclerItemBinding binding, SearchLog item) {
                binding.sriName.setText(item.getSearchKey());
                binding.sriDescription.setText(String.format("Liczba produktów %s / %s / %s", item.getProductsNumber(),
                    uiService.formatReadDateTime(item.getAt()), uiService.formatReadDateTime(item.getNextSearchAt())));
                binding.sriDelete.setVisibility(View.GONE);
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

    public void updateView() {
        if (recyclerAdapter != null) recyclerAdapter.setItems(prepareList());
    }

    private List<SearchLog> prepareList() {
        List<SearchLog> searchLogList = storage.findSearchLogAll();
        SortUtil.sortByDateDesc(searchLogList, SearchLog::getAt);

        return searchLogList;
    }
}