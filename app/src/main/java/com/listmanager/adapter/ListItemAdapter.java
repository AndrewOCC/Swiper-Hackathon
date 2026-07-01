package com.listmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.listmanager.R;
import com.listmanager.model.ListItem;

import java.util.ArrayList;
import java.util.List;

public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ItemViewHolder> {

    private final List<ListItem> items = new ArrayList<>();

    public void submitList(List<ListItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.li_list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        resetSwipeViewState(holder.itemView);
        ListItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());
    }

    static void resetSwipeViewState(@NonNull View itemView) {
        View foreground = itemView.findViewById(R.id.swipe_foreground);
        if (foreground != null) {
            foreground.animate().cancel();
            foreground.setTranslationX(0f);
            foreground.setAlpha(1f);
        }

        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        if (params != null) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            itemView.setLayoutParams(params);
        }

        View backgroundLeft = itemView.findViewById(R.id.swipe_background_left);
        if (backgroundLeft != null) {
            backgroundLeft.setVisibility(View.GONE);
        }
        View backgroundRight = itemView.findViewById(R.id.swipe_background_right);
        if (backgroundRight != null) {
            backgroundRight.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView description;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            description = itemView.findViewById(R.id.item_description);
        }
    }
}
