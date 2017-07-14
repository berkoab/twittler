package com.berko.tweetsaroundyou.managers;

import java.io.Serializable;

public class GeoPoint implements Serializable
{
  private static final long serialVersionUID = 1L;

  private double lat;
	private double lng;

	protected GeoPoint()
	{}

	public GeoPoint(double lat, double lng)
	{
		super();
		this.lat = lat;
		this.lng = lng;
	}

	public double getLat()
	{
		return lat;
	}

	public double getLng()
	{
		return lng;
	}

//	@Override
//	public String toString()
//	{
//		return ToStringBuilder.reflectionToString(this);
//	}
}
