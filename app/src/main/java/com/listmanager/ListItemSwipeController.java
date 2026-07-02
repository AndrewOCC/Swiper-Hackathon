package com.listmanager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.listmanager.model.ItemSwipeAction;
import com.listmanager.model.ListCategory;

import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Uses RecyclerView's supported swipe helper instead of hand-rolled touch
 * interception. This keeps taps, vertical scroll, and ViewPager gestures in their
 * normal dispatch paths while still allowing horizontal card actions.
 */
public class ListItemSwipeController {

    public interface Callback {
        void onSwipeLeft(int position);

        void onSwipeRight(int position);
    }

    private final RecyclerView recyclerView;
    private final Callback callback;
    private final Supplier<ListCategory> categorySupplier;
    private final Supplier<String> editingItemIdSupplier;
    private final IntFunction<String> itemIdAtPosition;
    private final ItemTouchHelper itemTouchHelper;

    public ListItemSwipeController(@NonNull RecyclerView recyclerView,
                                   @NonNull Supplier<ListCategory> categorySupplier,
                                   @NonNull Supplier<String> editingItemIdSupplier,
                                   @NonNull IntFunction<String> itemIdAtPosition,
                                   @NonNull Callback callback) {
        this.recyclerView = recyclerView;
        this.categorySupplier = categorySupplier;
        this.editingItemIdSupplier = editingItemIdSupplier;
        this.itemIdAtPosition = itemIdAtPosition;
        this.callback = callback;
        itemTouchHelper = new ItemTouchHelper(new SwipeCallback());
    }

    public void attach() {
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void detach() {
        itemTouchHelper.attachToRecyclerView(null);
    }

    private boolean isEditingPosition(int position) {
        if (position == RecyclerView.NO_POSITION) {
            return false;
        }
        String editingId = editingItemIdSupplier.get();
        if (editingId == null) {
            return false;
        }
        String itemId = itemIdAtPosition.apply(position);
        return editingId.equals(itemId);
    }

    private int getAllowedSwipeDirections(int position) {
        if (isEditingPosition(position)) {
            return 0;
        }
        ListCategory category = categorySupplier.get();
        if (category == null) {
            category = ListCategory.INBOX;
        }
        int directions = 0;
        if (ItemSwipeAction.forLeftSwipe(category, 0).type != ItemSwipeAction.Type.NONE) {
            directions |= ItemTouchHelper.LEFT;
        }
        if (ItemSwipeAction.forRightSwipe(category, 0).type != ItemSwipeAction.Type.NONE) {
            directions |= ItemTouchHelper.RIGHT;
        }
        return directions;
    }

    private class SwipeCallback extends ItemTouchHelper.SimpleCallback {
        SwipeCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder) {
            int position = viewHolder.getBindingAdapterPosition();
            return makeMovementFlags(0, getAllowedSwipeDirections(position));
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                RecyclerView.Adapter<?> adapter = viewHolder.getBindingAdapter();
                if (adapter != null) {
                    adapter.notifyItemChanged(viewHolder.getAbsoluteAdapterPosition());
                }
                return;
            }
            if (direction == ItemTouchHelper.RIGHT) {
                callback.onSwipeRight(position);
            } else {
                callback.onSwipeLeft(position);
            }
        }

        @Override
        public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
            return 0.33f;
        }
    }
}
