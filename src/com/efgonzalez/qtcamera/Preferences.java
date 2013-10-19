package com.efgonzalez.qtcamera;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Preferences extends PreferenceActivity implements
		OnPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		String[] prefNames = new String[] { "alt_shutter", "flash_mode",
				"scene_mode", "color_effect" };

		Preference p = null;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		for (String prefName : prefNames) {
			p = findPreference(prefName);
			p.setOnPreferenceChangeListener(this);
			String s = prefs.getString(p.getKey(), "");
			setPreferenceSummary(p, s);
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		setPreferenceSummary(preference, newValue.toString());
		return true;
	}

	private void setPreferenceSummary(Preference preference, String newSummary) {
		if (preference.getKey().equalsIgnoreCase("alt_shutter")) {
			String[] sc = getResources().getStringArray(R.array.shortcuts);
			newSummary = sc[Integer.parseInt(newSummary)];
		} else if (preference.getKey().equalsIgnoreCase("flash_mode")) {
			String[] sc = getResources().getStringArray(R.array.flash);
			newSummary = sc[Integer.parseInt(newSummary)];
		} else if (preference.getKey().equalsIgnoreCase("scene_mode")) {
			String[] sc = getResources().getStringArray(R.array.scene);
			newSummary = sc[Integer.parseInt(newSummary)];
		} else if (preference.getKey().equalsIgnoreCase("color_effect")) {
			String[] sc = getResources().getStringArray(R.array.effects);
			newSummary = sc[Integer.parseInt(newSummary)];
		}
		preference.setSummary(newSummary);
	}
}
