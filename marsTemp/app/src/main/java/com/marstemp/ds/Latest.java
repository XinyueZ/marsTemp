package com.marstemp.ds;

import com.google.gson.annotations.SerializedName;

/**
 * The latest report.
 *
 * @author Xinyue Zhao
 */
public final class Latest {
	@SerializedName("report")
	private Entry mReport;


	public Entry getReport() {
		return mReport;
	}
}
