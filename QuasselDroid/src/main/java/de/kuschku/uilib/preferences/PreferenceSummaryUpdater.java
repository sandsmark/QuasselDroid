/*
    Copyright © 2015 Janne Koschinski

    This program is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation, either version 3 of the License, or (at your option)
    any later version, or under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License and the
    GNU Lesser General Public License along with this program.  If not, see
    <http://www.gnu.org/licenses/>.
 */

package de.kuschku.uilib.preferences;

import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;

import com.iskrembilen.quasseldroid.R;

public class PreferenceSummaryUpdater implements Preference.OnPreferenceChangeListener {
    public void bindPreferenceSummaryToValue(Preference preference) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        preference.setOnPreferenceChangeListener(this);
        onPreferenceChange(preference, preferences.getString(preference.getKey(), ""));
    }

    /**
     * Called when a Preference has been changed by the user. This is
     * called before the state of the Preference is about to be updated and
     * before the state is persisted.
     *
     * @param preference The changed Preference.
     * @param value      The new value of the Preference.
     * @return True to update the state of the Preference with the new value.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        CharSequence stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            CharSequence[] entries = listPreference.getEntryValues();
            for (int i = 0; i < entries.length; i++) {
                if (entries[i].equals(value)) {
                    stringValue = listPreference.getEntries()[i];
                }
            }
            preference.setSummary(stringValue);
        } else if (preference instanceof RingtonePreference) {
            if (TextUtils.isEmpty(stringValue)) {
                preference.setSummary(R.string.preference_ringtone_silent);
            } else {
                Ringtone ringtone = RingtoneManager.getRingtone(
                        preference.getContext(), Uri.parse(stringValue.toString()));
                if (ringtone == null) {
                    preference.setSummary(null);
                } else {
                    preference.setSummary(ringtone.getTitle(preference.getContext()));
                }
            }
        } else if (preference instanceof EditTextPreference) {
            EditTextPreference editText = (EditTextPreference) preference;
            preference.setSummary(editText.getText());
        } else {
            Log.e(getClass().getSimpleName(), "OnChange of an unknown element! " + value);
        }
        return true;
    }
}
