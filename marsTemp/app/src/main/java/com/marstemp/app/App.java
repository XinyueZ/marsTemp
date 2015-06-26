/*
                   _ooOoo_
                  o8888888o
                  88" . "88
                  (| -_- |)
                  O\  =  /O
               ____/`---'\____
             .'  \\|     |//  `.
            /  \\|||  :  |||//  \
           /  _||||| -:- |||||-  \
           |   | \\\  -  /// |   |
           | \_|  ''\---/''  |   |
           \  .-\__  `-`  ___/-. /
         ___`. .'  /--.--\  `. . __
      ."" '<  `.___\_<|>_/___.'  >'"".
     | | :  `- \`.;`\ _ /`;.`/ - ` : | |
     \  \ `-.   \_ __\ /__ _/   .-` /  /
======`-.____`-.___\_____/___.-`____.-'======
                   `=---='
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
         佛祖保佑       永无BUG, Never BUG.
*/
//          佛曰:
//                  写字楼里写字间，写字间里程序员；
//                  程序人员写程序，又拿程序换酒钱。
//                  酒醒只在网上坐，酒醉还来网下眠；
//                  酒醉酒醒日复日，网上网下年复年。
//                  但愿老死电脑间，不愿鞠躬老板前；
//                  奔驰宝马贵者趣，公交自行程序员。
//                  别人笑我忒疯癫，我笑自己命太贱。
package com.marstemp.app;

import android.app.Application;
import android.text.TextUtils;

import com.chopping.net.TaskHelper;
import com.marstemp.R;
import com.marstemp.utils.Prefs;
import com.tinyurl4j.data.Response;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * The application-instance object, for global access cross some components.
 *
 * @author Xinyue Zhao
 */
public final class App extends Application {
	/**
	 * Const means that no-data available.
	 */
	public static final String NOT_FOUND = "Not found";
	/**
	 * First page-index of archive.
	 */
	public static final int FIRST_PAGE= 1;
	/**
	 * Singleton.
	 */
	public static App Instance;


	@Override
	public void onCreate() {
		super.onCreate();
		Instance = this;

		//Init components in Chopping(submodule)
		TaskHelper.init(getApplicationContext());
		//Init app-preference.
		Prefs.createInstance(this);
		//Short the link of app-download and make a download-info, store to preference.
		//You'll see text like
		//<code>
		//			Download: http://tinyurl/asdfasdf
		//</code>
		//in sharing text.
		String url = Prefs.getInstance().getAppDownloadInfo();
		if (TextUtils.isEmpty(url) || !url.contains("tinyurl")) {
			com.tinyurl4j.Api.getTinyUrl(getString(R.string.lbl_store_url, getPackageName()), new Callback<Response>() {
				@Override
				public void success(Response response, retrofit.client.Response response2) {
					Prefs.getInstance().setAppDownloadInfo(getString(R.string.lbl_share_download_app, getString(
							R.string.application_name), response.getResult()));
				}

				@Override
				public void failure(RetrofitError error) {
					Prefs.getInstance().setAppDownloadInfo(getString(R.string.lbl_share_download_app, getString(
							R.string.application_name), getString(R.string.lbl_store_url, getPackageName())));
				}
			});
		}
	}
}
