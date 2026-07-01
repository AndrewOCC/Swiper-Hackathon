package com.listmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.listmanager.adapter.ColumnPagerAdapter;
import com.listmanager.model.ListCategory;
import com.listmanager.ui.MainViewModel;
import com.listmanager.ui.MainViewModelFactory;
import com.listmanager.ui.TabBarAnimator;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private ColumnPagerAdapter columnPagerAdapter;

    private AppBarLayout appBarLayout;
    private View tabBarContainer;
    private TextView tabLowPriority;
    private TextView tabInbox;
    private TextView tabHighPriority;
    private View tabIndicator;
    private ImageButton settingsButton;
    private View bottomEdgeSwipeZone;
    private ViewPager2 columnPager;

    private TabBarAnimator tabBarAnimator;

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
        tabBarContainer = findViewById(R.id.tab_bar_container);
        tabLowPriority = findViewById(R.id.tab_low_priority);
        tabInbox = findViewById(R.id.tab_inbox);
        tabHighPriority = findViewById(R.id.tab_high_priority);
        tabIndicator = findViewById(R.id.tab_indicator);
        settingsButton = findViewById(R.id.settings_button);
        bottomEdgeSwipeZone = findViewById(R.id.bottom_edge_swipe_zone);
        columnPager = findViewById(R.id.column_pager);

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
        int swipeThreshold = getResources().getDimensionPixelSize(R.dimen.panel_swipe_threshold);

        PanelSwipeHandler panelSwipeHandler = new PanelSwipeHandler(this, new PanelSwipeHandler.Callback() {
            @Override
            public void onSwipeLeft() {
                scrollToPanel(columnPager.getCurrentItem() - 1);
            }

            @Override
            public void onSwipeRight() {
                scrollToPanel(columnPager.getCurrentItem() + 1);
            }
        }, swipeThreshold);

        columnPagerAdapter = new ColumnPagerAdapter(
                this,
                viewModel,
                listHorizontalPadding,
                listBottomPadding + bottomEdgeHeight,
                panelSwipeHandler.asRecyclerBlankAreaListener()
        );
        columnPager.setAdapter(columnPagerAdapter);
        columnPager.setOffscreenPageLimit(2);
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
        int peekPx = getResources().getDimensionPixelSize(R.dimen.column_page_peek);
        int gapPx = getResources().getDimensionPixelSize(R.dimen.column_page_gap);

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
            pagerRecycler.setPadding(peekPx, 0, peekPx, 0);
            pagerRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);

            CompositePageTransformer transformer = new CompositePageTransformer();
            transformer.addTransformer(new MarginPageTransformer(gapPx));
            transformer.addTransformer((page, position) -> {
                float absPos = Math.min(1f, Math.abs(position));
                page.setAlpha(0.7f + (1f - absPos) * 0.3f);
            });
            columnPager.setPageTransformer(transformer);
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
    }

    private void setupTabBar() {
        tabLowPriority.setOnClickListener(view -> scrollToPanel(0));
        tabInbox.setOnClickListener(view -> scrollToPanel(1));
        tabHighPriority.setOnClickListener(view -> scrollToPanel(2));
        settingsButton.setOnClickListener(view ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void setupPanelSwiping() {
        int swipeThreshold = getResources().getDimensionPixelSize(R.dimen.panel_swipe_threshold);
        PanelSwipeHandler panelSwipeHandler = new PanelSwipeHandler(this, new PanelSwipeHandler.Callback() {
            @Override
            public void onSwipeLeft() {
                scrollToPanel(columnPager.getCurrentItem() - 1);
            }

            @Override
            public void onSwipeRight() {
                scrollToPanel(columnPager.getCurrentItem() + 1);
            }
        }, swipeThreshold);

        View.OnTouchListener swipeListener = panelSwipeHandler.asTouchListener();
        appBarLayout.setOnTouchListener(swipeListener);
        tabBarContainer.setOnTouchListener(swipeListener);
        bottomEdgeSwipeZone.setOnTouchListener(swipeListener);
        columnPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                boolean idle = state == ViewPager2.SCROLL_STATE_IDLE;
                appBarLayout.setOnTouchListener(idle ? swipeListener : null);
                tabBarContainer.setOnTouchListener(idle ? swipeListener : null);
                bottomEdgeSwipeZone.setOnTouchListener(idle ? swipeListener : null);
            }
        });
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
