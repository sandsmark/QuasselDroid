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

package com.iskrembilen.quasseldroid;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.iskrembilen.quasseldroid.util.ThemeUtil;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(
        formUri = "https://reports.kuschku.de/report/1/",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.POST,
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PACKAGE_NAME,
                ReportField.REPORT_ID,
                ReportField.BUILD,
                ReportField.STACK_TRACE
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.notification_report_crash
)
public class Quasseldroid extends Application {
    public Bundle savedInstanceState;
    public static Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();

        ACRA.init(this);

        applicationContext = getApplicationContext();

        //Populate the preferences with default values if this has not been done before
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preference_appearance, true);
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preference_connection, true);
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preference_notification, true);

        //Load current theme
        ThemeUtil.initTheme(this);

    }
}
