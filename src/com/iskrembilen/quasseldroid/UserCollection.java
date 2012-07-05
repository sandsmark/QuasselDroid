package com.iskrembilen.quasseldroid;

import android.util.Log;

import java.util.*;

public class UserCollection extends Observable implements Observer {

	private static final String TAG = UserCollection.class.getSimpleName();
	private List<IrcUser> users;
	private List<IrcUser> voiced;
	private List<IrcUser> operators;

	private List<IrcUser> voicedAndOp; //Gay list to handle that some tards have both voice and op

	public UserCollection() {
		users = new ArrayList<IrcUser>();
		voiced = new ArrayList<IrcUser>();
		operators = new ArrayList<IrcUser>();
		voicedAndOp = new ArrayList<IrcUser>();
	}

	/**
	 * Remove a specific nick from the nick list
	 * @param nick the nick to remove
	 */
	public void removeNick(String nick) {
		removeNickIfExistsFromList(nick, users);
		removeNickIfExistsFromList(nick, voiced);
		removeNickIfExistsFromList(nick, operators);
		removeNickIfExistsFromList(nick, voicedAndOp);
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
	 * @param nick the nick to add
	 */
	public void addUser(IrcUser user, String modes) {
		if(modes.equals("")) {
			addUserIfNotAlreadyIn(users, user);
			Collections.sort(users);
			this.setChanged();
		}
		for(int i=0;i<modes.length();i++) {
			String mode = Character.toString(modes.charAt(i));
			if(mode.equals("v")) {
				if(operators.contains(user))
					addUserIfNotAlreadyIn(voicedAndOp, user);
				else {
					addUserIfNotAlreadyIn(voiced, user);
					Collections.sort(voiced);
				}
			}else if(mode.equals("o")) {
				if(voiced.contains(user)) {
					voiced.remove(user);
					addUserIfNotAlreadyIn(voicedAndOp, user);
				}
				addUserIfNotAlreadyIn(operators, user);
				Collections.sort(operators);
			}else {
				Log.e(TAG, "Unknown usermode " + mode + " for user " + user.nick);
			}
			user.addObserver(this);
			this.setChanged();
		}
		notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
	}

	public List<IrcUser> getUsers() {
		return users;
	}

	public List<IrcUser> getVoiced() {
		return voiced;
	}

	public List<IrcUser> getOperators() {
		return operators;
	}

	@Override
	public void update(Observable observable, Object data) {
		Collections.sort(users);
		Collections.sort(voiced);
		Collections.sort(operators);
		this.setChanged();
		notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);

	}

	public void addUserMode(IrcUser user, String mode) {
		if(mode.equals("o")) {
			operators.add(user);
			Collections.sort(operators);
			if(voiced.contains(user)) {
				addUserIfNotAlreadyIn(voicedAndOp, user);
				voiced.remove(user);
			}
			this.setChanged();
		} else if(mode.equals("v")) {
			if(operators.contains(user)) {
				addUserIfNotAlreadyIn(voicedAndOp, user);
			}else {
				addUserIfNotAlreadyIn(voiced, user);
				Collections.sort(voiced);
				this.setChanged();
			}
		} else {
			Log.e(TAG, "Unknown user mode " + mode);
			return;
		}
		users.remove(user);
		notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
	}

	public void removeUserMode(IrcUser user, String mode) {
		if(mode.equals("o")) {
			operators.remove(user);
			if(voicedAndOp.contains(user)) {
				addUserIfNotAlreadyIn(voiced, user);
				voicedAndOp.remove(user);
			}else if(!voiced.contains(user)) {
				addUserIfNotAlreadyIn(users, user);
			}
			this.setChanged();
		}else if(mode.equals("v")) {
			if(!voicedAndOp.remove(user)){
				voiced.remove(user);
				addUserIfNotAlreadyIn(users, user);
				Collections.sort(users);
				this.setChanged();
			}
		}else {
			Log.e(TAG, "Unknown user mode " + mode);
			return;
		}
		
		notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
	}
	
	public int getUserCount() {
		return operators.size() + voiced.size() + users.size();
	}
}
