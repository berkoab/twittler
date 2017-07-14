package com.berko.tweetsaroundyou;

import java.text.DecimalFormat;

public class Tweet implements Comparable<Tweet> {
	private String id;
	private String text;
	private String from;
	private String name;
	private double distance;
	
	public Tweet(Long id, String text, String from) {
		this.id = String.valueOf(id);
		this.text = text;
		this.from = "@"+from;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = String.valueOf(id);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = "@"+from;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(Tweet tweet) {
		if(Long.valueOf(this.id)<Long.valueOf(tweet.getId())) {
			return -1;
		}
		return 0;
	}

	public String getDistance() {
		return String.valueOf(distance) + " miles away";
	}

	public void setDistance(Double distance) {
		this.distance = roundTwoDecimals(distance);
	}
	
	private double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
	}
}
