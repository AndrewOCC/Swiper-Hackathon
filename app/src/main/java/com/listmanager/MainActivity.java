package com.listmanager;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.listmanager.adapter.ListItemAdapter;
import com.listmanager.model.ListCategory;
import com.listmanager.ui.MainViewModel;
import com.listmanager.ui.MainViewModelFactory;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private MainViewModel viewModel;
    private ListItemAdapter adapter;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(
                this,
                new MainViewModelFactory(getApplication())
        ).get(MainViewModel.class);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.app_bar);
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.container);

        setSupportActionBar(toolbar);
        setupEdgeToEdge();
        setupDrawer();
        setupSwipeRefreshColors();
        setupRecyclerView();
        observeViewModel();
    }

    private void setupEdgeToEdge() {
        final int listHorizontalPadding = getResources().getDimensionPixelSize(R.dimen.list_horizontal_padding);
        final int listBottomPadding = getResources().getDimensionPixelSize(R.dimen.list_bottom_padding);

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
                    listBottomPadding + insets.bottom
            );
            return windowInsets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(navigationView, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(view.getPaddingLeft(), insets.top, view.getPaddingRight(), insets.bottom);
            return windowInsets;
        });
    }

    private void setupDrawer() {
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_inbox) {
                viewModel.selectCategory(ListCategory.INBOX);
            } else if (itemId == R.id.nav_archived) {
                viewModel.selectCategory(ListCategory.ARCHIVED);
            } else if (itemId == R.id.nav_starred) {
                viewModel.selectCategory(ListCategory.STARRED);
            } else {
                return false;
            }
            item.setChecked(true);
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void setupSwipeRefreshColors() {
        swipeRefreshLayout.setOnRefreshListener(this);
        TypedValue primaryValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, primaryValue, true);
        swipeRefreshLayout.setColorSchemeColors(primaryValue.data);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(
                getColor(R.color.md_theme_surface_container_low)
        );
    }

    private void setupRecyclerView() {
        adapter = new ListItemAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        SwipeableRecyclerViewTouchListener swipeTouchListener =
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
        recyclerView.addOnItemTouchListener(swipeTouchListener);
    }

    private void observeViewModel() {
        viewModel.getVisibleItems().observe(this, items -> adapter.submitList(items));

        viewModel.getCurrentCategory().observe(this, category -> {
            if (category != null) {
                updateToolbarTitle(category);
                updateCheckedNavigationItem(category);
            }
        });
    }

    private void updateCheckedNavigationItem(ListCategory category) {
        int itemId;
        switch (category) {
            case ARCHIVED:
                itemId = R.id.nav_archived;
                break;
            case STARRED:
                itemId = R.id.nav_starred;
                break;
            case INBOX:
            default:
                itemId = R.id.nav_inbox;
                break;
        }
        navigationView.setCheckedItem(itemId);
    }

    private void updateToolbarTitle(ListCategory category) {
        switch (category) {
            case ARCHIVED:
                setToolbarTitle(R.string.category_archived);
                break;
            case STARRED:
                setToolbarTitle(R.string.category_starred);
                break;
            case INBOX:
            default:
                setToolbarTitle(R.string.category_inbox);
                break;
        }
    }

    private void setToolbarTitle(@StringRes int titleRes) {
        toolbar.setTitle(titleRes);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.menu_refresh) {
            onRefresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        viewModel.refreshItems();
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(this, R.string.items_reset, Toast.LENGTH_SHORT).show();
    }
}
