package com.example.hzwatch.ui;

import static android.view.View.GONE;

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
import com.example.hzwatch.domain.SearchLog;
import com.example.hzwatch.service.Services;
import com.example.hzwatch.service.Storage;
import com.example.hzwatch.service.UiService;
import com.example.hzwatch.util.SortUtil;

import java.util.List;

public class PriceErrorListFragment extends Fragment {
    private PriceErrorListBinding binding;
    private final Storage storage = Services.getStorage();
    private final UiService uiService = Services.getUiService();
    private StandardRecyclerAdapter<PriceError, StandardRecyclerItemBinding> recyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = PriceErrorListBinding.inflate(inflater, container, false);

        recyclerAdapter = new StandardRecyclerAdapter<>(R.layout.standard_recycler_item, storage.findPriceError(), new StandardRecyclerAdapter.Controller<PriceError, StandardRecyclerItemBinding>() {
            @Override
            public void bind(StandardRecyclerItemBinding binding, PriceError item) {
                binding.sriName.setText(item.getProduct());
                binding.sriDescription.setText(format("Cena %s do śr. %s / %s", item.getPrice(), item.getAvr(), uiService.formatReadDateTime(item.getAt())));
            }

            @Override
            public StandardRecyclerItemBinding create(View item) {
                return StandardRecyclerItemBinding.bind(item);
            }

            @Override
            public boolean onClickAction(PriceError priceError) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(priceError.getUrl())));
                return false;
            }
        });

        binding.prlList.setAdapter(recyclerAdapter);
        binding.prlList.setLayoutManager(new LinearLayoutManager(getContext()));

        return binding.getRoot();
    }

    public void notifyChange() {
        recyclerAdapter.setItems(storage.findPriceError());
    }

    private List<PriceError> prepareList() {
        List<PriceError> list = storage.findPriceError();
        SortUtil.sort(list, (o1, o2) -> o1.getAt().compareTo(o2.getAt()));

        return list;
    }
}