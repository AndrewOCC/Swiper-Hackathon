package com.listmanager.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.listmanager.R;
import com.listmanager.model.ListItem;

import java.util.ArrayList;
import java.util.List;

public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ItemViewHolder> {

    public interface EditCallback {
        void onSaveItem(@NonNull String id, @NonNull String title, @NonNull String description);
    }

    private final List<ListItem> items = new ArrayList<>();
    private EditCallback editCallback;
    @Nullable
    private String editingItemId;
    @Nullable
    private String pendingFocusItemId;

    public void setEditCallback(@Nullable EditCallback editCallback) {
        this.editCallback = editCallback;
    }

    public void setEditingItemId(@Nullable String editingItemId) {
        this.editingItemId = editingItemId;
        notifyDataSetChanged();
    }

    public void finishEditing(@NonNull RecyclerView recyclerView) {
        if (editingItemId == null) {
            return;
        }
        int position = findPositionById(editingItemId);
        if (position < 0) {
            return;
        }
        ItemViewHolder holder = (ItemViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        if (holder != null) {
            holder.saveAndClose(editCallback);
        }
    }

    public void requestFocusForItem(@NonNull RecyclerView recyclerView, @NonNull String itemId) {
        pendingFocusItemId = itemId;
        int position = findPositionById(itemId);
        if (position < 0) {
            return;
        }
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            layoutManager.scrollToPositionWithOffset(position, 0);
        }
        recyclerView.post(() -> focusPendingItem(recyclerView));
    }

    public void submitList(List<ListItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @Nullable
    public String getItemIdAt(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }
        return items.get(position).getId();
    }

    public int findPositionById(@NonNull String itemId) {
        for (int i = 0; i < items.size(); i++) {
            if (itemId.equals(items.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    @Nullable
    public String getEditingItemId() {
        return editingItemId;
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
        boolean editing = item.getId().equals(editingItemId);
        holder.bind(item, editing, editCallback);
        if (editing && item.getId().equals(pendingFocusItemId)) {
            holder.focusTitle();
            pendingFocusItemId = null;
        }
    }

    private void focusPendingItem(@NonNull RecyclerView recyclerView) {
        if (pendingFocusItemId == null) {
            return;
        }
        int position = findPositionById(pendingFocusItemId);
        if (position < 0) {
            return;
        }
        ItemViewHolder holder = (ItemViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        if (holder != null) {
            holder.focusTitle();
            pendingFocusItemId = null;
        }
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

        private final TextView title;
        private final TextView description;
        private final EditText titleEdit;
        private final EditText descriptionEdit;
        private ListItem boundItem;
        private boolean saved;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            description = itemView.findViewById(R.id.item_description);
            titleEdit = itemView.findViewById(R.id.item_title_edit);
            descriptionEdit = itemView.findViewById(R.id.item_description_edit);
        }

        void bind(@NonNull ListItem item,
                  boolean editing,
                  @Nullable EditCallback editCallback) {
            boundItem = item;
            saved = false;

            if (editing) {
                title.setVisibility(View.GONE);
                description.setVisibility(View.GONE);
                titleEdit.setVisibility(View.VISIBLE);
                descriptionEdit.setVisibility(View.VISIBLE);

                titleEdit.setText(item.getTitle());
                descriptionEdit.setText(item.getDescription());

                titleEdit.setOnEditorActionListener((textView, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        descriptionEdit.requestFocus();
                        return true;
                    }
                    return false;
                });

                descriptionEdit.setOnEditorActionListener((textView, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        saveAndClose(editCallback);
                        return true;
                    }
                    return false;
                });

                View.OnFocusChangeListener focusListener = (view, hasFocus) -> {
                    if (!hasFocus && !titleEdit.hasFocus() && !descriptionEdit.hasFocus()) {
                        saveAndClose(editCallback);
                    }
                };
                titleEdit.setOnFocusChangeListener(focusListener);
                descriptionEdit.setOnFocusChangeListener(focusListener);
            } else {
                titleEdit.setOnFocusChangeListener(null);
                descriptionEdit.setOnFocusChangeListener(null);
                titleEdit.setVisibility(View.GONE);
                descriptionEdit.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                description.setVisibility(View.VISIBLE);

                title.setText(item.getTitle());
                description.setText(item.getDescription());
                description.setVisibility(TextUtils.isEmpty(item.getDescription()) ? View.GONE : View.VISIBLE);
            }
        }

        void focusTitle() {
            titleEdit.requestFocus();
            titleEdit.setSelection(titleEdit.getText().length());
            InputMethodManager imm = (InputMethodManager) itemView.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(titleEdit, InputMethodManager.SHOW_IMPLICIT);
            }
        }

        void saveAndClose(@Nullable EditCallback editCallback) {
            if (saved || boundItem == null || editCallback == null) {
                return;
            }
            saved = true;
            hideKeyboard();
            editCallback.onSaveItem(
                    boundItem.getId(),
                    titleEdit.getText().toString(),
                    descriptionEdit.getText().toString()
            );
        }

        private void hideKeyboard() {
            InputMethodManager imm = (InputMethodManager) itemView.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(titleEdit.getWindowToken(), 0);
            }
        }
    }
}
