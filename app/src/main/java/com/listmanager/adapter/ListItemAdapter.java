package com.listmanager.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

    public interface SaveCallback {
        void onSaveItem(@NonNull String id, @NonNull String title, @NonNull String description);
    }

    public interface ItemActionListener {
        void onEditItem(int position);
        void onDeleteItem(@NonNull String itemId);
    }

    private final List<ListItem> items = new ArrayList<>();
    @Nullable
    private SaveCallback saveCallback;
    @Nullable
    private ItemActionListener actionListener;
    @Nullable
    private String editingItemId;
    @Nullable
    private String pendingFocusItemId;

    public void setSaveCallback(@Nullable SaveCallback saveCallback) {
        this.saveCallback = saveCallback;
    }

    public void setActionListener(@Nullable ItemActionListener actionListener) {
        this.actionListener = actionListener;
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
            holder.saveAndClose(saveCallback);
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
        holder.bind(item, editing, saveCallback, actionListener);
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
        itemView.animate().cancel();
        itemView.setTranslationX(0f);
        itemView.setTranslationZ(0f);

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
        private final LinearLayout actionsView;
        private final LinearLayout actionsEdit;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;
        private final View btnSave;
        private ListItem boundItem;
        private boolean saved;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            description = itemView.findViewById(R.id.item_description);
            titleEdit = itemView.findViewById(R.id.item_title_edit);
            descriptionEdit = itemView.findViewById(R.id.item_description_edit);
            actionsView = itemView.findViewById(R.id.card_actions_view);
            actionsEdit = itemView.findViewById(R.id.card_actions_edit);
            btnEdit = itemView.findViewById(R.id.btn_edit_item);
            btnDelete = itemView.findViewById(R.id.btn_delete_item);
            btnSave = itemView.findViewById(R.id.btn_save_item);
        }

        void bind(@NonNull ListItem item,
                  boolean editing,
                  @Nullable SaveCallback saveCallback,
                  @Nullable ItemActionListener actionListener) {
            boundItem = item;
            saved = false;

            if (editing) {
                title.setVisibility(View.GONE);
                description.setVisibility(View.GONE);
                titleEdit.setVisibility(View.VISIBLE);
                descriptionEdit.setVisibility(View.VISIBLE);
                actionsView.setVisibility(View.GONE);
                actionsEdit.setVisibility(View.VISIBLE);

                titleEdit.setText(item.getTitle());
                descriptionEdit.setText(item.getDescription());

                titleEdit.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        descriptionEdit.requestFocus();
                        return true;
                    }
                    return false;
                });

                descriptionEdit.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        saveAndClose(saveCallback);
                        return true;
                    }
                    return false;
                });

                View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
                    if (!hasFocus && !titleEdit.hasFocus() && !descriptionEdit.hasFocus()) {
                        saveAndClose(saveCallback);
                    }
                };
                titleEdit.setOnFocusChangeListener(focusListener);
                descriptionEdit.setOnFocusChangeListener(focusListener);

                btnSave.setOnClickListener(v -> saveAndClose(saveCallback));

                itemView.setOnClickListener(null);
            } else {
                titleEdit.setOnFocusChangeListener(null);
                descriptionEdit.setOnFocusChangeListener(null);
                titleEdit.setVisibility(View.GONE);
                descriptionEdit.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                description.setVisibility(View.VISIBLE);
                actionsView.setVisibility(View.VISIBLE);
                actionsEdit.setVisibility(View.GONE);

                title.setText(item.getTitle());
                description.setText(item.getDescription());
                description.setVisibility(TextUtils.isEmpty(item.getDescription()) ? View.GONE : View.VISIBLE);

                btnEdit.setOnClickListener(v -> {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && actionListener != null) {
                        actionListener.onEditItem(pos);
                    }
                });

                btnDelete.setOnClickListener(v -> {
                    if (boundItem != null && actionListener != null) {
                        actionListener.onDeleteItem(boundItem.getId());
                    }
                });

                itemView.setOnClickListener(v -> {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && actionListener != null) {
                        actionListener.onEditItem(pos);
                    }
                });
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

        void saveAndClose(@Nullable SaveCallback saveCallback) {
            if (saved || boundItem == null || saveCallback == null) {
                return;
            }
            saved = true;
            hideKeyboard();
            saveCallback.onSaveItem(
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
