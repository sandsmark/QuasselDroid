/*
    QuasselDroid - Quassel client for Android
    Copyright (C) 2015 Ken Børge Viktil
    Copyright (C) 2015 Magnus Fjell
    Copyright (C) 2015 Martin Sandsmark <martin.sandsmark@kde.org>

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

package com.iskrembilen.quasseldroid.gui.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.protocol.state.Client;
import com.iskrembilen.quasseldroid.protocol.state.IgnoreListManager;
import com.iskrembilen.quasseldroid.util.ThemeUtil;

public class IgnoreItemActivity extends ActionBarActivity {

    Spinner  ruleType;
    Spinner  strictness;

    Spinner  matching;
    EditText ignoreRule;

    Spinner  scopeType;
    EditText scopeRule;

    IgnoreListManager.IgnoreListItem item;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeUtil.theme);
        super.onCreate(savedInstanceState);

        if (!getIntent().hasExtra("ignoreId")) {
            finish();
            Toast.makeText(this, "Rule couldn’t be found", Toast.LENGTH_SHORT).show();
        } else if (getIntent().getIntExtra("ignoreId", -1) == -1) {
            item = null;
        } else {
            item = Client.getInstance().getIgnoreListManager().getIgnoreList().get(getIntent().getIntExtra("ignoreId", -1));
        }

        setContentView(R.layout.layout_ignoreitem);

        ruleType = (Spinner) findViewById(R.id.ignoreitem_ruletype);
        strictness = (Spinner) findViewById(R.id.ignoreitem_strictness);

        matching = (Spinner) findViewById(R.id.ignoreitem_matching);
        ignoreRule = (EditText) findViewById(R.id.ignoreitem_rule);

        scopeType = (Spinner) findViewById(R.id.ignoreitem_scopetype);
        scopeRule = (EditText) findViewById(R.id.ignoreitem_scoperule);

        SpinnerAdapter ruleTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.ignoreitem_ruletypes));
        ruleType.setAdapter(ruleTypeAdapter);
        SpinnerAdapter strictnessAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.ignoreitem_strictness));
        strictness.setAdapter(strictnessAdapter);
        SpinnerAdapter matchinAdapterg = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.ignoreitem_matching));
        matching.setAdapter(matchinAdapterg);
        SpinnerAdapter scopeTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.ignoreitem_scopeType));
        scopeType.setAdapter(scopeTypeAdapter);

        if (item!=null) {
            ruleType.setSelection(item.getType().ordinal());
            strictness.setSelection(item.getStrictness().ordinal());
            matching.setSelection(item.isRegEx() ? 1 : 0);
            scopeType.setSelection(item.getScope().ordinal());
            ignoreRule.setText(item.getIgnoreRule());
            scopeRule.setText(item.getScopeRule());
        }

        scopeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Don’t enable the input for scoperule if we have the globalscope selected
                scopeRule.setEnabled(position != IgnoreListManager.ScopeType.GLOBAL_SCOPE.value());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        scopeRule.setEnabled(scopeType.getSelectedItemPosition() != IgnoreListManager.ScopeType.GLOBAL_SCOPE.value());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_check);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                storeData();
                finish();
                return true;
            case R.id.menu_cancel:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void storeData() {
        IgnoreListManager.IgnoreType dataRuleType = IgnoreListManager.IgnoreType.fromValue(ruleType.getSelectedItemPosition());
        IgnoreListManager.StrictnessType dataStrictness = IgnoreListManager.StrictnessType.fromValue(strictness.getSelectedItemPosition());
        boolean dataIsRegEx = matching.getSelectedItemPosition() == 1;
        IgnoreListManager.ScopeType dataScopeType = IgnoreListManager.ScopeType.fromValue(scopeType.getSelectedItemPosition());

        String dataIgnoreRule = ignoreRule.getText().toString();
        String dataScopeRule = scopeRule.getText().toString();

        if (item!=null) {
            boolean originalIsActive = item.isActive();
            item.setAttributes(dataRuleType, dataIgnoreRule, dataIsRegEx, dataStrictness, dataScopeType, dataScopeRule, originalIsActive);
        } else {
            boolean originalIsActive = false;
            item = new IgnoreListManager.IgnoreListItem(dataRuleType, dataIgnoreRule, dataIsRegEx, dataStrictness, dataScopeType, dataScopeRule, originalIsActive);
            Client.getInstance().getIgnoreListManager().addIgnoreListItem(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.activity_identities, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
