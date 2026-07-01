package com.listmanager;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.listmanager.adapter.ListItemAdapter;
import com.listmanager.model.ListCategory;
import com.listmanager.ui.MainViewModel;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private MainViewModel viewModel;
    private ListItemAdapter adapter;

    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private ArrayAdapter<String> drawerAdapter;
    private ActionBarDrawerToggle drawerToggle;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private SwipeableRecyclerViewTouchListener swipeTouchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activityTitle = getString(R.string.app_name);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        drawerList = findViewById(R.id.navList);
        drawerLayout = findViewById(R.id.drawer_layout);
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.container);
        swipeRefreshLayout.setOnRefreshListener(this);

        setupDrawer();
        setupRecyclerView();
        observeViewModel();
    }

    private void setupDrawer() {
        String[] drawerItems = {
                getString(R.string.category_inbox),
                getString(R.string.category_archived),
                getString(R.string.category_starred)
        };
        drawerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drawerItems);
        drawerList.setAdapter(drawerAdapter);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewModel.selectCategory(categoryForDrawerPosition(position));
                drawerLayout.closeDrawers();
            }
        });

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                super.onDrawerOpened(drawerView);
                setActionBarTitle(R.string.drawer_title);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                super.onDrawerClosed(drawerView);
                updateActionBarTitleForCategory(viewModel.getCurrentCategory().getValue());
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    private void setupRecyclerView() {
        adapter = new ListItemAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeTouchListener = new SwipeableRecyclerViewTouchListener(recyclerView,
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
                updateActionBarTitleForCategory(category);
            }
        });
    }

    private ListCategory categoryForDrawerPosition(int position) {
        switch (position) {
            case 1:
                return ListCategory.ARCHIVED;
            case 2:
                return ListCategory.STARRED;
            case 0:
            default:
                return ListCategory.INBOX;
        }
    }

    private void updateActionBarTitleForCategory(ListCategory category) {
        if (category == null) {
            setActionBarTitle(R.string.app_name);
            return;
        }

        switch (category) {
            case ARCHIVED:
                setActionBarTitle(R.string.category_archived);
                break;
            case STARRED:
                setActionBarTitle(R.string.category_starred);
                break;
            case INBOX:
            default:
                setActionBarTitle(R.string.category_inbox);
                break;
        }
    }

    private void setActionBarTitle(@StringRes int titleRes) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titleRes);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
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

        int itemId = item.getItemId();
        if (itemId == R.id.menu_refresh) {
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
