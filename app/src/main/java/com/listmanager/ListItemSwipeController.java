package com.listmanager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.listmanager.model.ItemSwipeAction;
import com.listmanager.model.ListCategory;

import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Wraps ItemTouchHelper so card swipes stay on the standard AndroidX dispatch path
 * (taps, vertical scroll and panel swipes are unaffected). Draws a coloured
 * background on the RecyclerView canvas behind the sliding card.
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
    private final int moveBackgroundColor;
    private final int deleteBackgroundColor;
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

        Context ctx = recyclerView.getContext();
        moveBackgroundColor = ContextCompat.getColor(ctx, R.color.swipe_move_background);
        deleteBackgroundColor = ContextCompat.getColor(ctx, R.color.swipe_delete_background);

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
        return editingId.equals(itemIdAtPosition.apply(position));
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

        private final Paint bgPaint = new Paint();

        SwipeCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView rv,
                                    @NonNull RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(0, getAllowedSwipeDirections(viewHolder.getBindingAdapterPosition()));
        }

        @Override
        public boolean onMove(@NonNull RecyclerView rv,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                // Position lost; ask the adapter to rebind this holder so it snaps back.
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

        @Override
        public void onChildDraw(@NonNull Canvas c,
                                @NonNull RecyclerView rv,
                                @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY,
                                int actionState,
                                boolean isCurrentlyActive) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX != 0) {
                drawBackground(c, viewHolder.itemView, dX);
            }
            super.onChildDraw(c, rv, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        private void drawBackground(@NonNull Canvas c,
                                    @NonNull android.view.View itemView,
                                    float dX) {
            ListCategory cat = categorySupplier.get();
            if (cat == null) cat = ListCategory.INBOX;

            if (dX < 0) {
                // Swiping left — revealed area is on the RIGHT of the card's original position
                ItemSwipeAction action = ItemSwipeAction.forLeftSwipe(cat, moveBackgroundColor);
                if (action.type == ItemSwipeAction.Type.NONE) return;
                bgPaint.setColor(action.type == ItemSwipeAction.Type.DELETE
                        ? deleteBackgroundColor : moveBackgroundColor);
                c.drawRect(
                        itemView.getRight() + dX,
                        itemView.getTop(),
                        itemView.getRight(),
                        itemView.getBottom(),
                        bgPaint);
            } else {
                // Swiping right — revealed area is on the LEFT
                ItemSwipeAction action = ItemSwipeAction.forRightSwipe(cat, moveBackgroundColor);
                if (action.type == ItemSwipeAction.Type.NONE) return;
                bgPaint.setColor(moveBackgroundColor);
                c.drawRect(
                        itemView.getLeft(),
                        itemView.getTop(),
                        itemView.getLeft() + dX,
                        itemView.getBottom(),
                        bgPaint);
            }
        }
    }
}
