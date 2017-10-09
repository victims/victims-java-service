package com.redhat.victims.domain;

public class File {
	private String name;
	private String hash;
	
	public File(String name, String hash) {
		this.name = name;
		this.hash = hash;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
}
