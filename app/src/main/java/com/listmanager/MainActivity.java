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
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private FloatingActionButton fabAddItem;

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
        observeViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        columnPager.setUserInputEnabled(true);
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
                listBottomPadding + bottomEdgeHeight,
                panelDragListener.asRecyclerBlankAreaListener()
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
        int leftPeekPx = getResources().getDimensionPixelSize(R.dimen.column_page_peek);
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
            // Left-only peek avoids a full-height strip on the right where the FAB sits.
            pagerRecycler.setPadding(leftPeekPx, 0, 0, 0);
            pagerRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
            disableClippingOnPagerPages(pagerRecycler);

            CompositePageTransformer transformer = new CompositePageTransformer();
            transformer.addTransformer(new MarginPageTransformer(gapPx));
            transformer.addTransformer((page, position) -> {
                float absPos = Math.min(1f, Math.abs(position));
                page.setAlpha(0.7f + (1f - absPos) * 0.3f);
            });
            columnPager.setPageTransformer(transformer);
        });
    }

    private void disableClippingOnPagerPages(@NonNull RecyclerView pagerRecycler) {
        for (int i = 0; i < pagerRecycler.getChildCount(); i++) {
            View page = pagerRecycler.getChildAt(i);
            if (page instanceof ViewGroup) {
                ((ViewGroup) page).setClipChildren(false);
                ((ViewGroup) page).setClipToPadding(false);
            }
        }
        pagerRecycler.addOnLayoutChangeListener((view, left, top, right, bottom,
                                                 oldLeft, oldTop, oldRight, oldBottom) ->
                disableClippingOnPagerPages(pagerRecycler));
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
        columnPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                boolean idle = state == ViewPager2.SCROLL_STATE_IDLE;
                tabBar.setOnTouchListener(idle ? dragListener : null);
                bottomEdgeSwipeZone.setOnTouchListener(idle ? dragListener : null);
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
