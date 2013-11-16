package com.iskrembilen.quasseldroid;

import android.util.Log;
import android.util.Pair;

import java.util.*;

public class UserCollection extends Observable implements Observer {

	private static final String TAG = UserCollection.class.getSimpleName();
	private Map<IrcMode, ArrayList<IrcUser>> users;
    private Map<IrcMode, ArrayList<IrcUser>> uniqueUsers;

	public UserCollection() {
		users = new HashMap<IrcMode, ArrayList<IrcUser>>();
        uniqueUsers = new HashMap<IrcMode, ArrayList<IrcUser>>();
        for(IrcMode mode: IrcMode.values()){
            users.put(mode, new ArrayList<IrcUser>());
            uniqueUsers.put(mode, new ArrayList<IrcUser>());
        }
	}

    public void addUser(IrcUser user, String modes) {
        for(IrcMode mode: IrcMode.values()){
            if(modes.contains(mode.shortModeName)){
                if(addUserToModeList(mode, user)){
                    //Log.e(TAG, "Mode "+mode.modeName+" added to user "+user.nick+".");
                } else{
                    //Log.e(TAG, "User "+user.nick+" already has mode "+mode.modeName+".");
                }
            }
        }
        updateUniqueUsersSortedByMode();
        user.addObserver(this);
        update(null,null);
    }

    public void addUsers(ArrayList<Pair<IrcUser, String>> usersWithModes) {
        for(Pair<IrcUser, String> user: usersWithModes) {
            for(IrcMode mode: IrcMode.values()){
                if(user.second.contains(mode.shortModeName)){
                    if(addUserToModeList(mode, user.first)){
                        //Log.e(TAG, "Mode "+mode.modeName+" added to user "+user.first.nick+".");
                    } else {
                        //Log.e(TAG, "User "+user.nick+" already has mode "+mode.modeName+".");
                    }
                }
            }
            user.first.addObserver(this);
        }
        updateUniqueUsersSortedByMode();
        update(null,null);
    }

    private boolean addUserToModeList(IrcMode mode, IrcUser user) {
        if(users.get(mode).contains(user)){
            return false;
        }else{
            users.get(mode).add(user);
            Collections.sort(users.get(mode));
            this.setChanged();
            return true;
        }
    }

    public void removeUser(IrcUser user) {
        for(IrcMode mode: IrcMode.values()){
            if(removeUserFromModeList(mode, user)){
                //Log.e(TAG, "Mode "+mode.modeName+" was removed from user "+nick+".");
            } else {
                //Log.e(TAG, "User "+user.nick+" was not found with mode "+mode.modeName+".");
            }
        }
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
    }

    public void removeUsers(ArrayList<IrcUser> users) {
        for(IrcUser user: users){
            for(IrcMode mode: IrcMode.values()){
                if(removeUserFromModeList(mode, user)){
                    //Log.e(TAG, "Mode "+mode.modeName+" was removed from user "+nick+".");
                } else {
                    //Log.e(TAG, "User "+user.nick+" was not found with mode "+mode.modeName+".");
                }
            }
        }
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
    }

    public void removeUserByNick(String nick) {
        for(IrcMode mode: IrcMode.values()){
            for(IrcUser user: users.get(mode)){
                if(user.nick.equals(nick)){
                    if(removeUserFromModeList(mode, user)){
                        //Log.e(TAG, "Mode "+mode.modeName+" was removed from user "+nick+".");
                        break;
                    } else {
                        //Log.e(TAG, "User "+user.nick+" was not found with mode "+mode.modeName+".");
                    }
                }
            }
        }
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
    }

    public void removeUsersByNick(ArrayList<String> nicks) {
        for(String nick: nicks){
            for(IrcMode mode: IrcMode.values()){
                for(IrcUser user: users.get(mode)){
                    if(user.nick.equals(nick)){
                        if(removeUserFromModeList(mode, user)){
                            //Log.e(TAG, "Mode "+mode.modeName+" was removed from user "+nick+".");
                            break;
                        } else {
                            //Log.e(TAG, "User "+user.nick+" was not found with mode "+mode.modeName+".");
                        }
                    }
                }
            }
        }
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);

    }

    private boolean removeUserFromModeList(IrcMode mode, IrcUser user){
        if(users.get(mode).remove(user)){
            uniqueUsers.get(mode).remove(user);
            this.setChanged();
            return true;
        } else {
            return false;
        }
    }

    public void addModeToUser(IrcUser user, String mode) {
        for(IrcMode ircMode: IrcMode.values()){
            if(mode.equals(ircMode.shortModeName)){
                if(addUserToModeList(ircMode, user)){
                    //Log.e(TAG, "Mode " + ircMode.modeName + " added to user " + user.nick);
                    break;
                } else {
                    //Log.e(TAG, "User "+user.nick+" already has mode "+mode.modeName+".");
                }
            }
        }
        updateUniqueUsersSortedByMode();
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
    }

    public void removeModeFromUser(IrcUser user, String mode) {
        if(mode.equals("")){
            //Log.e(TAG,"Cannot remove empty mode from user "+user.nick+".");
            return;
        }
        for(IrcMode ircMode: IrcMode.values()){
            if(mode.equals(ircMode.shortModeName)){
                if(removeUserFromModeList(ircMode,user)){
                    //Log.e(TAG, "Mode " + ircMode.modeName + " removed from user " + user.nick+".");
                    break;
                } else {
                    //Log.e(TAG, "User "+user.nick+" was not found with mode "+mode.modeName+".");
                }
            }
        }
        updateUniqueUsersSortedByMode();
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
    }

    public ArrayList<IrcUser> getUniqueUsers(){
        /*
        * Because IrcMode.values() starts at the first declaration and moves down,
        * we can be sure that users get added to the list with the highest ranking mode first.
        */
        ArrayList<IrcUser> uniqueUsers = new ArrayList<IrcUser>();
        for(IrcMode mode: IrcMode.values()){
            for(IrcUser user: users.get(mode)){
                if(!uniqueUsers.contains(user)){
                    uniqueUsers.add(user);
                }
            }
        }
        return uniqueUsers;
    }

    public ArrayList<IrcUser> getUniqueUsersWithMode(IrcMode mode) {
        return uniqueUsers.get(mode);
    }

    private void updateUniqueUsersSortedByMode(){
        /*
        * Because IrcMode.values() starts at the first declaration and moves down,
        * we can be sure that users get added to the list with the highest ranking mode first.
        */
        for(IrcMode mode: IrcMode.values()){
            for(IrcUser user: users.get(mode)){
                //Log.e(TAG, "Checking user "+user.nick+" for mode "+mode.modeName+".");
                if(!isIrcUserAlreadyAddedWithAHigherRankingMode(mode, user)){
                    //Log.e(TAG, "Adding unique user "+user.nick+" with mode "+mode.modeName+".");
                    uniqueUsers.get(mode).add(user);
                    removeUserFromLowerRankingMode(mode, user);
                }
            }
        }
    }

    private boolean isIrcUserAlreadyAddedWithAHigherRankingMode(IrcMode currentMode, IrcUser user) {
        boolean found = false;
        for(IrcMode mode: IrcMode.values()){
            if(uniqueUsers.get(mode).contains(user)) {
                //Log.e(TAG, "Found user "+user.nick+" in the list for mode "+mode.modeName+".");
                found = true;
                break;
            }
            if(mode==currentMode){
                break;
            }
        }
        return found;
    }

    private void removeUserFromLowerRankingMode(IrcMode hasMode, IrcUser user) {
        boolean lowerRank = false;
        for(IrcMode mode: IrcMode.values()) {
            if(lowerRank) {
                uniqueUsers.get(mode).remove(user);
            }
            if(mode==hasMode) {
                lowerRank = true;
            }
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        for(IrcMode mode: IrcMode.values()){
            Collections.sort(users.get(mode));
            Collections.sort(uniqueUsers.get(mode));
            this.setChanged();
        }
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
    }
}
