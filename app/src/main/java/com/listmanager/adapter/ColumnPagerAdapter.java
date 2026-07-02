package com.listmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.listmanager.ListItemSwipeController;
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
    private int horizontalPadding;
    private int bottomPadding;
    private int extraHorizontalInset;
    private int extraBottomInset;

    private final ColumnPageViewHolder[] pageHolders = new ColumnPageViewHolder[CATEGORIES.length];
    @Nullable
    private Observer<String> editingItemObserver;

    public ColumnPagerAdapter(@NonNull LifecycleOwner lifecycleOwner,
                              @NonNull MainViewModel viewModel,
                              int horizontalPadding,
                              int bottomPadding) {
        this.lifecycleOwner = lifecycleOwner;
        this.viewModel = viewModel;
        this.horizontalPadding = horizontalPadding;
        this.bottomPadding = bottomPadding;

        editingItemObserver = editingItemId -> {
            for (ColumnPageViewHolder holder : pageHolders) {
                if (holder != null) {
                    holder.applyEditingItemId(editingItemId);
                }
            }
        };
        viewModel.getEditingItemId().observe(lifecycleOwner, editingItemObserver);
    }

    public void setPagePadding(int extraHorizontalInset, int extraBottomInset) {
        this.extraHorizontalInset = extraHorizontalInset;
        this.extraBottomInset = extraBottomInset;
        notifyDataSetChanged();
    }

    public int getFirstVisibleInsertIndex(@NonNull ListCategory category) {
        ColumnPageViewHolder holder = pageHolders[category.getPanelIndex()];
        if (holder == null) {
            return 0;
        }
        return holder.getFirstVisibleInsertIndex();
    }

    public void beginEditingItem(@NonNull ListCategory category, @NonNull String itemId, int insertIndex) {
        ColumnPageViewHolder holder = pageHolders[category.getPanelIndex()];
        if (holder != null) {
            holder.beginEditingItem(itemId, insertIndex);
        }
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
        pageHolders[position] = holder;
        holder.bind(CATEGORIES[position]);
    }

    @Override
    public void onViewRecycled(@NonNull ColumnPageViewHolder holder) {
        holder.unbind();
        int index = holder.getBindingAdapterPosition();
        if (index >= 0 && index < pageHolders.length) {
            pageHolders[index] = null;
        }
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return CATEGORIES.length;
    }

    class ColumnPageViewHolder extends RecyclerView.ViewHolder {

        private final RecyclerView recyclerView;
        private final ListItemAdapter listAdapter;
        private ListItemSwipeController swipeController;
        private Observer<List<ListItem>> itemsObserver;
        private ListCategory boundCategory;

        ColumnPageViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.column_recycler_view);
            listAdapter = new ListItemAdapter();
            listAdapter.setEditCallback((id, title, description) ->
                    viewModel.saveItem(id, title, description));
            recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            recyclerView.setAdapter(listAdapter);
            recyclerView.setClipChildren(false);
            recyclerView.setClipToPadding(false);
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

            View pageParent = (View) itemView.getParent();
            if (pageParent instanceof ViewGroup) {
                ((ViewGroup) pageParent).setClipChildren(false);
            }

            swipeController = new ListItemSwipeController(
                    recyclerView,
                    () -> boundCategory,
                    viewModel::getEditingItemIdValue,
                    listAdapter::getItemIdAt,
                    new ListItemSwipeController.Callback() {
                        @Override
                        public void onSwipeLeft(int position) {
                            viewModel.swipeLeft(boundCategory, position);
                        }

                        @Override
                        public void onSwipeRight(int position) {
                            viewModel.swipeRight(boundCategory, position);
                        }

                        @Override
                        public void onItemClick(int position) {
                            String itemId = listAdapter.getItemIdAt(position);
                            if (itemId == null || itemId.equals(viewModel.getEditingItemIdValue())) {
                                return;
                            }
                            listAdapter.finishEditing(recyclerView);
                            viewModel.startEditingItem(itemId);
                        }
                    }
            );
            listAdapter.setSwipeController(swipeController);

            itemsObserver = items -> {
                listAdapter.setEditingItemId(viewModel.getEditingItemIdValue());
                listAdapter.submitList(items);

                String editingId = viewModel.getEditingItemIdValue();
                if (editingId != null && category == viewModel.getCurrentCategoryValue()) {
                    int position = indexOfItem(items, editingId);
                    if (position >= 0) {
                        listAdapter.requestFocusForItem(recyclerView, editingId);
                    }
                }
            };
            viewModel.observeItems(category).observe(lifecycleOwner, itemsObserver);
            listAdapter.setEditingItemId(viewModel.getEditingItemIdValue());
        }

        void applyEditingItemId(@Nullable String editingItemId) {
            if (boundCategory == null) {
                return;
            }
            listAdapter.setEditingItemId(editingItemId);
            if (editingItemId != null && boundCategory == viewModel.getCurrentCategoryValue()) {
                listAdapter.requestFocusForItem(recyclerView, editingItemId);
            }
        }

        void beginEditingItem(@NonNull String itemId, int insertIndex) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(insertIndex, 0);
            }
            listAdapter.setEditingItemId(itemId);
            listAdapter.requestFocusForItem(recyclerView, itemId);
        }

        int getFirstVisibleInsertIndex() {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager == null) {
                return 0;
            }
            int firstVisible = layoutManager.findFirstVisibleItemPosition();
            return firstVisible == RecyclerView.NO_POSITION ? 0 : firstVisible;
        }

        void unbind() {
            if (itemsObserver != null && boundCategory != null) {
                viewModel.observeItems(boundCategory).removeObserver(itemsObserver);
            }
            listAdapter.setSwipeController(null);
            swipeController = null;
            itemsObserver = null;
            boundCategory = null;
        }

        private int indexOfItem(@Nullable List<ListItem> items, @NonNull String itemId) {
            if (items == null) {
                return -1;
            }
            for (int i = 0; i < items.size(); i++) {
                if (itemId.equals(items.get(i).getId())) {
                    return i;
                }
            }
            return -1;
        }
    }
}
