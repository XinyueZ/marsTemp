package com.marstemp.ds;

import com.google.gson.annotations.SerializedName;

/**
 * A report item.
 * <p/>
 * <p/>
 * <p/>
 * <a href="http://marsweather.ingenology.com/">See more meanings of variables.</a>
 *
 * @author Xinyue Zhao
 */
public final class Entry {
	@SerializedName("terrestrial_date")
	private String mTerrestrialDate;
	@SerializedName("sol")
	private int mMarsDate;
	@SerializedName("ls")
	private double mMarsSeason;
	@SerializedName("min_temp")
	private double minTemp;
	@SerializedName("min_temp_fahrenheit")
	private double mMinTempF;
	@SerializedName("max_temp")
	private double maxTemp;
	@SerializedName("max_temp_fahrenheit")
	private double maxTempF;
	@SerializedName("pressure")
	private double mPressure;
	@SerializedName("pressure_string")
	private String mPressureStr;
	@SerializedName("atmo_opacity")
	private String mAtmoOpacity;
	@SerializedName("season")
	private String mSeason;
	@SerializedName("sunrise")
	private String mSunrise;
	@SerializedName("sunset")
	private String mSunset;


	public String getTerrestrialDate() {
		return mTerrestrialDate;
	}

	public int getMarsDate() {
		return mMarsDate;
	}

	public double getMarsSeason() {
		return mMarsSeason;
	}

	public double getMinTemp() {
		return minTemp;
	}

	public double getMinTempF() {
		return mMinTempF;
	}

	public double getMaxTemp() {
		return maxTemp;
	}

	public double getMaxTempF() {
		return maxTempF;
	}

	public double getPressure() {
		return mPressure;
	}

	public String getPressureStr() {
		return mPressureStr;
	}

	public String getAtmoOpacity() {
		return mAtmoOpacity;
	}

	public String getSeason() {
		return mSeason;
	}

	public String getSunrise() {
		return mSunrise;
	}

	public String getSunset() {
		return mSunset;
	}
}
