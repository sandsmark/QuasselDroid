package com.iskrembilen.quasseldroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class NetworkCollection extends Observable implements Observer {
	private static final String TAG = NetworkCollection.class.getSimpleName();
	List<Network> networkList = new ArrayList<Network>();
	
	public void addNetwork(Network network) {
		networkList.add(network);
		network.addObserver(this);
		Collections.sort(networkList);
		setChanged();
		notifyObservers();
	}
	
	public Network getNetwork(int location) {
		return networkList.get(location);
	}
	
	public Buffer getBufferById(int bufferId) {
		for (Network network : networkList) {
			if(network.getStatusBuffer().getInfo().id == bufferId)
				return network.getStatusBuffer();
			if(network.getBuffers().hasBuffer(bufferId)) {
				return network.getBuffers().getBuffer(bufferId);
			}
		}
		return null;
	}
	
	public Network getNetworkById(int networkId) {
		for(Network network : networkList) {
			if(network.getId() == networkId)
				return network;
		}
		return null;
	}
	
	public void addBuffer(Buffer buffer) {
		System.out.println(buffer);
		int id = buffer.getInfo().networkId;
		for(Network network : networkList) {
			if(network.getId() == id) {
				network.addBuffer(buffer);
				return;
			}
		}
		throw new RuntimeException("Buffer + " + buffer.getInfo().name + " has no valide network id " + id);
	}

	public List<Network> getNetworkList() {
		return networkList;
	}

	public int size() {
		return networkList.size();
	}

	@Override
	public void update(Observable observable, Object data) {
		setChanged();
		notifyObservers();
		
	}
}
