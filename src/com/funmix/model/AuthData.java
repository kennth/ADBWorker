package com.funmix.model;

public class AuthData {

	int id;
	String zoneid;
	int platformid;
	String openid;
	String access_token;
	String pay_token;
	String pf;
	String pf_key;
	double subtime;
	double lastupdate;
	String area;
	String imei;
	int phone;
	int ver;
	String ip;
	String info;	
	String amount;
	
	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public double getSubtime() {
		return subtime;
	}

	public void setSubtime(double subtime) {
		this.subtime = subtime;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public int getPhone() {
		return phone;
	}

	public void setPhone(int phone) {
		this.phone = phone;
	}

	public int getVer() {
		return ver;
	}

	public void setVer(int ver) {
		this.ver = ver;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getZoneid() {
		return zoneid;
	}

	public void setZoneid(String zoneid) {
		this.zoneid = zoneid;
	}

	public int getPlatformid() {
		return platformid;
	}

	public void setPlatformid(int platformid) {
		this.platformid = platformid;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getPay_token() {
		return pay_token;
	}

	public void setPay_token(String pay_token) {
		this.pay_token = pay_token;
	}

	public String getPf() {
		return pf;
	}

	public void setPf(String pf) {
		this.pf = pf;
	}

	public String getPf_key() {
		return pf_key;
	}

	public void setPf_key(String pf_key) {
		this.pf_key = pf_key;
	}

	public double getLastupdate() {
		return lastupdate;
	}

	public void setLastupdate(double lastupdate) {
		this.lastupdate = lastupdate;
	}

}
