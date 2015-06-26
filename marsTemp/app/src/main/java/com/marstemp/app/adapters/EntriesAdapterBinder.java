package com.marstemp.app.adapters;

import android.databinding.BindingAdapter;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.marstemp.R;
import com.marstemp.ds.Entry;

/**
 * Helper-bind for {@link EntriesAdapter}.
 * <p/>
 * See more about <a href="https://developer.android.com/intl/zh-cn/tools/data-binding/guide.html">Data-binding</a>
 *
 * @author Xinyue Zhao
 */
public final class EntriesAdapterBinder {
	@SuppressWarnings("unchecked")
	@BindingAdapter("entriesAdapter")
	public static void setEntriesBinder(RecyclerView recyclerView, RecyclerView.Adapter adp) {
		recyclerView.setAdapter(adp);
	}

	@BindingAdapter({ "entry" })
	public static void setEntry(TextView tv, Entry entry) {
		String entryContent = tv.getContext().getString(R.string.lbl_entry_content, entry.getMinTemp(),
				entry.getMaxTemp(), String.format("%.0f, %s", entry.getPressure(), entry.getPressureStr()), entry.getAtmoOpacity(), entry.getSunrise(), entry.getSunset(),
				entry.getSeason(), entry.getTerrestrialDate());
		tv.setText(entryContent);
	}
}
