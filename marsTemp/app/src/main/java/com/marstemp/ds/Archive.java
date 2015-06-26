package com.marstemp.ds;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * A list of {@link Entry}s.
 *
 * @author Xinyue Zhao
 */
public final class Archive {
	@SerializedName("results")
	private List<Entry> mResults;


	public List<Entry> getResults() {
		return mResults;
	}
}
