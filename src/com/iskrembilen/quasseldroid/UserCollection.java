package com.iskrembilen.quasseldroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.util.Log;

import com.iskrembilen.quasseldroid.BufferInfo.Type;

public class UserCollection extends Observable implements Observer {

	private List<IrcUser> users;
	private List<IrcUser> voiced;
	private List<IrcUser> operators;
	
	public UserCollection() {
		users = new ArrayList<IrcUser>();
		voiced = new ArrayList<IrcUser>();
		operators = new ArrayList<IrcUser>();
	}

	/**
	 * Remove a specific nick from the nick list
	 * @param nick the nick to remove
	 */
	public void removeNick(String nick) {
		if (removeNickIfExistsFromList(nick, users)) return;
		if (removeNickIfExistsFromList(nick, voiced)) return;
		if (removeNickIfExistsFromList(nick, operators)) return;
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
	public void addUser(IrcUser user, UserMode mode) {
		switch (mode) {
		case USER:
			users.add(user);
			Collections.sort(users);
			break;
		case VOICED:
			voiced.add(user);
			Collections.sort(voiced);
			break;
		case OPERATOR:
			operators.add(user);
			Collections.sort(operators);
			break;
		}
		user.addObserver(this);
		setChanged();
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

	public enum UserMode {
		OPERATOR("o"), VOICED("v"), USER("");
		
		String value;
		
		UserMode(String mode) {
			this.value = mode;
		}
		public String getValue(){
			return value;
		}
		public static UserMode getUserMode(String value) {
			if(value.length()>1) {
				Log.d("UserMode", "UserMode is not 1 char, WTF is " + value);
				value = value.substring(0,1);
			}
			for (UserMode t: values()) {
				if (t.value.equals(value))
					return t;
			}
			throw new IllegalArgumentException("UserMode " + value + " was not recognized");
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		Collections.sort(users);
		Collections.sort(voiced);
		Collections.sort(operators);
		this.setChanged();
		notifyObservers(R.id.BUFFERUPDATE_USERSCHANGED);
		
	}
}
