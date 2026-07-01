package com.freelancer.flapisample;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.freelancer.flapisample.adapter.RecommendedProjectsAdapter;
import com.freelancer.flapisample.data.SharedPreferencesAuthStorage;
import com.freelancer.flapisample.model.GafProject;
import com.freelancer.flapisample.model.retrofit.RetrofitResponse;
import com.freelancer.flapisample.retrofit.AuthInterceptor;
import com.freelancer.flapisample.retrofit.AuthStorage;
import com.freelancer.flapisample.retrofit.AuthTokenInterceptor;
import com.freelancer.flapisample.retrofit.FLApiConstants;
import com.freelancer.flapisample.retrofit.FLProjectsApi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final int ITEMS_PER_REQUEST = 20;
    private static final int ITEMS_LEFT_BEFORE_LOADING_MORE = 10;

    private AuthStorage authStorage;
    private FLProjectsApi projectsApi;
    private LinearLayoutManager layoutManager;

    private RecommendedProjectsAdapter adapter;
    private RecommendedProjectsAdapter dismissedAdapter;
    private RecommendedProjectsAdapter interestedAdapter;
    private RecommendedProjectsAdapter currentAdapter;

    private final List<GafProject> projects = new LinkedList<>();
    private final List<GafProject> dismissedProjects = new LinkedList<>();
    private final List<GafProject> interestedProjects = new LinkedList<>();
    private List<GafProject> currentList = new LinkedList<>();

    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    private int viewMode = 0;
    private int currentSize = 0;

    private int offset;
    private boolean isLoading;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivityTitle = getString(R.string.app_name);

        mDrawerList = findViewById(R.id.navList);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        addDrawerItems();
        setupDrawer();

        mSwipeRefreshLayout = findViewById(R.id.container);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        prepareApiService();
        prepareViews();
        refreshViewState();

        offset = 0;
        getRecommendedProjects();
    }

    private void addDrawerItems() {
        String[] drawerItems = {"Interested", "New", "Discarded"};
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drawerItems);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = mAdapter.getItem(position);
                if ("New".equals(item)) {
                    viewMode = 0;
                } else if ("Discarded".equals(item)) {
                    viewMode = 1;
                } else if ("Interested".equals(item)) {
                    viewMode = 2;
                }
                refreshViewState();
                mDrawerLayout.closeDrawers();
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                super.onDrawerOpened(drawerView);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Navigation");
                }
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                super.onDrawerClosed(drawerView);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(mActivityTitle);
                }
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int itemId = item.getItemId();
        if (itemId == R.id.menu_settings) {
            return true;
        } else if (itemId == R.id.menu_refresh) {
            onRefresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void prepareApiService() {
        authStorage = SharedPreferencesAuthStorage.instance(getApplication());

        // Don't forget to add in a valid freelancer account's username and password.
        authStorage.saveUsername("AC246");
        authStorage.savePassword("upstairsfr46");
        authStorage.saveUserId("10");
        authStorage.saveAuthToken("FLNS5T35XAX8EIKYBDYK7XMHWD3CX5J6");

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(authStorage))
                .addInterceptor(new AuthTokenInterceptor(authStorage))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FLApiConstants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        projectsApi = retrofit.create(FLProjectsApi.class);
    }

    private void prepareViews() {
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecommendedProjectsAdapter();
        dismissedAdapter = new RecommendedProjectsAdapter();
        interestedAdapter = new RecommendedProjectsAdapter();
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(scrollListener);
    }

    private void refreshViewState() {
        if (adapter.getItemCount() == 0 && viewMode == 0) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            if (viewMode == 0) {
                recyclerView.setAdapter(adapter);
                currentList = projects;
                currentAdapter = adapter;
            } else if (viewMode == 1) {
                recyclerView.setAdapter(dismissedAdapter);
                currentList = dismissedProjects;
                currentAdapter = dismissedAdapter;
            } else if (viewMode == 2) {
                recyclerView.setAdapter(interestedAdapter);
                currentList = interestedProjects;
                currentAdapter = interestedAdapter;
            }

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
                                        if (currentList.size() > 0) {
                                            if (viewMode == 0) {
                                                interestedProjects.add(currentList.get(position));
                                                interestedAdapter.add(currentList.get(position));
                                            } else if (viewMode == 1) {
                                                projects.add(currentList.get(position));
                                                adapter.add(currentList.get(position));
                                            } else if (viewMode == 2) {
                                                interestedProjects.add(currentList.get(position));
                                                interestedAdapter.add(currentList.get(position));
                                            }

                                            currentList.remove(position);
                                            currentAdapter.remove(position);
                                            for (GafProject project : projects) {
                                                Log.w("MyApp", project.getId() + project.getPreviewDescription());
                                            }
                                        }
                                    }
                                    currentAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                    for (int position : reverseSortedPositions) {
                                        if (viewMode == 0) {
                                            dismissedProjects.add(currentList.get(position));
                                            dismissedAdapter.add(currentList.get(position));
                                        } else if (viewMode == 2) {
                                            projects.add(currentList.get(position));
                                            adapter.add(currentList.get(position));
                                        }

                                        currentList.remove(position);
                                        currentAdapter.remove(position);
                                    }
                                    currentAdapter.notifyDataSetChanged();
                                }
                            });

            recyclerView.addOnItemTouchListener(swipeTouchListener);
        }

        adapter.setLoading(isLoading);
    }

    private final RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            int lastVisibleItemIndex = layoutManager.findLastVisibleItemPosition();
            int totalItemCount = currentList.size();

            Log.w("MyApp", String.valueOf(totalItemCount));
            Log.w("MyApp", String.valueOf(layoutManager.getItemCount()));
            if (!isLoading && totalItemCount - lastVisibleItemIndex <= ITEMS_LEFT_BEFORE_LOADING_MORE) {
                getRecommendedProjects();
                refreshViewState();
            }
        }
    };

    private void getRecommendedProjects() {
        if (isLoading) {
            return;
        }

        isLoading = true;
        HashMap<String, String> values = new HashMap<>();
        values.put("jobs[]", "9");

        Log.w("myApp", "Sending new request");
        projectsApi.getRecommendedProjects(offset, ITEMS_PER_REQUEST, values)
                .enqueue(new Callback<RetrofitResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<RetrofitResponse> call,
                                           @NonNull Response<RetrofitResponse> response) {
                        RetrofitResponse retrofitResponse = response.body();
                        if (retrofitResponse != null && retrofitResponse.getResult() != null) {
                            List<GafProject> tempProjects = retrofitResponse.getResult().getProjects();
                            currentSize += tempProjects.size();
                            for (GafProject project : tempProjects) {
                                projects.add(project);
                                Log.w("MyApp", "adding: " + project.getTitle());
                            }
                            adapter.addAll(tempProjects);
                            offset += currentSize;
                        }

                        isLoading = false;
                        refreshViewState();
                        Log.w("myApp", "SUCCESS!!");
                    }

                    @Override
                    public void onFailure(@NonNull Call<RetrofitResponse> call, @NonNull Throwable t) {
                        isLoading = false;
                        refreshViewState();
                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onRefresh() {
        Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> mSwipeRefreshLayout.setRefreshing(false),
                2000
        );
    }
}
