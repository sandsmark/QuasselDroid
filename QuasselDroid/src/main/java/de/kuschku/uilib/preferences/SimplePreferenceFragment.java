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
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;

import java.util.Map;

public class SimplePreferenceFragment extends PreferenceFragment {

    PreferenceSummaryUpdater updater = new PreferenceSummaryUpdater();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey("dataset")) {
            String res = getArguments().getString("dataset");
            int id = getResources().getIdentifier(res, "xml", getActivity().getApplication().getPackageName());
            addPreferencesFromResource(id);

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Map<String, ?> map = pref.getAll();
            for (String key : map.keySet()) {
                Preference preference = findPreference(key);
                if (preference != null) {
                    if (preference instanceof ListPreference ||
                            preference instanceof EditTextPreference ||
                            preference instanceof RingtonePreference) {
                        updater.bindPreferenceSummaryToValue(preference);
                    }
                }
            }
        }
    }
}
