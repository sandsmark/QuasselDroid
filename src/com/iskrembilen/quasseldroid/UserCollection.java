package com.iskrembilen.quasseldroid;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.util.Log;

import com.iskrembilen.quasseldroid.BufferInfo.Type;

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
			users.add(user);
			Collections.sort(users);
			this.setChanged();
		}
		for(int i=0;i<modes.length();i++) {
			String mode = Character.toString(modes.charAt(i));
			if(mode.equals("v")) {
				if(operators.contains(user))
					voicedAndOp.add(user);
				else {
					voiced.add(user);
					Collections.sort(voiced);
				}
			}else if(mode.equals("o")) {
				if(voiced.contains(user)) {
					voiced.remove(user);
					voicedAndOp.add(user);
				}
				operators.add(user);
				Collections.sort(operators);
			}else {
				Log.e(TAG, "Unknown usermode " + mode + " for user " + user.name);
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
}
