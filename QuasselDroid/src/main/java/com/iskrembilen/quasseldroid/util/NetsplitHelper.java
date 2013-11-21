package com.iskrembilen.quasseldroid.util;

import java.util.ArrayList;
import java.util.List;

public class NetsplitHelper {
    private static final int maxNetsplitNicks = 15;

    private List<String> nicks = new ArrayList<String>();
    private String sideOne;
    private String sideTwo;

    public NetsplitHelper(String netsplitString) {
        String[] splitString = netsplitString.split("#:#");
        for (int i = 0; i < splitString.length; i++) {
            if (i < splitString.length - 1) {
                nicks.add(splitString[i].split("!")[0]);
            } else if (i == splitString.length - 1) {
                String[] sides = splitString[i].split(" ");
                if (sides.length > 1) {
                    sideOne = sides[0];
                    sideTwo = sides[1];
                }
            }
        }
    }

    public List<String> getNicks() {
        return nicks;
    }

    public String getSideOne() {
        return sideOne;
    }

    public String getSideTwo() {
        return sideTwo;
    }

    public String formatJoinMessage() {
        return "Netsplit between " + sideOne + " and " + sideTwo + " ended. Users joined: " + formatNickList();
    }

    public String formatQuitMessage() {
        return "Netsplit between " + sideOne + " and " + sideTwo + ". Users quit: " + formatNickList();
    }

    private String formatNickList() {
        String nickList = "";

        int i = 0;
        for (; i < nicks.size() && i < maxNetsplitNicks; i++) {
            nickList += nicks.get(i);
            if ((i < nicks.size() - 1) && (i < maxNetsplitNicks - 1)) {
                nickList += ", ";
            }
        }

        if (i == maxNetsplitNicks && nicks.size() > maxNetsplitNicks) {
            nickList += " (" + (nicks.size() - maxNetsplitNicks) + " more)";
        }

        return nickList;
    }
}
