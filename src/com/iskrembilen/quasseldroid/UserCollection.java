package com.iskrembilen.quasseldroid;

import android.util.Log;

import java.util.*;

public class UserCollection extends Observable implements Observer {

	private static final String TAG = UserCollection.class.getSimpleName();
	private Map<IrcMode, ArrayList<IrcUser>> users;

	public UserCollection() {
		users = new HashMap<IrcMode, ArrayList<IrcUser>>();
        for(IrcMode mode: IrcMode.values()){
            users.put(mode, new ArrayList<IrcUser>());
        }
	}

	public void removeUserWithNick(String nick) {
        for(IrcMode mode: IrcMode.values()){
            try{
                removeUserWithNickFromModeList(mode, nick);
                Log.e(TAG, "Mode "+mode.modeName+" was removed from user "+nick+".");
            } catch(IllegalArgumentException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
	}

    private void removeUserWithNickFromModeList(IrcMode mode, String nick) {
        for(IrcUser user : users.get(mode)) {
            if(user.nick.equals(nick) && users.get(mode).contains(user)) {
                users.get(mode).remove(user);
                this.setChanged();
            }else{
                throw new IllegalArgumentException("User with nick "+nick+" was not found in list for mode "+mode.modeName+".");
            }
        }
    }

    public void removeUser(IrcUser user){
        for(IrcMode mode: IrcMode.values()){
            try{
                removeUserFromModeList(users.get(mode), user);
                Log.e(TAG,  "Mode "+mode.modeName+" removed from user "+user.nick+".");
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
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
                    Log.e(TAG, "Mode "+mode.modeName+" added to user "+user.nick+".");
                } catch (IllegalArgumentException e){
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        user.addObserver(this);
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

    public Map<IrcMode, ArrayList<IrcUser>> getUniqueUsersSortedByMode(){
        /*
        * Because IrcMode.values() starts at the first declaration and moves down,
        * we can be sure that users get added to the list with the highest ranking mode first.
        */
        Map<IrcMode, ArrayList<IrcUser>> uniqueUsers = new HashMap<IrcMode, ArrayList<IrcUser>>();
        for(IrcMode mode: IrcMode.values()){
            uniqueUsers.put(mode,new ArrayList<IrcUser>());
            for(IrcUser user: users.get(mode)){
                if(!isIrcUserAlreadyAdded(uniqueUsers, user)){
                    uniqueUsers.get(mode).add(user);
                }
            }
        }
        return uniqueUsers;
    }

    private boolean isIrcUserAlreadyAdded(Map<IrcMode, ArrayList<IrcUser>> uniqueUsers, IrcUser user) {
        boolean found = false;
        for(ArrayList<IrcUser> listOfAlreadyAddedUsers: uniqueUsers.values()){
            if(isIrcUserInList(user, listOfAlreadyAddedUsers)){
                found = true;
            }
        }
        return found;
    }

    private boolean isIrcUserInList(IrcUser user, ArrayList<IrcUser> listOfAlreadyAddedUsers) {
        for(IrcUser alreadyAddedUser: listOfAlreadyAddedUsers){
            if (alreadyAddedUser == user) {
                return true;
            }
        }
        return false;
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
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);

	}

	public void addUserMode(IrcUser user, String mode) {
		for(IrcMode ircMode: IrcMode.values()){
            if(mode.equals(ircMode.shortModeName)){
                try{
                    addUserToModeList(users.get(ircMode), user);
                    Log.e(TAG, "Mode " + ircMode.modeName + " added to user " + user.nick);
                    break;
                } catch (IllegalArgumentException e){
                    Log.e(TAG, e.getMessage());
                }
            }
        }
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
                    Log.e(TAG, "Mode " + ircMode.modeName + " removed from user " + user.nick+".");
                    break;

                } catch (IllegalArgumentException e){
                    Log.e(TAG, e.getMessage());
                }
            }
        }
		notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
	}
}
