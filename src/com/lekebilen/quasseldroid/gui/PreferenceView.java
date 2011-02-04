package com.lekebilen.quasseldroid.gui;

import com.lekebilen.quasseldroid.R;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class PreferenceView extends PreferenceActivity {
	
	Preference backlogLimit;
	Preference backlogAdditional;
	
	/**	 Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.layout.preferences);
    	
    	
    	//get a handle on preferences that require validation
    	backlogLimit = getPreferenceScreen().findPreference("backlogLimit");
    	backlogAdditional = getPreferenceScreen().findPreference("backlogAdditional");
        //Validate numbers only
    	backlogLimit.setOnPreferenceChangeListener(numberCheckListener);
    	backlogAdditional.setOnPreferenceChangeListener(numberCheckListener);
    }

    /**
     * Checks that a preference is a valid numerical value
     */
    Preference.OnPreferenceChangeListener numberCheckListener = new OnPreferenceChangeListener() {
		@Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            //Check that the string is an integer.
            return numberCheck(newValue);
        }
    };
    private boolean numberCheck(Object newValue) {
        if( !newValue.toString().equals("")  &&  newValue.toString().matches("\\d*") ) {
            return true;
        }
        else {
            Toast.makeText(PreferenceView.this, newValue+" "+getResources().getString(R.string.notANumber), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
