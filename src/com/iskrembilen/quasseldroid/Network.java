package com.iskrembilen.quasseldroid;

import java.util.ArrayList;
import java.util.List;

public class Network {
	private int networkId;
	private Buffer statusBuffer;
	private String networkName;
	private Boolean isConnected;
	private BufferCollection buffers;
	private List<IrcUser> userList;

	


	public Network(int networkId) {
		this.networkId = networkId;
		userList = new ArrayList<IrcUser>();
		buffers = new BufferCollection();
	}
	
	
	public Buffer getStatusBuffer() {
		return statusBuffer;
	}

	public void setStatusBuffer(Buffer statusBuffer) {
		this.statusBuffer = statusBuffer;
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
		statusBuffer.getInfo().name = networkName;
	}


	public String getName() {
		return networkName;
	}


	public void setConnected(Boolean isConnected) {
		this.isConnected = isConnected;
	}


	public Boolean isConnected() {
		return isConnected;
	}


	public void setUserList(List<IrcUser> userList) {
		this.userList = userList;
	}


	public List<IrcUser> getUserList() {
		return userList;
	}
	
	

	

}
