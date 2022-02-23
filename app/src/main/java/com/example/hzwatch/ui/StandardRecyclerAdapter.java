package com.example.hzwatch.ui;

import android.annotation.SuppressLint;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hzwatch.R;
import com.example.hzwatch.domain.Entity;
import com.example.hzwatch.util.Util;

import java.util.List;

public class StandardRecyclerAdapter<T extends Entity, B> extends RecyclerView.Adapter<StandardRecyclerAdapter.StandardViewHolder<T, B>> {
    private final int layoutId;
    private final Controller<T, B> controller;

    private List<T> items;

    public StandardRecyclerAdapter(List<T> items, Controller<T, B> controller) {
        this.layoutId = R.layout.standard_recycler_item;
        this.items = items;
        this.controller = controller;
    }

    public StandardRecyclerAdapter(int layoutId, List<T> items, Controller<T, B> controller) {
        this.layoutId = layoutId;
        this.items = items;
        this.controller = controller;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<T> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }


    @Override
    public void onBindViewHolder(@NonNull StandardViewHolder<T, B> holder, int position) {
        holder.entity = items.get(position);
        holder.itemView.setOnClickListener(v -> controller.onClickAction(holder.entity));

        controller.bind(holder.binding, holder.entity);
    }

    @NonNull
    @Override
    public StandardViewHolder<T, B> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new StandardViewHolder<>(this, view, controller.create(view), controller);
    }

    public interface Controller<T, B> {
        B create(View item);

        void bind(B binding, T item);

        default boolean onClickAction(T entity) {
            return false;
        }

        default boolean onDeleteAction(T entity) {
            return false;
        }

        default boolean onOrderChanged() {
            return false;
        }

        default boolean isAutoUpdateOrder() {
            return true;
        }
    }

    public void moveItemDown(Integer itemId) {
        int indexA = Util.getIndexById(items, itemId);

        if (indexA == items.size() - 1) {
            return;
        }

        int indexB = indexA + 1;

        T b = items.get(indexB);

        items.set(indexB, items.get(indexA));
        items.set(indexA, b);

        notifyItemRangeChanged(indexA, 2);
        controller.onOrderChanged();

        updateItemOrderField();
    }

    public void moveItemUp(Integer itemId) {
        int indexA = Util.getIndexById(items, itemId);

        if (indexA == 0) {
            return;
        }

        int indexB = indexA - 1;

        T b = items.get(indexB);

        items.set(indexB, items.get(indexA));
        items.set(indexA, b);

        notifyItemRangeChanged(indexA - 1, 2);
        controller.onOrderChanged();

        updateItemOrderField();
    }

    private void updateItemOrderField() {
        if (!controller.isAutoUpdateOrder()) return;

        for (int i = 0; i < items.size(); i++) {
            items.get(i).setOrder(i);
        }
    }

    public void deleteItem(Integer itemId) {
        int i = Util.getIndexById(items, itemId);

        if (i == -1) throw new IllegalStateException(String.format("There is no item with id [%s].", itemId));

        if (controller.onDeleteAction(items.get(i))) {
            items.remove(i);
            notifyItemRemoved(i);
        }
    }

    public static class StandardViewHolder<T extends Entity, B> extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        StandardRecyclerAdapter<T, B> adapter;
        B binding;
        T entity;
        Controller<T, B> controller;

        public StandardViewHolder(StandardRecyclerAdapter<T, B> adapter, @NonNull View itemView, B binding, Controller<T, B> controller) {
            super(itemView);
            this.adapter = adapter;
            this.binding = binding;
            this.entity = null;
            this.controller = controller;

            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem delete = menu.add(Menu.NONE, 0, 2, "Delete");
            MenuItem moveUp = menu.add(Menu.NONE, 0, 2, "Move Up");
            MenuItem moveDown = menu.add(Menu.NONE, 0, 2, "Move Down");

            delete.setOnMenuItemClickListener(item -> {
                adapter.deleteItem(entity.getId());
                return false;
            });

            moveUp.setOnMenuItemClickListener(item -> {
                adapter.moveItemUp(entity.getId());
                return false;
            });

            moveDown.setOnMenuItemClickListener(item -> {
                adapter.moveItemDown(entity.getId());
                return false;
            });
        }
    }
}
