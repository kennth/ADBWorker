package com.funmix.model;


public class ADBWork {
	int id;
	int deviceid;
	String work;
	String keyid;
	String extra;
	int status;
	int spec;
	String area;

	
	public String getKeyid() {
		return keyid;
	}

	public void setKeyid(String keyid) {
		this.keyid = keyid;
	}

	public int getSpec() {
		return spec;
	}

	public void setSpec(int spec) {
		this.spec = spec;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getDeviceid() {
		return deviceid;
	}
	
	public void setDeviceid(int deviceid) {
		this.deviceid = deviceid;
	}
	
	public String getWork() {
		return work;
	}
	
	public void setWork(String work) {
		this.work = work;
	}
	
	public String getExtra() {
		return extra;
	}
	
	public void setExtra(String extra) {
		this.extra = extra;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
}
