package com.iskrembilen.quasseldroid.util;

import java.util.ArrayList;
import java.util.List;

public class InputHistoryHelper {
	private static List<String> history = new ArrayList<String>();
	private static int currentIndex = 0;
	
	public static String getNextHistoryEntry(){
		if (history.size() == 0) {
			return "";
		}

		if (currentIndex < history.size() -1 ) {
			currentIndex++;
		}

		return history.get(currentIndex);
	}

	public static String getPreviousHistoryEntry(){
		if (history.size() == 0) {
			return "";
		}

		if (currentIndex > 0) {
			currentIndex--;
		}

		return  history.get(currentIndex);
	}

	public static void addHistoryEntry(String text)
	{
		history.add(1, text);
		currentIndex=0;
	}

	public static void tempStoreCurrentEntry(String text)
	{
		if (currentIndex == 0) {
			if (history.size() > 0) {
				history.remove(0);
			}
			history.add(0, text);
		}
	}
}
