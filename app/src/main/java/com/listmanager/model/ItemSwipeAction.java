package com.listmanager.model;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.listmanager.R;

public final class ItemSwipeAction {

    public enum Type {
        MOVE,
        DELETE,
        NONE
    }

    public final Type type;
    @Nullable
    public final ListCategory targetCategory;
    @StringRes
    public final int labelRes;
    @ColorInt
    public final int backgroundColor;
    public final boolean showIcon;

    private ItemSwipeAction(Type type,
                            @Nullable ListCategory targetCategory,
                            @StringRes int labelRes,
                            @ColorInt int backgroundColor,
                            boolean showIcon) {
        this.type = type;
        this.targetCategory = targetCategory;
        this.labelRes = labelRes;
        this.backgroundColor = backgroundColor;
        this.showIcon = showIcon;
    }

    public static ItemSwipeAction none() {
        return new ItemSwipeAction(Type.NONE, null, 0, 0, false);
    }

    public static ItemSwipeAction move(@NonNull ListCategory target,
                                       @StringRes int labelRes,
                                       @ColorInt int backgroundColor) {
        return new ItemSwipeAction(Type.MOVE, target, labelRes, backgroundColor, false);
    }

    public static ItemSwipeAction delete(@ColorInt int backgroundColor) {
        return new ItemSwipeAction(Type.DELETE, null, R.string.action_delete, backgroundColor, true);
    }

    public static ItemSwipeAction forLeftSwipe(@NonNull ListCategory category,
                                               @ColorInt int moveBackgroundColor) {
        switch (category) {
            case INBOX:
                return move(ListCategory.ARCHIVED, R.string.category_low_priority, moveBackgroundColor);
            case ARCHIVED:
                return delete(0);
            case STARRED:
                return move(ListCategory.INBOX, R.string.category_inbox, moveBackgroundColor);
            default:
                return none();
        }
    }

    public static ItemSwipeAction forRightSwipe(@NonNull ListCategory category,
                                                @ColorInt int moveBackgroundColor) {
        switch (category) {
            case INBOX:
                return move(ListCategory.STARRED, R.string.category_high_priority, moveBackgroundColor);
            case ARCHIVED:
                return move(ListCategory.INBOX, R.string.category_inbox, moveBackgroundColor);
            case STARRED:
            default:
                return none();
        }
    }
}
