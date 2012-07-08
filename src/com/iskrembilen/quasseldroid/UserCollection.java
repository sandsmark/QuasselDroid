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

	public void removeUserWithNick(String nick) {
        for(IrcMode mode: IrcMode.values()){
            try{
                removeUserWithNickFromModeList(mode, nick);
                //Log.e(TAG, "Mode "+mode.modeName+" was removed from user "+nick+".");
            } catch(IllegalArgumentException e) {
                //Log.e(TAG, e.getMessage());
            }
        }
        findUniqueUsersSortedByMode();
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
	}

    private void removeUserWithNickFromModeList(IrcMode mode, String nick) {
        boolean found = false;
        IrcUser userToRemove = null;
        for(IrcUser user : users.get(mode)) {
            if(user.nick.equals(nick) && users.get(mode).contains(user)) {
                found = true;
                userToRemove = user;
                break;
            }
        }
        if(found) {
            users.get(mode).remove(userToRemove);
            if(uniqueUsers.get(mode).contains(userToRemove)){
                uniqueUsers.get(mode).remove(userToRemove);
            }
            this.setChanged();
        } else{
            throw new IllegalArgumentException("User with nick "+nick+" was not found in list for mode "+mode.modeName+".");
        }
    }

    private void removeUserFromModeList(List<IrcUser> list, IrcUser user){
        if(list.contains(user)){
            list.remove(user);
            this.setChanged();
        }else{
            throw new IllegalArgumentException("User "+user.nick+" was not found.");
        }
    }
    public void addUser(IrcUser user, String modes) {
        for(IrcMode mode: IrcMode.values()){
            if(modes.contains(mode.shortModeName)){
                try{
                    addUserToModeList(users.get(mode), user);
                    //Log.e(TAG, "Mode "+mode.modeName+" added to user "+user.nick+".");
                } catch (IllegalArgumentException e){
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        findUniqueUsersSortedByMode();
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
        user.addObserver(this);

    }
    public void addUsers(ArrayList<Pair<IrcUser, String>> usersToAdd) {
        for(Pair<IrcUser, String> user: usersToAdd) {
            for(IrcMode mode: IrcMode.values()){
                if(user.second.contains(mode.shortModeName)){
                    try{
                        addUserToModeList(users.get(mode), user.first);
                        //Log.e(TAG, "Mode "+mode.modeName+" added to user "+user.first.nick+".");
                    } catch (IllegalArgumentException e){
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
            user.first.addObserver(this);
        }
        findUniqueUsersSortedByMode();
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
    }

    private void addUserToModeList(List<IrcUser> list, IrcUser user) {
        if(list.contains(user)){
            throw new IllegalArgumentException("User "+user.nick+" is already in this list.");
        }else{
            list.add(user);
            Collections.sort(list);
            this.setChanged();
        }
    }

	public Map<IrcMode,ArrayList<IrcUser>> getUsers() {
		return users;
	}

    public ArrayList<IrcUser> getUsersWithMode(IrcMode mode){
        return users.get(mode);
    }

    private void findUniqueUsersSortedByMode(){
        /*
        * Because IrcMode.values() starts at the first declaration and moves down,
        * we can be sure that users get added to the list with the highest ranking mode first.
        */
        for(IrcMode mode: IrcMode.values()){
            for(IrcUser user: users.get(mode)){
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
        }
        return found;
    }
    private void removeUserFromLowerRankingMode(IrcMode hasMode, IrcUser user) {
        boolean lowerRank = false;
        for(IrcMode mode: IrcMode.values()) {
            if(lowerRank) {
                if(uniqueUsers.get(mode).contains(user)){
                    //Log.e(TAG, "Removing user "+user.nick+" from mode "+mode.modeName+".");
                    uniqueUsers.get(mode).remove(user);
                }
            }
            if(mode==hasMode) {
                lowerRank = true;
            }
        }
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

    public int getUserCount() {
        //All users have the mode IrcMode.USER
        return users.get(IrcMode.USER).size();
    }

    @Override
	public void update(Observable observable, Object data) {
        for(IrcMode mode: IrcMode.values()){
            if(users.get(mode).contains(observable)){
                Collections.sort(users.get(mode));
                this.setChanged();
            }
        }
        findUniqueUsersSortedByMode();
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);

	}

	public void addUserMode(IrcUser user, String mode) {
		for(IrcMode ircMode: IrcMode.values()){
            if(mode.equals(ircMode.shortModeName)){
                try{
                    addUserToModeList(users.get(ircMode), user);
                    //Log.e(TAG, "Mode " + ircMode.modeName + " added to user " + user.nick);
                    break;
                } catch (IllegalArgumentException e){
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        findUniqueUsersSortedByMode();
		notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
	}

	public void removeModeFromUser(IrcUser user, String mode) {
        if(mode.equals("")){
            throw new IllegalArgumentException("Cannot remove empty mode from user.");
        }
        for(IrcMode ircMode: IrcMode.values()){
            if(mode.equals(ircMode.shortModeName)){
                try{
                    removeUserFromModeList(users.get(ircMode),user);
                    if(uniqueUsers.get(ircMode).contains(user)){
                        uniqueUsers.get(ircMode).remove(user);
                    }
                    //Log.e(TAG, "Mode " + ircMode.modeName + " removed from user " + user.nick+".");
                    break;

                } catch (IllegalArgumentException e){
                    //Log.e(TAG, e.getMessage());
                }
            }
        }
        findUniqueUsersSortedByMode();
		notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
	}
}
