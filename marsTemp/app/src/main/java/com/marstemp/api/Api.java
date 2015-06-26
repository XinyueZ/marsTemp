package com.marstemp.api;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.util.Log;

import com.marstemp.ds.Archive;
import com.marstemp.ds.Latest;
import com.squareup.okhttp.OkHttpClient;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Api to get all Faroo-feeds.
 *
 * @author Xinyue Zhao
 */
public final class Api {
	/**
	 * For header, cache before request will be out.
	 */
	private final static RequestInterceptor sInterceptor = new RequestInterceptor() {
		@Override
		public void intercept(RequestFacade request) {
			request.addHeader("Content-Type", "application/json");
		}
	};
	private static final String TAG = Api.class.getSimpleName();
	/**
	 * Response-cache.
	 */
	private static com.squareup.okhttp.Cache sCache;
	/**
	 * The host of API.
	 */
	private static String sHost = null;
	/**
	 * Response-cache size with default value.
	 */
	private static long sCacheSize = 1024 * 10;

	/**
	 * Http-client.
	 */
	private static OkClient sClient = null;
	/**
	 * API methods.
	 */
	private static S s;

	/**
	 * Init the http-client and cache.
	 */
	private static void initClient(Context cxt) {
		// Create an HTTP client that uses a cache on the file system. Android applications should use
		// their Context to get a cache directory.
		OkHttpClient okHttpClient = new OkHttpClient();
		//		okHttpClient.networkInterceptors().add(new StethoInterceptor());

		File cacheDir = new File(cxt != null ? cxt.getCacheDir().getAbsolutePath() : System.getProperty(
				"java.io.tmpdir"), UUID.randomUUID().toString());
		try {
			sCache = new com.squareup.okhttp.Cache(cacheDir, sCacheSize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		okHttpClient.setCache(sCache);
		okHttpClient.setReadTimeout(3600, TimeUnit.SECONDS);
		okHttpClient.setConnectTimeout(3600, TimeUnit.SECONDS);
		sClient = new OkClient(okHttpClient);



		RestAdapter adapter = new RestAdapter.Builder().setClient(sClient).setRequestInterceptor(sInterceptor)
				.setLogLevel(RestAdapter.LogLevel.FULL).setEndpoint(sHost).build();
		s = adapter.create(S.class);
	}

	/**
	 * Init the http-client and cache.
	 */
	private static void initClient() {
		initClient(null);
	}

	/**
	 * To initialize API.
	 *
	 * @param host
	 * 		The host of API.
	 * @param cacheSz
	 * 		Response-cache size .
	 */
	public static void initialize(Context cxt, String host, long cacheSz) {
		sHost = host;
		sCacheSize = cacheSz;
		initClient(cxt);
	}


	/**
	 * To initialize API.
	 *
	 * @param cacheSz
	 * 		Response-cache size.
	 */
	public static void initialize(Context cxt, long cacheSz) {
		sCacheSize = cacheSz;
		initClient(cxt);
	}


	/**
	 * To initialize API.
	 *
	 * @param host
	 * 		The host of API.
	 */
	public static void initialize(Context cxt, String host) {
		sHost = host;
		initClient(cxt);
	}

	/**
	 * Api port.
	 */
	static private interface S {
		/**
		 * API method to get latest mars-temp.
		 *
		 * @param api
		 * 		The api-name.
		 * @param callback
		 * 		The response of api-call.
		 */
		@GET("/{api}/?format=json")
		void getLatest(@Path("api") String api, Callback<Latest> callback);

		/**
		 * API method to get top hot-queries.
		 *
		 * @param api
		 * 		The api-name.
		 * @param page
		 * 		The paging-index from {@code 1}.
		 * @param callback
		 * 		The response of api-call.
		 */
		@GET("/{api}/?format=json")
		void getArchive(@Path("api") String api, @Query("page") int page, Callback<Archive> callback);
	}

	/**
	 * API method to get latest mars-temp.
	 *
	 * @param api
	 * 		The api-name.
	 * @param callback
	 * 		The response of api-call.
	 */
	public static final void getLatest(String api, Callback<Latest> callback) {
		assertCall();
		s.getLatest(api, callback);
	}


	/**
	 * API method to get top hot-queries.
	 *
	 * @param api
	 * 		The api-name.
	 * @param page
	 * 		The paging-index from {@code 1}.
	 * @param callback
	 * 		The response of api-call.
	 */
	public static final void getArchive(@Path("api") String api, @Query("page") int page, Callback<Archive> callback) {
		assertCall();
		s.getArchive(api, page, callback);
	}


	/**
	 * Assert before calling api.
	 */
	private static void assertCall() {
		if (sClient == null) {//Create http-client when needs.
			initClient();
		}
		if (sHost == null) {//Default when needs.
			sHost = "http://www.faroo.com/";
		}
		Log.i(TAG, String.format("Host:%s, Cache:%d", sHost, sCacheSize));
		if (sCache != null) {
			Log.i(TAG, String.format("RequestCount:%d", sCache.getRequestCount()));
			Log.i(TAG, String.format("NetworkCount:%d", sCache.getNetworkCount()));
			Log.i(TAG, String.format("HitCount:%d", sCache.getHitCount()));
		}
	}

}
