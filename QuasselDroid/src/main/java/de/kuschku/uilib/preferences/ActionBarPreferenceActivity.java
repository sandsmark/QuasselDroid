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

import android.app.*;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.gui.settings.IgnoreListFragment;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class ActionBarPreferenceActivity extends PreferenceActivity implements Toolbar.OnMenuItemClickListener, View.OnClickListener {
    private static final String BACK_STACK_PREFS = ":android:prefs";
    private Toolbar actionbar;
    private boolean mSinglePane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String initialFragment = getIntent().getStringExtra(EXTRA_SHOW_FRAGMENT);
        Bundle initialArguments = getIntent().getBundleExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.preference_actionbar);
        View mPrefsContainer = findViewById(R.id.prefs_frame);
        setValue("mPrefsContainer", mPrefsContainer);

        actionbar = (Toolbar) findViewById(R.id.action_bar);
        mSinglePane = onIsHidingHeaders() || !onIsMultiPane();

        if (!mSinglePane) {
            mPrefsContainer.setVisibility(View.VISIBLE);
        } else if (initialFragment != null) {
            switchToHeader(initialFragment, initialArguments);
            findViewById(R.id.headers).setVisibility(View.GONE);
            mPrefsContainer.setVisibility(View.VISIBLE);
        }

        setTitle(parentTitle());
        actionbar.setOnMenuItemClickListener(this);
        actionbar.setNavigationOnClickListener(this);
        actionbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        onCreateOptionsMenu();
    }

    public Toolbar getSupportActionBar() {
        return actionbar;
    }

    protected CharSequence parentTitle() {
        return getTitle();
    }

    private Object getValue(String fieldName) {
        try {
            Field field = PreferenceActivity.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(this);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setValue(String fieldName, Object value) {
        try {
            Field field = PreferenceActivity.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(this, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void switchToHeader(String fragmentName, Bundle args) {
        Header selectedHeader = null;
        ArrayList<Header> localHeaders = ((ArrayList<Header>)getValue("mHeaders"));
        for (int i = 0; i < localHeaders.size(); i++) {
            if (fragmentName.equals(localHeaders.get(i).fragment)) {
                selectedHeader = localHeaders.get(i);
                break;
            }
        }
        setSelectedHeader(selectedHeader);
        switchToHeaderInner(fragmentName, args);
    }

    private void switchToHeaderInner(String fragmentName, Bundle args) {
        getFragmentManager().popBackStack(BACK_STACK_PREFS,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
        final android.app.Fragment f = android.app.Fragment.instantiate(this, fragmentName, args);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.prefs, f);
        transaction.commitAllowingStateLoss();

        if (actionbar!=null) {
            if (f instanceof IgnoreListFragment) {
                Log.e("PA", "load menu for fragment");
                actionbar.inflateMenu(R.menu.fragment_ignorelist);
                actionbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Log.e("PA", menuItem.getTitle().toString());
                        return f.onOptionsItemSelected(menuItem);
                    }
                });
                actionbar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v instanceof MenuItem) {
                            MenuItem menuItem = (MenuItem) v;
                            Log.e("PA", menuItem.getTitle().toString());
                            f.onOptionsItemSelected(menuItem);
                        }
                    }
                });
            } else {
                Log.e("PA", "load empty menu");
                actionbar.inflateMenu(R.menu.empty);
                actionbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Log.e("PA", menuItem.getTitle().toString());
                        return false;
                    }
                });
            }
        }
    }

    private void setSelectedHeader(Header header) {
        setValue("mCurHeader", header);
        int index = ((ArrayList<Header>)getValue("mHeaders")).indexOf(header);
        if (index >= 0) {
            getListView().setItemChecked(index, true);
        } else {
            getListView().clearChoices();
        }
        showBreadCrumbs(header);
    }

    private void showBreadCrumbs(Header header) {
        if (header != null) {
            CharSequence title = header.getBreadCrumbTitle(getResources());
            if (title == null) title = header.getTitle(getResources());
            if (title == null) title = getTitle();
            showBreadCrumbs(title, header.getBreadCrumbShortTitle(getResources()));
        } else {
            showBreadCrumbs(getTitle(), null);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if (actionbar!=null) ((TextView) actionbar.findViewById(R.id.action_bar_title)).setText(title);
        super.setTitle(title);
    }

    @Override
    public void switchToHeader(Header header) {
        if (getValue("mCurHeader") == header) {
            // This is the header we are currently displaying.  Just make sure
            // to pop the stack up to its root state.
            getFragmentManager().popBackStack(BACK_STACK_PREFS,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            if (header.fragment == null) {
                throw new IllegalStateException("can't switch to header that has no fragment");
            }
            switchToHeaderInner(header.fragment, header.fragmentArguments);
            setSelectedHeader(header);
        }
    }

    /**
     * Start a new fragment.
     *
     * @param fragment The fragment to start
     * @param push If true, the current fragment will be pushed onto the back stack.  If false,
     * the current fragment will be replaced.
     */
    @Override
    public void startPreferenceFragment(Fragment fragment, boolean push) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.prefs, fragment);
        if (push) {
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(BACK_STACK_PREFS);
        } else {
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        }
        transaction.commitAllowingStateLoss();
    }

    /**
     * Start a new fragment containing a preference panel.  If the preferences
     * are being displayed in multi-pane mode, the given fragment class will
     * be instantiated and placed in the appropriate pane.  If running in
     * single-pane mode, a new activity will be launched in which to show the
     * fragment.
     *
     * @param fragmentClass Full name of the class implementing the fragment.
     * @param args Any desired arguments to supply to the fragment.
     * @param titleRes Optional resource identifier of the title of this
     * fragment.
     * @param titleText Optional text of the title of this fragment.
     * @param resultTo Optional fragment that result data should be sent to.
     * If non-null, resultTo.onActivityResult() will be called when this
     * preference panel is done.  The launched panel must use
     * {@link #finishPreferencePanel(Fragment, int, android.content.Intent)} when done.
     * @param resultRequestCode If resultTo is non-null, this is the caller's
     * request code to be received with the resut.
     */
    @Override
    public void startPreferencePanel(String fragmentClass, Bundle args, int titleRes,
                                     CharSequence titleText, Fragment resultTo, int resultRequestCode) {
        if (mSinglePane) {
            startWithFragment(fragmentClass, args, resultTo, resultRequestCode, titleRes, 0);
        } else {
            Fragment f = Fragment.instantiate(this, fragmentClass, args);
            if (resultTo != null) {
                f.setTargetFragment(resultTo, resultRequestCode);
            }
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.prefs, f);
            if (titleRes != 0) {
                transaction.setBreadCrumbTitle(titleRes);
            } else if (titleText != null) {
                transaction.setBreadCrumbTitle(titleText);
            }
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(BACK_STACK_PREFS);
            transaction.commitAllowingStateLoss();
        }
    }

    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.home) {
            return true;
        } else {
            return false;
        }
    }

    public void onCreateOptionsMenu() {
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        onBackPressed();
    }
}