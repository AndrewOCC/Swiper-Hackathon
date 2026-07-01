package com.listmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.listmanager.ListItemSwipeTouchListener;
import com.listmanager.R;
import com.listmanager.model.ListCategory;
import com.listmanager.model.ListItem;
import com.listmanager.ui.MainViewModel;

import java.util.List;

public class ColumnPagerAdapter extends RecyclerView.Adapter<ColumnPagerAdapter.ColumnPageViewHolder> {

    private static final ListCategory[] CATEGORIES = {
            ListCategory.ARCHIVED,
            ListCategory.INBOX,
            ListCategory.STARRED
    };

    private final LifecycleOwner lifecycleOwner;
    private final MainViewModel viewModel;
    private final RecyclerView.OnItemTouchListener panelBlankAreaListener;
    private int horizontalPadding;
    private int bottomPadding;
    private int extraHorizontalInset;
    private int extraBottomInset;

    public ColumnPagerAdapter(@NonNull LifecycleOwner lifecycleOwner,
                              @NonNull MainViewModel viewModel,
                              int horizontalPadding,
                              int bottomPadding,
                              @NonNull RecyclerView.OnItemTouchListener panelBlankAreaListener) {
        this.lifecycleOwner = lifecycleOwner;
        this.viewModel = viewModel;
        this.horizontalPadding = horizontalPadding;
        this.bottomPadding = bottomPadding;
        this.panelBlankAreaListener = panelBlankAreaListener;
    }

    public void setPagePadding(int extraHorizontalInset, int extraBottomInset) {
        this.extraHorizontalInset = extraHorizontalInset;
        this.extraBottomInset = extraBottomInset;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ColumnPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.column_page, parent, false);
        return new ColumnPageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColumnPageViewHolder holder, int position) {
        holder.bind(CATEGORIES[position]);
    }

    @Override
    public void onViewRecycled(@NonNull ColumnPageViewHolder holder) {
        holder.unbind();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return CATEGORIES.length;
    }

    class ColumnPageViewHolder extends RecyclerView.ViewHolder {

        private final RecyclerView recyclerView;
        private final ListItemAdapter listAdapter;
        private Observer<List<ListItem>> itemsObserver;
        private ListCategory boundCategory;

        ColumnPageViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.column_recycler_view);
            listAdapter = new ListItemAdapter();
            recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            recyclerView.setAdapter(listAdapter);
            applyRecyclerPadding();
        }

        private void applyRecyclerPadding() {
            recyclerView.setPadding(
                    horizontalPadding + extraHorizontalInset,
                    0,
                    horizontalPadding + extraHorizontalInset,
                    bottomPadding + extraBottomInset
            );
        }

        void bind(ListCategory category) {
            unbind();
            boundCategory = category;
            applyRecyclerPadding();

            itemsObserver = items -> listAdapter.submitList(items);
            viewModel.observeItems(category).observe(lifecycleOwner, itemsObserver);

            ListItemSwipeTouchListener itemSwipeListener = new ListItemSwipeTouchListener(
                    recyclerView,
                    () -> boundCategory,
                    new ListItemSwipeTouchListener.Callback() {
                        @Override
                        public void onSwipeLeft(int position) {
                            viewModel.swipeLeft(boundCategory, position);
                        }

                        @Override
                        public void onSwipeRight(int position) {
                            viewModel.swipeRight(boundCategory, position);
                        }
                    }
            );
            recyclerView.addOnItemTouchListener(itemSwipeListener);
            recyclerView.setTag(R.id.tag_item_touch_listener, itemSwipeListener);
            recyclerView.addOnItemTouchListener(panelBlankAreaListener);
            recyclerView.setTag(R.id.tag_panel_touch_listener, panelBlankAreaListener);
        }

        void unbind() {
            if (itemsObserver != null && boundCategory != null) {
                viewModel.observeItems(boundCategory).removeObserver(itemsObserver);
            }
            Object itemListenerTag = recyclerView.getTag(R.id.tag_item_touch_listener);
            if (itemListenerTag instanceof RecyclerView.OnItemTouchListener) {
                recyclerView.removeOnItemTouchListener((RecyclerView.OnItemTouchListener) itemListenerTag);
            }
            Object panelListenerTag = recyclerView.getTag(R.id.tag_panel_touch_listener);
            if (panelListenerTag instanceof RecyclerView.OnItemTouchListener) {
                recyclerView.removeOnItemTouchListener((RecyclerView.OnItemTouchListener) panelListenerTag);
            }
            itemsObserver = null;
            boundCategory = null;
            recyclerView.setTag(R.id.tag_item_touch_listener, null);
            recyclerView.setTag(R.id.tag_panel_touch_listener, null);
        }
    }
}
