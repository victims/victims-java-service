package com.redhat.victims.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.redhat.victims.fingerprint.Algorithms;
import com.redhat.victims.fingerprint.Artifact;
import com.redhat.victims.fingerprint.JarFile;
import com.redhat.victims.fingerprint.Key;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class Hash {
	private String hash;
	private String name;
	private String format;
    private List<File> files;
    
    public Hash(String hash, String name, String format, List<String> cves, String submitter, List<File> files) {
    		this.hash = hash;
    		this.name = name;
    		this.format = format;
    		this.files = files;
    }
    
    public Hash(JarFile jarFile) {
    	this.hash = jarFile.getFingerprint().get(Algorithms.SHA512);
    	this.name = jarFile.getFileName();
    	this.format = "SHA512";
    	List<Artifact> contents = (List<Artifact>) jarFile.getRecord().get(Key.CONTENT);
    	this.files = new ArrayList<File>();
		for( Artifact a : contents) {
			Map<Algorithms, String> fingerprint = (Map<Algorithms, String>) a.get(Key.FINGERPRINT);
			if(fingerprint.containsKey(Algorithms.SHA512)) {
				String filename = a.filename();
				//MongoDB doesn't allow field names with '.'
				String withoutExtention = filename.substring(0, filename.lastIndexOf('.'));
				File file = new File(withoutExtention, (String) fingerprint.get(Algorithms.SHA512));
				this.files.add(file);
			}
		}
    }
    
    
    public JsonObject asJson() {
    	return new JsonObject(Json.encode(this));
    }

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}
}
