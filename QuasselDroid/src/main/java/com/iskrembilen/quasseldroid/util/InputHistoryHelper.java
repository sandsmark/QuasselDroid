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
}
