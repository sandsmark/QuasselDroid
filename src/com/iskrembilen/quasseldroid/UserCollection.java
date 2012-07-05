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

	/**
	 * Remove a specific nick from the nick list
	 * @param nick the nick to remove
	 */
	public void removeNick(String nick) {
        for(ArrayList<IrcUser> list: users.values()){
            removeNickIfExistsFromList(nick, list);
        }
	}

	private void addUserIfNotAlreadyIn(List<IrcUser> list, IrcUser user) {
		if(list.contains(user)) return;
		list.add(user);
	}

	private boolean removeNickIfExistsFromList(String nick, List<IrcUser> list) {
		for(IrcUser user : list) {
			if(user.nick.equals(nick)) {
				list.remove(user);
				setChanged();
				notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
				return true;
			}
		}
		return false;
	}

	/**
	 * Add a specific nick to the nick list
	 * @param user the user to add
     * @param modes the modes of the user
	 */
	public void addUser(IrcUser user, String modes) {
        for(IrcMode mode: IrcMode.values()){
            if(modes.contains(mode.shortModeName)){
                addUserIfNotAlreadyIn(users.get(mode), user);
                Collections.sort(users.get(mode));
                this.setChanged();
                Log.e(TAG, "User added with user mode " + mode.modeName + " for user " + user.nick);
            }
        }
        user.addObserver(this);
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
	}

	public Map<IrcMode,ArrayList<IrcUser>> getUsers() {
		return users;
	}

    public ArrayList<IrcUser> getUsersWithMode(IrcMode mode){
        return users.get(mode);
    }

    public Map<IrcMode, ArrayList<IrcUser>> getUniqueUsers(){
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
            found = isIrcUserInList(user, listOfAlreadyAddedUsers);
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

    @Override
	public void update(Observable observable, Object data) {
        System.err.println(observable.toString());
        for(IrcMode mode: IrcMode.values()){
            if(users.get(mode).contains(observable)){
                System.err.println("Found the nick "+((IrcUser)observable).nick+" in the list of "+mode.modeName+".");
                Collections.sort(users.get(mode));
            }
        }
        this.setChanged();
        notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);

	}

	public void addUserMode(IrcUser user, String mode) {
		for(IrcMode ircMode: IrcMode.values()){
            if(mode.equals(ircMode.shortModeName)){
                addUserIfNotAlreadyIn(users.get(mode), user);
                Collections.sort(users.get(mode));
                this.setChanged();
                Log.e(TAG, "User added with user mode " + ircMode.modeName + " for user " + user.nick);
                break;
            }
        }
		notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
	}

	public void removeUserMode(IrcUser user, String mode) {
        if(mode.equals("")){
            Log.e(TAG, "Cannot remove mode from user since mode is empty");
            return;
        }
        for(IrcMode ircMode: IrcMode.values()){
            if(mode.equals(ircMode.shortModeName)){
                if(users.get(ircMode).contains(user)){
                    users.get(ircMode).remove(user);
                }
                this.setChanged();
                Log.e(TAG, "User removed with user mode " + ircMode.modeName + " for user " + user.nick);
                break;
            }
        }
		
		notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
	}
	
	public int getUserCount() {
		return users.get(IrcMode.USER).size();
	}
}
