package com.marstemp.app.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chopping.bus.CloseDrawerEvent;
import com.chopping.utils.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.marstemp.R;
import com.marstemp.api.Api;
import com.marstemp.app.App;
import com.marstemp.app.adapters.EntriesAdapter;
import com.marstemp.app.fragments.AboutDialogFragment;
import com.marstemp.app.fragments.AppListImpFragment;
import com.marstemp.bus.EULAConfirmedEvent;
import com.marstemp.bus.EULARejectEvent;
import com.marstemp.databinding.ActivityMainBinding;
import com.marstemp.ds.Archive;
import com.marstemp.ds.Entry;
import com.marstemp.ds.Latest;
import com.marstemp.utils.Prefs;
import com.marstemp.widgets.DynamicShareActionProvider;

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
	/**
	 * Current loading page.
	 */
	private volatile int mPage = App.FIRST_PAGE;
	/**
	 * The interstitial ad.
	 */
	private InterstitialAd mInterstitialAd;
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
		if (savedInstanceState != null) {
			mPage = savedInstanceState.getInt("page");
			mIsBottom = savedInstanceState.getBoolean("isBottom");
			mLoadingLatest = savedInstanceState.getBoolean("loadingLatest");
			mLoadingArchive = savedInstanceState.getBoolean("loadingArchive");
		}
		//Init data-binding.
		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		mBinding.setEntriesAdapter(new EntriesAdapter());
		//Init application basic elements.
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));
		initNavHead();
		initAppBar();
		initPull2Load();
		setupDrawerContent(mBinding.navView);
		initList();
		initAdmob();
		mBinding.fab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Click FAB and jump page to top.
				if (mBinding.getEntriesAdapter() != null && mBinding.getEntriesAdapter().getItemCount() > 0) {
					mLayoutManager.scrollToPositionWithOffset(0, 0);
				}
				Utils.showShortToast(App.Instance, R.string.lbl_jump_up);
			}
		});
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("page", mPage);
		outState.putBoolean("isBottom", mIsBottom);
		outState.putBoolean("loadingLatest", mLoadingLatest);
		outState.putBoolean("loadingArchive", mLoadingArchive);
	}

	/**
	 * Ads for the app.
	 */
	private void initAdmob() {
		int curTime = App.Instance.getAdsShownTimes();
		int adsTimes = 7;
		if (curTime % adsTimes == 0) {
			// Create an ad.
			mInterstitialAd = new InterstitialAd(this);
			mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
			// Create ad request.
			AdRequest adRequest = new AdRequest.Builder().build();
			// Begin loading your interstitial.
			mInterstitialAd.setAdListener(new AdListener() {
				@Override
				public void onAdLoaded() {
					super.onAdLoaded();
					displayInterstitial();
				}
			});
			mInterstitialAd.loadAd(adRequest);
		}
		curTime++;
		App.Instance.setAdsShownTimes(curTime);
	}

	/**
	 * Invoke displayInterstitial() when you are ready to display an interstitial.
	 */
	public void displayInterstitial() {
		if (mInterstitialAd.isLoaded()) {
			mInterstitialAd.show();
		}
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

				if (!mIsBottom) {
					if (mLoading) {
						if ((mVisibleItemCount + mPastVisibleItems) >= mTotalItemCount) {
							mLoading = false;
							mPage++;//Move to next page.
							Snackbar.make(mBinding.coordinatorLayout, R.string.lbl_load_more, Snackbar.LENGTH_SHORT)
									.show();
							getArchive();
						}
					}
				}
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
				//Reload all data.
				mIsBottom = false;
				mPage = 1;
				mLoadingArchive = false;
				mBinding.getEntriesAdapter().getData().clear();
				Snackbar.make(mBinding.coordinatorLayout, R.string.lbl_reload, Snackbar.LENGTH_SHORT).show();
				getArchive();
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
	public boolean onPrepareOptionsMenu(final Menu menu) {
		//Share application.
		MenuItem menuAppShare = menu.findItem(R.id.action_share);
		DynamicShareActionProvider shareLaterProvider = (DynamicShareActionProvider) MenuItemCompat.getActionProvider(
				menuAppShare);
		shareLaterProvider.setShareDataType("text/plain");
		shareLaterProvider.setOnShareLaterListener(new DynamicShareActionProvider.OnShareLaterListener() {
			@Override
			public void onShareClick(final Intent shareIntent) {
				String subject = getString(R.string.lbl_share_app_title);
				String text = getString(R.string.lbl_share_app_content, getString(R.string.application_name),
						Prefs.getInstance().getAppDownloadInfo());
				shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
				shareIntent.putExtra(Intent.EXTRA_TEXT, text);
				startActivity(shareIntent);
			}
		});
		return super.onPrepareOptionsMenu(menu);
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
		onFinishedConfig();
	}


	@Override
	protected void onAppConfigIgnored() {
		super.onAppConfigIgnored();
		onFinishedConfig();
	}

	/**
	 * App-config has been done.
	 */
	private void onFinishedConfig() {
		showAppList();
		Api.initialize(App.Instance, Prefs.getInstance().getApiHost());
		getLatest();
		getArchive();
		Utils.showShortToast(App.Instance, R.string.lbl_loading_archive);
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
	private synchronized void getLatest() {
		if (!mLoadingLatest) {
			mLoadingLatest = true;
			Api.getLatest(Prefs.getInstance().getApiLatest(), new Callback<Latest>() {
				@Override
				public void success(Latest latest, Response response) {
					Entry entry = latest.getReport();
					String latestReport = getString(R.string.lbl_latest_report_content, entry.getMinTemp(),
							entry.getMaxTemp(), entry.getPressure(), entry.getAtmoOpacity(), entry.getSunrise(),
							entry.getSunset(), entry.getSeason(), entry.getTerrestrialDate());
					mLatestReportTv.setText(latestReport);
					mLoadingLatest = false;
					Utils.showLongToast(App.Instance, R.string.lbl_loaded_latest_report);
				}

				@Override
				public void failure(RetrofitError error) {
					mLoadingLatest = false;
					mLatestReportTv.setText(R.string.lbl_loading_error);
				}
			});
		}
	}

	/**
	 * {@code true} if it is stilling loading archive.
	 */
	private volatile boolean mLoadingArchive;
	/**
	 * {@code true} if reached bottom of data-source.
	 */
	private volatile boolean mIsBottom;

	/**
	 * Load list of archives.
	 */
	private synchronized void getArchive() {
		if (!mLoadingArchive) {
			mBinding.contentSrl.setRefreshing(true);
			mLoadingArchive = true;
			Api.getArchive(Prefs.getInstance().getApiArchive(), mPage, new Callback<Archive>() {
				@Override
				public void success(Archive archive, Response response) {
					if (TextUtils.equals(archive.getDetail(), App.NOT_FOUND)) {
						mIsBottom = true;
						Snackbar.make(mBinding.coordinatorLayout, R.string.lbl_no_more_data, Snackbar.LENGTH_LONG)
								.show();
					} else {
						mBinding.getEntriesAdapter().getData().addAll(archive.getResults());
						mBinding.getEntriesAdapter().notifyDataSetChanged();
						//Finish loading
						mBinding.contentSrl.setRefreshing(false);
						mLoadingArchive = false;
						mLoading = true;
					}
				}

				@Override
				public void failure(RetrofitError error) {
					if (mPage > App.FIRST_PAGE) {
						mPage--;
					}
					Snackbar.make(mBinding.coordinatorLayout, R.string.lbl_loading_error, Snackbar.LENGTH_LONG)
							.setAction(R.string.btn_retry, new OnClickListener() {
								@Override
								public void onClick(View v) {
									getArchive();
								}
							}).show();

					//Finish loading
					mBinding.contentSrl.setRefreshing(false);
					mLoadingArchive = false;
					mLoading = true;
				}
			});
		}
	}
}
