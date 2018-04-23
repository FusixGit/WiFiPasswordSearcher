package com.example.WiFiPasswordSearcher;

public class ItemWps {
	/**
	 * Заголовок
	 */
	String pin;
	String metod;
	String score;
	String db;
	
	ItemWps(String h, String s, String c, String d){
		this.pin=h;
		this.metod=s;
		this.score=c;
		this.db=d;
	}
	
	//Всякие гетеры и сеттеры
	public String getHeader() {
		return pin;
	}
	public void setHeader(String header) {
		this.pin = header;
	}
	public String getSubHeader() {
		return metod;
	}
	public void setSubHeader(String subHeader) {
		this.metod = subHeader;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	public String getDb() {
		return db;
	}
	public void setDb(String db) {
		this.db = db;
	}
	
}
