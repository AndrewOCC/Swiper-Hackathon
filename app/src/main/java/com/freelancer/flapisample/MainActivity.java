package com.freelancer.flapisample;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.freelancer.flapisample.adapter.RecommendedProjectsAdapter;
import com.freelancer.flapisample.data.SharedPreferencesAuthStorage;
import com.freelancer.flapisample.model.GafProject;
import com.freelancer.flapisample.model.retrofit.RetrofitResponse;
import com.freelancer.flapisample.retrofit.AuthInterceptor;
import com.freelancer.flapisample.retrofit.AuthStorage;
import com.freelancer.flapisample.retrofit.AuthTokenInterceptor;
import com.freelancer.flapisample.retrofit.FLApiConstants;
import com.freelancer.flapisample.retrofit.FLProjectsApi;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;



public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, Callback<RetrofitResponse> {

    // Constants
    private static final int ITEMS_PER_REQUEST = 20;
    private static final int ITEMS_LEFT_BEFORE_LOADING_MORE = 10;

    private AuthStorage authStorage;
    private FLProjectsApi projectsApi;
    private LinearLayoutManager layoutManager;

    // Adapters
    private RecommendedProjectsAdapter adapter;
    private RecommendedProjectsAdapter dismissedAdapter;
    private RecommendedProjectsAdapter interestedAdapter;
    private RecommendedProjectsAdapter currentAdapter;

    List<GafProject> projects = new LinkedList<GafProject>();
    List<GafProject> dismissedProjects = new LinkedList<GafProject>();
    List<GafProject> interestedProjects = new LinkedList<GafProject>();
    List<GafProject> currentList = new LinkedList<GafProject>();

    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    int viewMode = 0;
    int currentSize = 0;

    private int offset;
    private boolean isLoading;

    // Swipe from top to refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.progress_bar)
    View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Variables
        mActivityTitle = this.getString(R.string.app_name);

        // Sets up the app drawer
        mDrawerList = (ListView)findViewById(R.id.navList);mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        addDrawerItems();
        setupDrawer();

        // Sets up the swipe to refresh feature
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.container);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // Sets up the API service (which uses retrofit to turn the api into a set of java interfaces)
        prepareApiService();
        prepareViews();
        refreshViewState();

        // sets the offset for the new items being fetched
        offset = 0;

        //fetches list of recommended projects
        getRecommendedProjects();

    }

    private void addDrawerItems() {
        String[] osArray = {"Interested", "New", "Discarded"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mAdapter.getItem(position).equals("New")){
                    viewMode = 0;
                    refreshViewState();
                } else if (mAdapter.getItem(position).equals("Discarded")) {
                    viewMode = 1;
                    refreshViewState();
                } else if (mAdapter.getItem(position).equals("Interested")) {
                    viewMode = 2;
                    refreshViewState();
                }
            }
        });
    }


    private void setupDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
//        mDrawerToggle.setDrawerIndicatorEnabled(true);
//        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    //---------- Drawer Toggle methods -----------

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    //------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // Navigation Bar Toggle
        //      Pass the event to ActionBarDrawerToggle, if it returns
        //      true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Other action bar items...
        switch (item.getItemId()) {
            case R.id.menu_settings:
                // User chose the "Settings" item, show the app settings UI
                return true;

            case R.id.menu_refresh:
                // User chose the "Refresh" action, refresh the list
                onRefresh();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void prepareApiService() {
        authStorage = SharedPreferencesAuthStorage.instance(getApplication());

        // Don't forget to add in a valid freelancer account's username and password
        // These details should be provided on the day of the hackathon, or you can create one
        // yourself
        authStorage.saveUsername("AC246");
        authStorage.savePassword("upstairsfr46");
        authStorage.saveUserId("10");
        authStorage.saveAuthToken("FLNS5T35XAX8EIKYBDYK7XMHWD3CX5J6");
        //authStorage.saveAuthToken("FLNJ1M21PACNH8Z9F5TJNWM4PX7ABLSO");

        // Create an {@link OkHttpClient} that adds an {@link AuthInterceptor} to the interceptors
        // chain so that the auth credentials can automatically be refreshed when the request
        // encounters a 401 response from the server.
        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new AuthInterceptor(authStorage));

        // Create a {@link RestAdapter} that creates our api service instances
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .setEndpoint(FLApiConstants.BASE_URL)
                .setClient(new OkClient(client))
                .setRequestInterceptor(new AuthTokenInterceptor(authStorage))
                .build();



        // Create an instance of our projects api service
        projectsApi = restAdapter.create(FLProjectsApi.class);
    }

    private void prepareViews() {
        ButterKnife.bind(this);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecommendedProjectsAdapter();
        dismissedAdapter = new RecommendedProjectsAdapter();
        interestedAdapter = new RecommendedProjectsAdapter();
        recyclerView.setAdapter(adapter);

        recyclerView.setOnScrollListener(scrollListener);
    }

    private void refreshViewState() {
        if (adapter.getItemCount() == 0 && viewMode == 0) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        } else {
            if (this.isLoading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.INVISIBLE);
            }
            recyclerView.setVisibility(View.VISIBLE);


            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            if(viewMode == 0) {
                mRecyclerView.setAdapter(adapter);
                currentList = projects;
                currentAdapter = adapter;
            } else if (viewMode == 1) {
                mRecyclerView.setAdapter(dismissedAdapter);
                currentList = dismissedProjects;
                currentAdapter = dismissedAdapter;
            } else if (viewMode == 2) {
                mRecyclerView.setAdapter(interestedAdapter);
                currentList = interestedProjects;
                currentAdapter = interestedAdapter;
            }



            SwipeableRecyclerViewTouchListener swipeTouchListener =
                    new SwipeableRecyclerViewTouchListener(mRecyclerView,
                            new SwipeableRecyclerViewTouchListener.SwipeListener() {
                                @Override
                                public boolean canSwipe(int position) {

                                    if(viewMode == 0) {
                                        return true;
                                    } else if (viewMode == 1) {
                                        return true;
                                    } else if (viewMode == 2) {
                                        return true;
                                    }
                                    return false;
                                }

                                @Override
                                public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                    for (int position : reverseSortedPositions) {
                                        if (currentList.size() > 0) {

                                            if(viewMode == 0) {
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
                                            for (GafProject i : projects) {
                                                Log.w("MyApp", i.getId() + i.getPreviewDescription());
                                            }
                                        }

                                    }
                                    currentAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                    for (int position : reverseSortedPositions) {

                                        if(viewMode == 0) {
                                            dismissedProjects.add(currentList.get(position));
                                            dismissedAdapter.add(currentList.get(position));
                                        } else if (viewMode == 1) {
                                            //nothing: remove completely
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

            mRecyclerView.addOnItemTouchListener(swipeTouchListener);

        }

        adapter.setLoading(isLoading);
    }

    // A simple scroll listener that loads more items if we're nearing the end of the list
    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView mRecyclerView, int dx, int dy) {
            int lastVisibleItemIndex = layoutManager.findLastVisibleItemPosition();//.findFirstVisibleItemPosition() + layoutManager.getChildCount();
            int totalItemCount = currentList.size(); //layoutManager.getItemCount();

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

        //RetrofitResponse x = new RetrofitResponse();

        HashMap<String, String> values = new HashMap<String, String>();
        values.put("jobs[]", "9");

        Log.w("myApp", "Sendingnew  request");
        projectsApi.getRecommendedProjects(offset, ITEMS_PER_REQUEST, values, this);

        //Log.w("myApp", x.)
    }

    @Override
    public void success(RetrofitResponse retrofitResponse, Response response) {
        if (retrofitResponse.getResult() != null) {
            List<GafProject> tempProjects = retrofitResponse.getResult().getProjects();
            currentSize += tempProjects.size();
            for(GafProject i: tempProjects){
                projects.add(i);
                Log.w("MyApp", "adding: "+ i.getTitle());
            }
            adapter.addAll(tempProjects);
            offset += currentSize;
        }

        isLoading = false;
        refreshViewState();
        Log.w("myApp", "SUCCESS!!");
    }

    @Override
    public void failure(RetrofitError error) {
        isLoading = false;
        refreshViewState();
        Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
        //Log.w("myApp", new String(((TypedByteArray) error.getResponse().getBody()).getBytes()));
    }

    @Override
    public void onRefresh() {

        Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

}
