package com.iskrembilen.quasseldroid;

import java.util.*;

public class Network extends Observable implements Observer, Comparable<Network> {
	public enum ConnectionState {
		Disconnected(0),
		Connecting(1),
		Initializing(2),
		Initialized(3),
		Reconnecting(4),
		Disconnecting(5);
		int value;
		static final Map<Integer, ConnectionState> intToStateMap = new HashMap<Integer, ConnectionState>();
		static {
			for (ConnectionState type : ConnectionState.values()) {
				intToStateMap.put(type.value, type);
			}
		}
		public static ConnectionState getForValue(int value) {
			return intToStateMap.get(value);
		}
		ConnectionState(int value){
			this.value = value;
		}
		public int getValue(){
			return value;
		}

	}

	private int networkId;
	private Buffer statusBuffer;
	private String networkName;
	private BufferCollection buffers;
	private List<IrcUser> userList;
	private HashMap<String, IrcUser> nickUserMap;
	private String nick;
	private boolean open;
	private ConnectionState connectionState;
	private Boolean isConnected;
	private int latency;
	private String server;

	public Network(int networkId) {
		this.networkId = networkId;
		userList = new ArrayList<IrcUser>();
		buffers = new BufferCollection();
		buffers.addObserver(this);
		nickUserMap = new HashMap<String, IrcUser>();
		open=false;
		connectionState = ConnectionState.Disconnected;
		isConnected = false;
		latency = 0;
	}

	public Buffer getStatusBuffer() {
		return statusBuffer;
	}

	public void setStatusBuffer(Buffer statusBuffer) {
		this.statusBuffer = statusBuffer;
        statusBuffer.addObserver(this);
		this.setChanged();
		notifyObservers();
	}

	public int getId() {
		return networkId;
	}

	public BufferCollection getBuffers() {
		return buffers;
	}


	public void addBuffer(Buffer buffer) {
		buffers.addBuffer(buffer);
	}


	public void setName(String networkName) {
		this.networkName = networkName;
	}


	public String getName() {
		return networkName;
	}

	public Boolean isConnected() {
		return isConnected;
	}


	public void setUserList(List<IrcUser> userList) {
		if(userList != null && userList.size() > 0) {
			for(IrcUser user: userList) {
				user.deleteObserver(this);
			}
		}
		this.userList = userList;
		nickUserMap.clear();
		for(IrcUser user: userList) {
			nickUserMap.put(user.nick, user);
			user.addObserver(this);
		}
	}


	public List<IrcUser> getUserList() {
		return userList;
	}


	@Override
	public void update(Observable observable, Object data) {
		if(data != null && ((Integer)data == R.id.USER_CHANGEDNICK)) {
			IrcUser changedUser = (IrcUser)observable; 
			for(Map.Entry<String,IrcUser> entry : nickUserMap.entrySet()) {
				if(entry.getValue().nick.equals(changedUser.nick)) {
					nickUserMap.remove(entry.getKey());
					nickUserMap.put(entry.getValue().nick, entry.getValue());
					break;
				}
			}
		}
		setChanged();
		notifyObservers();

	}


	@Override
	public int compareTo(Network another) {
		return getName().compareTo(another.getName());
	}


	public void setOpen(boolean open) {
		this.open = open;
	}


	public boolean isOpen() {
		return open;
	}


	public String getNick() {
		return nick;
	}


	public void setNick(String nick) {
		this.nick = nick;
	}

	public void onUserJoined(IrcUser user) {
		userList.add(user);
		nickUserMap.put(user.nick, user);
		user.addObserver(this);
	}


	public void onUserQuit(String nick) {
		nickUserMap.remove(nick);
		for(IrcUser user: userList) {
			if(user.nick.equals(nick)) {
				for(Buffer buffer : buffers.getRawBufferList()) {
					if(user.channels.contains(buffer.getInfo().name)) {
						buffer.getUsers().removeUserByNick(nick);
					}
				}
				userList.remove(user);
				user.deleteObserver(this);
				return;
			}
		}
	}


	public void onUserParted(String nick, String bufferName) {
		for(IrcUser user: userList) {
			if(user.nick.equals(nick) && user.channels.contains(bufferName)) {
				user.channels.remove(bufferName);
				break;
			}
		}
		for(Buffer buffer : buffers.getRawBufferList()) {
			if(buffer.getInfo().name.equalsIgnoreCase(bufferName)) {
				buffer.getUsers().removeUserByNick(nick);
				if(nick.equalsIgnoreCase(getNick())) {
					buffer.setActive(false);
				}
				break;
			}
		}
	}


	public boolean hasNick(String nick) {
		return nickUserMap.containsKey(nick);
	}


	public IrcUser getUserByNick(String nick) {
		return nickUserMap.get(nick);
	}

	public boolean containsBuffer(int id) {
		return buffers.hasBuffer(id);
	}


	public int getBufferCount() {
		return buffers.getBufferCount();
	}


	public void setConnectionState(ConnectionState state) {
		this.connectionState = state;
	}


	public void setConnected(Boolean connected) {
		if(connected) {
			setOpen(true);
			if(statusBuffer != null) statusBuffer.setActive(true);
			
		} else {
			setOpen(false);
			if(statusBuffer != null) statusBuffer.setActive(false);
			for(Buffer buffer : buffers.getRawBufferList()) {
				buffer.setActive(false);
			}
		}
		this.isConnected = connected;
		this.setChanged();
		notifyObservers();
	}


	public void removeBuffer(int bufferId) {
		buffers.removeBuffer(bufferId);
		
	}

	public void setLatency(int latency) {
        this.latency = latency;
    }

    public int getLatency() {
        return latency;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getServer() {
        return server;
    }

    public int getCountUsers() {
        return userList.size();
    }

}
