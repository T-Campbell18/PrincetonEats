import java.util.*;
import javax.print.*;

public class Restaurant
{
	private String name;
	private double rating;
	private String address;
	private String number;
	private TreeSet<String> tags;
	private String picture;
	private String menu;
	private String website;
	private double lon;
	private double lat;


	public Restaurant(String name, String addy, String phone, String menu, String web, double r, String tag, String pic)
	{
		this.name = name;
		this.address = addy;
		this.number = phone;
		this.menu = menu;
		this.website = web;
		this.picture = pic;
		rating = r;
		String[] arr = tag.split(",");
		List<String> list = Arrays.asList(arr);
		tags = new TreeSet<String>(list);
		//this.lon = lon;
		//this.lat = lat;
	}
	
	public Restaurant(String name, String addy, String phone, String menu, String web, double r, String tag, String pic, double lon, double lat)
		{
			this.name = name;
			this.address = addy;
			this.number = phone;
			this.menu = menu;
			this.website = web;
			this.picture = pic;
			rating = r;
			String[] arr = tag.split(",");
			List<String> list = Arrays.asList(arr);
			tags = new TreeSet<String>(list);
			this.lon = lon;
			this.lat = lat;
		}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Name: " + name + "\n");
		sb.append("Address: " + address + "\n");
		sb.append("Phone: " + number + "\n");
		sb.append("Menu: " + menu + "\n");
		for (String t : tags)
			sb.append(t + " ");

		return sb.toString();
	}

	public String getName()
	{
		return name;
	}

	/*public String getPic()
	{
		return picture;
	} */

	public String getAddress()
	{
		return address;
	}

	public String getPhone()
	{
		return number;
	}

	public String getMenu()
	{
		return menu;
	}

	public String getWeb()
	{
		return website;
	}

	public double getRating()
	{
		return rating;
	}

	public TreeSet<String> getTags()
	{
		return tags;
	}

	public String getPic()
	{
		return picture;
	}

	public void setLonLat(double lon, double lat)
	{
		this.lon = lon;
		this.lat = lat;
	}
	public double getLon()
	{
		return lon;
	}

	public double getLat()
	{
		return lat;
	}

	public int compare(Restaurant a, Restaurant b)
	{
		if (a.rating - b.rating < 0)
			return -1;
		if (a.rating - b.rating > 0)
			return 1;
		return 0;
	}

	public static void main(String[] args)
	{

	}
}

class RSort implements Comparator<Restaurant>
{
	public int compare(Restaurant a, Restaurant b)
	{
		double x = a.getRating();
		double y = b.getRating();
		if (x - y < 0)
			return 1;
		if (x - y > 0)
			return -1;
		return 0;
	}
}

class DSort implements Comparator<Restaurant>
{
	public int compare(Restaurant a, Restaurant b)
	{
		double x1 = a.getLon();
		double y1 = a.getLat();
		double x2 = b.getLon();
		double y2 = b.getLat();
		double lat = 40.3467;
		double lon = -74.6554;
		double d1 = Math.hypot(x1-lon, y1-lat);
		double d2 = Math.hypot(x2-lon, y2-lat);
		if (d1 - d2 > 0)
			return 1;
		if (d1 - d2 < 0)
			return -1;
		return 0;
	}
}
