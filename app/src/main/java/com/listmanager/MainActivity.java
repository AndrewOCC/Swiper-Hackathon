package com.listmanager;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.listmanager.adapter.ListItemAdapter;
import com.listmanager.model.ListCategory;
import com.listmanager.ui.MainViewModel;
import com.listmanager.ui.MainViewModelFactory;

public class MainActivity extends AppCompatActivity {

    private static final float TAB_SELECTED_ALPHA = 1f;
    private static final float TAB_UNSELECTED_ALPHA = 0.4f;

    private MainViewModel viewModel;
    private ListItemAdapter adapter;

    private MaterialToolbar toolbar;
    private AppBarLayout appBarLayout;
    private View tabBar;
    private TextView tabLowPriority;
    private TextView tabInbox;
    private TextView tabHighPriority;
    private View bottomEdgeSwipeZone;
    private RecyclerView recyclerView;

    private int selectedTabColor;
    private int unselectedTabColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(
                this,
                new MainViewModelFactory(getApplication())
        ).get(MainViewModel.class);

        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.app_bar);
        tabBar = findViewById(R.id.tab_bar);
        tabLowPriority = findViewById(R.id.tab_low_priority);
        tabInbox = findViewById(R.id.tab_inbox);
        tabHighPriority = findViewById(R.id.tab_high_priority);
        bottomEdgeSwipeZone = findViewById(R.id.bottom_edge_swipe_zone);
        recyclerView = findViewById(R.id.recycler_view);

        selectedTabColor = resolveThemeColor(com.google.android.material.R.attr.colorPrimary);
        unselectedTabColor = resolveThemeColor(com.google.android.material.R.attr.colorOnSurface);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        setupEdgeToEdge();
        setupTabBar();
        setupPanelSwiping();
        setupRecyclerView();
        observeViewModel();
    }

    private int resolveThemeColor(int attr) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    private void setupEdgeToEdge() {
        final int listHorizontalPadding = getResources().getDimensionPixelSize(R.dimen.list_horizontal_padding);
        final int listBottomPadding = getResources().getDimensionPixelSize(R.dimen.list_bottom_padding);
        final int bottomEdgeHeight = getResources().getDimensionPixelSize(R.dimen.bottom_edge_swipe_height);

        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(view.getPaddingLeft(), insets.top, view.getPaddingRight(), view.getPaddingBottom());
            return windowInsets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(recyclerView, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    listHorizontalPadding + insets.left,
                    view.getPaddingTop(),
                    listHorizontalPadding + insets.right,
                    listBottomPadding + bottomEdgeHeight + insets.bottom
            );
            return windowInsets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(bottomEdgeSwipeZone, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), insets.bottom);
            return windowInsets;
        });
    }

    private void setupTabBar() {
        tabLowPriority.setOnClickListener(view -> viewModel.selectCategory(ListCategory.ARCHIVED));
        tabInbox.setOnClickListener(view -> viewModel.selectCategory(ListCategory.INBOX));
        tabHighPriority.setOnClickListener(view -> viewModel.selectCategory(ListCategory.STARRED));
    }

    private void setupPanelSwiping() {
        int swipeThreshold = getResources().getDimensionPixelSize(R.dimen.panel_swipe_threshold);
        PanelSwipeHandler.Callback callback = new PanelSwipeHandler.Callback() {
            @Override
            public void onSwipeLeft() {
                viewModel.selectNextPanel();
            }

            @Override
            public void onSwipeRight() {
                viewModel.selectPreviousPanel();
            }
        };

        PanelSwipeHandler panelSwipeHandler = new PanelSwipeHandler(this, callback, swipeThreshold);
        View.OnTouchListener swipeListener = panelSwipeHandler.asTouchListener();

        appBarLayout.setOnTouchListener(swipeListener);
        tabBar.setOnTouchListener(swipeListener);
        bottomEdgeSwipeZone.setOnTouchListener(swipeListener);
        recyclerView.addOnItemTouchListener(panelSwipeHandler.asRecyclerBlankAreaListener());
    }

    private void setupRecyclerView() {
        adapter = new ListItemAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        SwipeableRecyclerViewTouchListener itemSwipeListener =
                new SwipeableRecyclerViewTouchListener(recyclerView,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipe(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    viewModel.swipeLeft(position);
                                }
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    viewModel.swipeRight(position);
                                }
                            }
                        });
        recyclerView.addOnItemTouchListener(itemSwipeListener);
    }

    private void observeViewModel() {
        viewModel.getVisibleItems().observe(this, items -> adapter.submitList(items));
        viewModel.getCurrentCategory().observe(this, this::updateTabSelection);
    }

    private void updateTabSelection(ListCategory category) {
        if (category == null) {
            category = ListCategory.INBOX;
        }

        styleTab(tabLowPriority, category == ListCategory.ARCHIVED);
        styleTab(tabInbox, category == ListCategory.INBOX);
        styleTab(tabHighPriority, category == ListCategory.STARRED);
    }

    private void styleTab(TextView tab, boolean selected) {
        tab.setAlpha(selected ? TAB_SELECTED_ALPHA : TAB_UNSELECTED_ALPHA);
        tab.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        tab.setTextColor(selected ? selectedTabColor : unselectedTabColor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
