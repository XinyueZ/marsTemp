package com.marstemp.app.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.marstemp.BR;
import com.marstemp.R;
import com.marstemp.ds.Entry;


/**
 * The adapter for the list of {@link  Entry}s.
 *
 * @author Xinyue Zhao
 */
public final class EntriesAdapter extends RecyclerView.Adapter<EntriesAdapter.ViewHolder> {
	/**
	 * Data-source.
	 */
	private List<Entry> mEntries;

	/**
	 * Main layout for this component.
	 */
	private static final int ITEM_LAYOUT = R.layout.item_entry_list;

	/**
	 * Constructor of {@link EntriesAdapter}
	 */
	public EntriesAdapter( ) {
		setData(new ArrayList<Entry>());
	}

	/**
	 * Constructor of {@link EntriesAdapter}
	 * @param entries
	 * 		Data-source.
	 */
	public EntriesAdapter( List<Entry> entries) {
		setData(entries);
	}

	/**
	 * Set data-source.
	 *
	 * @param entries
	 */
	public void setData(List<Entry> entries) {
		mEntries = entries;
	}

	/**
	 * @return Data-source.
	 */
	public List<Entry> getData() {
		return mEntries;
	}


	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Context cxt = parent.getContext();
		LayoutInflater inflater = LayoutInflater.from(cxt);
		ViewDataBinding binding = DataBindingUtil.inflate(inflater, ITEM_LAYOUT, parent, false);
		return new EntriesAdapter.ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {
		final Entry entry = getData().get(position);
		holder.mBinding.setVariable(BR.entry, entry);
		holder.mBinding.executePendingBindings();
	}


	@Override
	public int getItemCount() {
		return mEntries == null ? 0 : mEntries.size();
	}

	/**
	 * View-holder pattern.
	 */
	public static class ViewHolder extends RecyclerView.ViewHolder {
		private ViewDataBinding mBinding;

		public ViewHolder(ViewDataBinding binding) {
			super(binding.getRoot());
			mBinding = binding;
		}
	}
}
