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

package com.iskrembilen.quasseldroid.protocol.state;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.iskrembilen.quasseldroid.R;

import java.util.*;

public class UserCollection extends Observable implements Observer {

    private static final String TAG = UserCollection.class.getSimpleName();
    private Map<IrcMode, ArrayList<IrcUser>> users = new HashMap<>();
    private Map<IrcMode, ArrayList<IrcUser>> uniqueUsers = new HashMap<>();

    public UserCollection() {
        for (IrcMode mode : IrcMode.values()) {
            users.put(mode, new ArrayList<IrcUser>());
            uniqueUsers.put(mode, new ArrayList<IrcUser>());
        }
    }

    public void addUser(@NonNull IrcUser user, @NonNull String modes) {
        for (IrcMode mode : IrcMode.values()) {
            if (modes.contains(mode.shortModeName)) {
                addUserToModeList(mode, user);
            }
        }
        updateUniqueUsersSortedByMode();
        user.addObserver(this);
        update(null, null);
    }

    public void addUsers(List<Pair<IrcUser, String>> usersWithModes) {
        for (Pair<IrcUser, String> user : usersWithModes) {
            for (IrcMode mode : IrcMode.values()) {
                if (user.second.contains(mode.shortModeName)) {
                    addUserToModeList(mode, user.first);
                }
            }
            user.first.addObserver(this);
        }
        updateUniqueUsersSortedByMode();
        update(null, null);
    }

    private boolean addUserToModeList(IrcMode mode, IrcUser user) {
        if (user==null) {
            Log.e(TAG, "NULL user added with mode " + mode.name());
            return false;
        } else if (users.get(mode).contains(user)) {
            return false;
        } else {
            users.get(mode).add(user);
            Collections.sort(users.get(mode));
            this.setChanged();
            return true;
        }
    }

    public void removeUser(IrcUser user) {
        for (IrcMode mode : IrcMode.values()) {
            removeUserFromModeList(mode, user);
        }
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
    }

    public void removeUsers(List<IrcUser> users) {
        for (IrcUser user : users) {
            for (IrcMode mode : IrcMode.values()) {
                removeUserFromModeList(mode, user);
            }
        }
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
    }

    public void removeUserByNick(String nick) {
        for (IrcMode mode : IrcMode.values()) {
            for (IrcUser user : users.get(mode)) {
                if (user.nick.equals(nick)) {
                    if (removeUserFromModeList(mode, user)) {
                        break;
                    }
                }
            }
        }
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
    }

    public void removeUsersByNick(List<String> nicks) {
        for (String nick : nicks) {
            for (IrcMode mode : IrcMode.values()) {
                for (IrcUser user : users.get(mode)) {
                    if (user.nick.equals(nick)) {
                        if (removeUserFromModeList(mode, user)) {
                            break;
                        }
                    }
                }
            }
        }
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);

    }

    private boolean removeUserFromModeList(IrcMode mode, IrcUser user) {
        if (users.get(mode).remove(user)) {
            uniqueUsers.get(mode).remove(user);
            this.setChanged();
            return true;
        } else {
            return false;
        }
    }

    public void addModeToUser(IrcUser user, String mode) {
        for (IrcMode ircMode : IrcMode.values()) {
            if (mode.equals(ircMode.shortModeName)) {
                if (addUserToModeList(ircMode, user)) {
                    break;
                }
            }
        }
        updateUniqueUsersSortedByMode();
        update(null,null);
    }

    public void removeModeFromUser(IrcUser user, String mode) {
        if (mode.equals("")) {
            //Log.e(TAG,"Cannot remove empty mode from user "+user.myNick+".");
            return;
        }
        for (IrcMode ircMode : IrcMode.values()) {
            if (mode.equals(ircMode.shortModeName)) {
                if (removeUserFromModeList(ircMode, user)) {
                    break;
                }
            }
        }
        updateUniqueUsersSortedByMode();
        update(null,null);
    }

    public ArrayList<IrcUser> getUniqueUsers() {
        /*
        * Because IrcMode.values() starts at the first declaration and moves down,
        * we can be sure that users get added to the list with the highest ranking mode first.
        */
        ArrayList<IrcUser> uniqueUsers = new ArrayList<IrcUser>();
        for (IrcMode mode : IrcMode.values()) {
            for (IrcUser user : users.get(mode)) {
                if (!uniqueUsers.contains(user)) {
                    uniqueUsers.add(user);
                }
            }
        }
        return uniqueUsers;
    }

    public ArrayList<IrcUser> getUniqueUsersWithMode(IrcMode mode) {
        return uniqueUsers.get(mode);
    }

    private void updateUniqueUsersSortedByMode() {
        /*
        * Because IrcMode.values() starts at the first declaration and moves down,
        * we can be sure that users get added to the list with the highest ranking mode first.
        */
        for (IrcMode mode : IrcMode.values()) {
            for (IrcUser user : users.get(mode)) {
                //Log.e(TAG, "Checking user "+user.myNick+" for mode "+mode.modeName+".");
                if (!isIrcUserAlreadyAddedWithAHigherRankingMode(mode, user)) {
                    //Log.e(TAG, "Adding unique user "+user.myNick+" with mode "+mode.modeName+".");
                    uniqueUsers.get(mode).add(user);
                    removeUserFromLowerRankingMode(mode, user);
                }
            }
        }
    }

    private boolean isIrcUserAlreadyAddedWithAHigherRankingMode(IrcMode currentMode, IrcUser user) {
        boolean found = false;
        for (IrcMode mode : IrcMode.values()) {
            if (uniqueUsers.get(mode).contains(user)) {
                //Log.e(TAG, "Found user "+user.myNick+" in the list for mode "+mode.modeName+".");
                found = true;
                break;
            }
            if (mode == currentMode) {
                break;
            }
        }
        return found;
    }

    private void removeUserFromLowerRankingMode(IrcMode hasMode, IrcUser user) {
        boolean lowerRank = false;
        for (IrcMode mode : IrcMode.values()) {
            if (lowerRank) {
                uniqueUsers.get(mode).remove(user);
            }
            if (mode == hasMode) {
                lowerRank = true;
            }
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        for (IrcMode mode : IrcMode.values()) {
            Collections.sort(users.get(mode));
            Collections.sort(uniqueUsers.get(mode));
            this.setChanged();
        }
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
    }

    public IrcMode getMode(IrcUser user) {
        for (IrcMode mode : IrcMode.values()) {
            if (users.get(mode).contains(user))
                return mode;
        }
        return IrcMode.USER;
    }
}
