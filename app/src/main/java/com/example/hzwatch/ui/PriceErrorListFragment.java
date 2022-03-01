package com.example.hzwatch.ui;

import static java.lang.String.format;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hzwatch.R;
import com.example.hzwatch.databinding.PriceErrorListBinding;
import com.example.hzwatch.databinding.StandardRecyclerItemBinding;
import com.example.hzwatch.domain.PriceError;
import com.example.hzwatch.service.HzwatchService;
import com.example.hzwatch.service.Services;
import com.example.hzwatch.service.Storage;
import com.example.hzwatch.service.UiService;
import com.example.hzwatch.util.SortUtil;

import java.util.Collections;
import java.util.List;

public class PriceErrorListFragment extends Fragment {
    private final Storage storage = Services.getStorage();
    private final UiService uiService = Services.getUiService();
    private final HzwatchService hzwatchService = Services.getHzwatchService();

    private PriceErrorListBinding binding;
    private StandardRecyclerAdapter<PriceError, StandardRecyclerItemBinding> recyclerAdapter;

    public void notifyChange() {
        if (recyclerAdapter != null) {
            recyclerAdapter.setItems(prepareList());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = PriceErrorListBinding.inflate(inflater, container, false);

        recyclerAdapter = new StandardRecyclerAdapter<>(R.layout.standard_recycler_item, prepareList(), new StandardRecyclerAdapter.Controller<PriceError, StandardRecyclerItemBinding>() {
            @Override
            public void bind(StandardRecyclerItemBinding binding, PriceError priceError) {
                binding.sriName.setText(priceError.getProduct());
                binding.sriDescription.setText(format("Cena %s do śr. %s / %s", uiService.formatNumber(priceError.getPrice()), uiService.formatNumber(priceError.getAvr()), uiService.formatReadDateTime(priceError.getAt())));
                binding.sriDelete.setOnClickListener(v -> {
                    storage.deletePriceError(priceError.getId());
                    notifyChange();
                });
            }

            @Override
            public StandardRecyclerItemBinding create(View item) {
                return StandardRecyclerItemBinding.bind(item);
            }

            @Override
            public boolean onClickAction(PriceError priceError) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(hzwatchService.getHzUrl(priceError.getHzId()))));
                return false;
            }
        });

        binding.prlList.setAdapter(recyclerAdapter);
        binding.prlList.setLayoutManager(new LinearLayoutManager(getContext()));

        return binding.getRoot();
    }

    private List<PriceError> prepareList() {
        List<PriceError> list = storage.findPriceErrorAll();
        SortUtil.sortDesc(list, PriceError::getAt);

        return list;
    }
}