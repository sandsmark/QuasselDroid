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

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.iskrembilen.quasseldroid.R;

public class ActionBarPreferenceActivity extends PreferenceActivity implements Toolbar.OnMenuItemClickListener, View.OnClickListener {
    private Toolbar actionbar;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        actionbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.widget_actionbar, root, false);
        root.addView(actionbar, 0); // insert at top
        actionbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setTitle(getTitle());
        actionbar.setOnMenuItemClickListener(this);
        actionbar.setNavigationOnClickListener(this);
        actionbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
    }

    @Override
    public void setTitle(CharSequence title) {
        if (actionbar != null) ((TextView) actionbar.findViewById(R.id.action_bar_title)).setText(title);
        super.setTitle(title);
    }

    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.home) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(View view) {
        onBackPressed();
    }
}