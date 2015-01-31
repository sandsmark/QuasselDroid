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
