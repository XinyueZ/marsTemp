package com.marstemp.app.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chopping.bus.CloseDrawerEvent;
import com.chopping.utils.Utils;
import com.marstemp.R;
import com.marstemp.api.Api;
import com.marstemp.app.App;
import com.marstemp.app.fragments.AboutDialogFragment;
import com.marstemp.app.fragments.AppListImpFragment;
import com.marstemp.bus.EULAConfirmedEvent;
import com.marstemp.bus.EULARejectEvent;
import com.marstemp.databinding.ActivityMainBinding;
import com.marstemp.ds.Entry;
import com.marstemp.ds.Latest;
import com.marstemp.utils.Prefs;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Main {@link android.app.Activity} of the App.
 *
 * @author Xinyue Zhao
 */
public class MainActivity extends MarsTempActivity {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_main;
	/**
	 * Height of action-bar general.
	 */
	private int mActionBarHeight;
	/**
	 * The layout-mgr controls over {@link RecyclerView}.
	 */
	private LinearLayoutManager mLayoutManager;
	/**
	 * Data-binding.
	 */
	private ActivityMainBinding mBinding;
	/**
	 * Latest report data.
	 */
	private TextView mLatestReportTv;

	//[Begin for detecting scrolling onto bottom]
	private int mVisibleItemCount;
	private int mPastVisibleItems;
	private int mTotalItemCount;
	private boolean mLoading = true;
	//[End]

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.chopping.bus.CloseDrawerEvent }.
	 *
	 * @param e
	 * 		Event {@link com.chopping.bus.CloseDrawerEvent}.
	 */
	public void onEvent(CloseDrawerEvent e) {
		mBinding.drawerLayout.closeDrawer(Gravity.RIGHT);
	}


	//	/**
	//	 * Handler for {@link com.cusnews.bus.OpenEntryEvent}.
	//	 *
	//	 * @param e
	//	 * 		Event {@link com.cusnews.bus.OpenEntryEvent}.
	//	 */
	//	public void onEvent(OpenEntryEvent e) {
	//		DetailActivity.showInstance(this, e.getEntry(), mKeyword);
	//	}


	/**
	 * Handler for {@link  EULARejectEvent}.
	 *
	 * @param e
	 * 		Event {@link  EULARejectEvent}.
	 */
	public void onEvent(EULARejectEvent e) {
		ActivityCompat.finishAfterTransition(this);
	}

	/**
	 * Handler for {@link EULAConfirmedEvent}
	 *
	 * @param e
	 * 		Event {@link  EULAConfirmedEvent}.
	 */
	public void onEvent(EULAConfirmedEvent e) {
		//getData()
	}

	//------------------------------------------------


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Init data-binding.
		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		//Init application basic elements.
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));
		initNavHead();
		initAppBar();
		initPull2Load();
		setupDrawerContent(mBinding.navView);
		initList();
		mBinding.fab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				if (mBinding.getEntriesAdapter() != null && mBinding.getEntriesAdapter().getItemCount() > 0) {
//					mLayoutManager.scrollToPositionWithOffset(0, 0);
//				}
			}
		});
	}

	/**
	 * Init the "head"-view on the navigation-drawer.
	 */
	private void initNavHead() {
		mLatestReportTv = (TextView) findViewById(R.id.latest_data_tv);
		findViewById(R.id.latest_retry_vg).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.showLongToast(App.Instance, R.string.lbl_loading_latest_report);
				getLatest();
			}
		});
	}

	/**
	 * Init the view of list of data.
	 */
	private void initList() {
		mBinding.entriesRv.setLayoutManager(mLayoutManager = new LinearLayoutManager(MainActivity.this));
		mBinding.entriesRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				//Scrolling up and down can hiden and show the FAB.
				float y = ViewCompat.getY(recyclerView);
				if (y < dy) {
					if (!mBinding.fab.isHidden()) {
						mBinding.fab.hide();
					}
				} else {
					if (mBinding.fab.isHidden()) {
						mBinding.fab.show();
					}
				}

				//Calc whether the list has been scrolled on bottom,
				//this lets app to getting next page.
				mVisibleItemCount = mLayoutManager.getChildCount();
				mTotalItemCount = mLayoutManager.getItemCount();
				if (mLayoutManager instanceof LinearLayoutManager) {
					mPastVisibleItems = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
				}

				//if (!mIsBottom) {
				if (mLoading) {
					if ((mVisibleItemCount + mPastVisibleItems) >= mTotalItemCount) {
						mLoading = false;
						//mStart += 10;
						//getData();
					}
				}
				//}

			}

		});
	}

	/**
	 * Make the app-bar top.
	 */
	private void initAppBar() {
		mActionBarHeight = calcActionBarHeight(this);
		setSupportActionBar(mBinding.toolbar);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	/**
	 * Make the refresh-tool: pull-to-reload.
	 */
	private void initPull2Load() {
		mBinding.contentSrl.setColorSchemeResources(R.color.green_1, R.color.green_2, R.color.green_3, R.color.green_4);
		mBinding.contentSrl.setProgressViewEndTarget(true, mActionBarHeight * 2);
		mBinding.contentSrl.setProgressViewOffset(false, 0, mActionBarHeight * 2);
		mBinding.contentSrl.setRefreshing(true);
		mBinding.contentSrl.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				//				TabLabelManager.getInstance().init(MainActivity.this, false);
				//getData();
			}
		});
	}


	/**
	 * Set-up of navi-bar left.
	 */
	private void setupDrawerContent(NavigationView navigationView) {
		navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(MenuItem menuItem) {
				menuItem.setChecked(true);
				mBinding.drawerLayout.closeDrawer(Gravity.LEFT);

				switch (menuItem.getItemId()) {
				case R.id.action_api:
					WebViewActivity.showInstance(MainActivity.this, getString(R.string.lbl_maas),
							Prefs.getInstance().getApiHome());
					break;
				case R.id.action_more_apps:
					mBinding.drawerLayout.openDrawer(Gravity.RIGHT);
					break;
				}
				return true;
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (mBinding.drawerLayout.isDrawerOpen(Gravity.RIGHT) || mBinding.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
			mBinding.drawerLayout.closeDrawers();
		} else {
			super.onBackPressed();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mBinding.drawerLayout.openDrawer(GravityCompat.START);
			return true;
		case R.id.action_about:
			showDialogFragment(AboutDialogFragment.newInstance(this), null);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onAppConfigLoaded() {
		super.onAppConfigLoaded();
		showAppList();
		com.marstemp.api.Api.initialize(App.Instance, Prefs.getInstance().getApiHost());
		getLatest();
	}

	@Override
	protected void onAppConfigIgnored() {
		super.onAppConfigIgnored();
		showAppList();
		com.marstemp.api.Api.initialize(App.Instance, Prefs.getInstance().getApiHost());
		getLatest();
	}

	/**
	 * Show all external applications links.
	 */
	private void showAppList() {
		getSupportFragmentManager().beginTransaction().replace(R.id.app_list_fl, AppListImpFragment.newInstance(this))
				.commit();
	}

	/**
	 * {@code true} if it is stilling loading the last report.
	 */
	private volatile boolean mLoadingLatest;

	/**
	 * Get latest data.
	 */
	private synchronized  void getLatest() {
		if(!mLoadingLatest) {
			mLoadingLatest = true;
			Api.getLatest(Prefs.getInstance().getApiLatest(), new Callback<Latest>() {
				@Override
				public void success(Latest latest, Response response) {
					Entry entry = latest.getReport();
					String latestReport = getString(R.string.lbl_latest_report_content,
							entry.getMinTemp(),
							entry.getMaxTemp(),
							entry.getPressure(),
							entry.getAtmoOpacity(),
							entry.getSunrise(),
							entry.getSunset()
							);
					mLatestReportTv.setText(latestReport);
					mLoadingLatest=false;
					Utils.showLongToast(App.Instance, R.string.lbl_loaded_latest_report);
				}

				@Override
				public void failure(RetrofitError error) {
					mLoadingLatest=false;
					mLatestReportTv.setText(R.string.lbl_loading_error);
				}
			});
		}
	}
}
