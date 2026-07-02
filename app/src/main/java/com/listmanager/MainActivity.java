package com.listmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.listmanager.adapter.ColumnPagerAdapter;
import com.listmanager.model.ListCategory;
import com.listmanager.model.ListItem;
import com.listmanager.ui.MainViewModel;
import com.listmanager.ui.MainViewModelFactory;
import com.listmanager.ui.TabBarAnimator;

public class MainActivity extends AppCompatActivity implements PanelDragListener.Host {

    private MainViewModel viewModel;
    private ColumnPagerAdapter columnPagerAdapter;
    private PanelDragListener panelDragListener;

    private AppBarLayout appBarLayout;
    private TextView tabLowPriority;
    private TextView tabInbox;
    private TextView tabHighPriority;
    private View tabIndicator;
    private ImageButton settingsButton;
    private View bottomEdgeSwipeZone;
    private ViewPager2 columnPager;
    private ImageButton fabAddItem;
    private TextView debugVersionBadge;

    private TabBarAnimator tabBarAnimator;

    @NonNull
    @Override
    public ViewPager2 getViewPager() {
        return columnPager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(
                this,
                new MainViewModelFactory(getApplication())
        ).get(MainViewModel.class);

        appBarLayout = findViewById(R.id.app_bar);
        tabLowPriority = findViewById(R.id.tab_low_priority);
        tabInbox = findViewById(R.id.tab_inbox);
        tabHighPriority = findViewById(R.id.tab_high_priority);
        tabIndicator = findViewById(R.id.tab_indicator);
        settingsButton = findViewById(R.id.settings_button);
        bottomEdgeSwipeZone = findViewById(R.id.bottom_edge_swipe_zone);
        columnPager = findViewById(R.id.column_pager);
        fabAddItem = findViewById(R.id.fab_add_item);
        debugVersionBadge = findViewById(R.id.debug_version_badge);

        panelDragListener = new PanelDragListener(this);

        int selectedTabColor = resolveThemeColor(com.google.android.material.R.attr.colorPrimary);
        int unselectedTabColor = resolveThemeColor(com.google.android.material.R.attr.colorOnSurface);

        tabBarAnimator = new TabBarAnimator(
                tabLowPriority,
                tabInbox,
                tabHighPriority,
                tabIndicator,
                selectedTabColor,
                unselectedTabColor
        );

        setupColumnPager();
        setupColumnPagerEffects();
        setupEdgeToEdge();
        setupTabBar();
        setupPanelSwiping();
        setupFab();
        setupDebugVersionBadge();
        observeViewModel();
    }

    private int resolveThemeColor(int attr) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    private void setupColumnPager() {
        int listHorizontalPadding = getResources().getDimensionPixelSize(R.dimen.list_horizontal_padding);
        int listBottomPadding = getResources().getDimensionPixelSize(R.dimen.list_bottom_padding);
        int bottomEdgeHeight = getResources().getDimensionPixelSize(R.dimen.bottom_edge_swipe_height);

        columnPagerAdapter = new ColumnPagerAdapter(
                this,
                viewModel,
                listHorizontalPadding,
                listBottomPadding + bottomEdgeHeight
        );
        columnPager.setAdapter(columnPagerAdapter);
        columnPager.setOffscreenPageLimit(2);
        // Disable ViewPager2 built-in touch input so it cannot steal card swipes.
        // Panel navigation is via PanelDragListener on the tab bar and bottom edge zone.
        columnPager.setUserInputEnabled(false);
        columnPager.setCurrentItem(ListCategory.INBOX.getPanelIndex(), false);

        columnPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tabBarAnimator.onPageScrolled(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                viewModel.selectCategory(ListCategory.fromPanelIndex(position));
            }
        });
    }

    private void setupColumnPagerEffects() {
        ViewGroup contentFrame = findViewById(R.id.content_frame);
        contentFrame.setClipChildren(false);
        columnPager.setClipChildren(false);
        columnPager.setClipToPadding(false);

        columnPager.post(() -> {
            RecyclerView pagerRecycler = (RecyclerView) columnPager.getChildAt(0);
            if (pagerRecycler == null) {
                return;
            }
            pagerRecycler.setClipToPadding(false);
            pagerRecycler.setClipChildren(false);
            pagerRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
        });
    }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(view.getPaddingLeft(), insets.top, view.getPaddingRight(), view.getPaddingBottom());
            return windowInsets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(columnPager, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(insets.left, 0, insets.right, 0);
            columnPagerAdapter.setPagePadding(insets.left, insets.bottom);
            return windowInsets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(bottomEdgeSwipeZone, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), insets.bottom);
            return windowInsets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(fabAddItem, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int baseMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.bottomMargin = baseMargin + insets.bottom;
            params.rightMargin = baseMargin + insets.right;
            view.setLayoutParams(params);
            return windowInsets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(debugVersionBadge, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int baseMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.bottomMargin = baseMargin + insets.bottom;
            params.leftMargin = baseMargin + insets.left;
            view.setLayoutParams(params);
            return windowInsets;
        });
    }

    private void setupDebugVersionBadge() {
        if (BuildConfig.DEBUG) {
            debugVersionBadge.setVisibility(View.VISIBLE);
            debugVersionBadge.setText(getString(R.string.debug_version_format, BuildConfig.VERSION_NAME));
        } else {
            debugVersionBadge.setVisibility(View.GONE);
        }
    }

    private void setupFab() {
        fabAddItem.setOnClickListener(view -> {
            ListCategory category = viewModel.getCurrentCategoryValue();
            int insertIndex = columnPagerAdapter.getFirstVisibleInsertIndex(category);
            ListItem item = viewModel.createItemAt(category, insertIndex);
            columnPagerAdapter.beginEditingItem(category, item.getId(), insertIndex);
        });
    }

    private void setupTabBar() {
        tabLowPriority.setOnClickListener(view -> scrollToPanel(0));
        tabInbox.setOnClickListener(view -> scrollToPanel(1));
        tabHighPriority.setOnClickListener(view -> scrollToPanel(2));
        settingsButton.setOnClickListener(view ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void setupPanelSwiping() {
        View.OnTouchListener dragListener = panelDragListener.asTouchListener();
        View tabBar = findViewById(R.id.tab_bar);
        tabBar.setOnTouchListener(dragListener);
        bottomEdgeSwipeZone.setOnTouchListener(dragListener);
        // Note: we intentionally do NOT remove these listeners on scroll state changes.
        // Removing mid-gesture (when state changes to DRAGGING) would cut the fake drag
        // short. PanelDragListener's own `tracking` flag prevents double-entry.
    }

    private void scrollToPanel(int index) {
        if (index < 0 || index > 2) {
            return;
        }
        columnPager.setCurrentItem(index, true);
    }

    private void observeViewModel() {
        viewModel.getCurrentCategory().observe(this, category -> {
            if (category == null) {
                return;
            }
            int targetIndex = category.getPanelIndex();
            if (columnPager.getCurrentItem() != targetIndex) {
                columnPager.setCurrentItem(targetIndex, true);
            }
        });
    }
}
