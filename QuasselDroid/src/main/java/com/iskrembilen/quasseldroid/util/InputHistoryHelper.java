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

package com.iskrembilen.quasseldroid.util;

import java.util.ArrayList;
import java.util.List;

public class InputHistoryHelper {
    private static List<String> history = new ArrayList<String>();
    private static int currentIndex = -1;
    private static String tempStore;

    public static String getNextHistoryEntry() {
        if (history.size() == 0) {
            return "";
        }

        if (currentIndex < history.size() - 1) {
            currentIndex++;
        }

        return history.get(currentIndex);
    }

    public static String getPreviousHistoryEntry() {
        if (history.size() == 0) {
            return "";
        }

        if (currentIndex >= 0) {
            currentIndex--;
        }
        if (currentIndex == -1)
            return tempStore;

        return history.get(currentIndex);
    }

    public static void addHistoryEntry(String text) {
        history.add(0, text);
        currentIndex = -1;
    }

    public static void tempStoreCurrentEntry(String text) {
        if (currentIndex == -1)
            tempStore = text;
    }

    public static boolean isViewingHistory() {
        return (currentIndex >= 0);
    }

    public static String[] getHistory() {
        return history.toArray(new String[history.size()]);
    }

    public static void removeItem() {
        history.remove(0);
    }
}
