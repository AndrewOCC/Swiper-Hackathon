package com.listmanager.model;

public enum ListCategory {
    ARCHIVED,
    INBOX,
    STARRED;

    public int getPanelIndex() {
        switch (this) {
            case ARCHIVED:
                return 0;
            case STARRED:
                return 2;
            case INBOX:
            default:
                return 1;
        }
    }

    public static ListCategory fromPanelIndex(int index) {
        switch (index) {
            case 0:
                return ARCHIVED;
            case 2:
                return STARRED;
            case 1:
            default:
                return INBOX;
        }
    }

    public ListCategory nextPanel() {
        return fromPanelIndex(Math.min(getPanelIndex() + 1, 2));
    }

    public ListCategory previousPanel() {
        return fromPanelIndex(Math.max(getPanelIndex() - 1, 0));
    }
}
