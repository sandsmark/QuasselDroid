/*
    QuasselDroid - Quassel client for Android
 	Copyright (C) 2011 Ken Børge Viktil
 	Copyright (C) 2011 Magnus Fjell
 	Copyright (C) 2011 Martin Sandsmark <martin.sandsmark@kde.org>

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
    	backlogLimit = getPreferenceScreen().findPreference(getString(R.string.preference_initial_backlog_limit));
    	backlogAdditional = getPreferenceScreen().findPreference(getString(R.string.preference_dynamic_backlog));
     //   //Validate numbers only
    //	backlogLimit.setOnPreferenceChangeListener(numberCheckListener);
    //	backlogAdditional.setOnPreferenceChangeListener(numberCheckListener);
    }

//    /**
//     * Checks that a preference is a valid numerical value
//     */
//    Preference.OnPreferenceChangeListener numberCheckListener = new OnPreferenceChangeListener() {
//		@Override
//        public boolean onPreferenceChange(Preference preference, Object newValue) {
//            //Check that the string is an integer.
//            return numberCheck(newValue);
//        }
//    };
//    private boolean numberCheck(Object newValue) {
//        if( !newValue.toString().equals("")  &&  newValue.toString().matches("\\d*") ) {
//            return true;
//        }
//        else {
//            Toast.makeText(PreferenceView.this, newValue+" "+getResources().getString(R.string.notANumber), Toast.LENGTH_SHORT).show();
//            return false;
//        }
//    }
}
