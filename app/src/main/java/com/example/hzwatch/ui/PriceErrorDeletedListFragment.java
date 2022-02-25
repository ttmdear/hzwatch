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
import com.example.hzwatch.databinding.PriceErrorDeletedListBinding;
import com.example.hzwatch.databinding.StandardRecyclerItemBinding;
import com.example.hzwatch.domain.PriceError;
import com.example.hzwatch.service.Services;
import com.example.hzwatch.service.Storage;
import com.example.hzwatch.service.UiService;
import com.example.hzwatch.util.SortUtil;

import java.util.List;

public class PriceErrorDeletedListFragment extends Fragment {
    private final Storage storage = Services.getStorage();
    private final UiService uiService = Services.getUiService();
    private PriceErrorDeletedListBinding binding;
    private StandardRecyclerAdapter<PriceError, StandardRecyclerItemBinding> recyclerAdapter;

    public void notifyChange() {
        if (recyclerAdapter != null) {
            recyclerAdapter.setItems(prepareList());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = PriceErrorDeletedListBinding.inflate(inflater, container, false);

        recyclerAdapter = new StandardRecyclerAdapter<>(R.layout.standard_recycler_item, prepareList(), new StandardRecyclerAdapter.Controller<PriceError, StandardRecyclerItemBinding>() {
            @Override
            public void bind(StandardRecyclerItemBinding binding, PriceError priceError) {
                binding.sriName.setText(priceError.getProduct());
                binding.sriDescription.setText(format("Cena %s do śr. %s / %s", priceError.getPrice(), priceError.getAvr(), uiService.formatReadDateTime(priceError.getAt())));
                binding.sriDelete.setVisibility(View.GONE);
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

        binding.pedlList.setAdapter(recyclerAdapter);
        binding.pedlList.setLayoutManager(new LinearLayoutManager(getContext()));

        return binding.getRoot();
    }

    private List<PriceError> prepareList() {
        List<PriceError> list = storage.findPriceErrorDeletedAll();
        SortUtil.sort(list, (o1, o2) -> o1.getAt().compareTo(o2.getAt()));

        return list;
    }
}