import java.util.*;
import javax.print.*;

public class RestaurantAmenities 
{
	private String name;
	private int price;
	private boolean cashOnly;
	private boolean delivers;
  private boolean reservations;
  private boolean takeout;
	private boolean discounts;
	private boolean wifi;
	
	
    public RestaurantAmenities(String name, int price, int cashOnly, int delivers, int reservations,
    int takeout, int discounts, int wifi)
	{
		this.name = name;
    this.price = price;
    this.cashOnly = (1 == cashOnly);
    this.delivers = (1 == delivers);
    this.reservations = (1 == reservations);
    this.takeout = (1 == takeout);
    this.discounts = (1 == discounts);
    this.wifi = (1 == wifi);
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Name: " + name + "\n");
		sb.append("Price: " + getPrice() + "\n");
		sb.append("Cash Only: " + isCashOnly() + "\n");
		sb.append("Delivers: " + hasDelivery() + "\n");
        sb.append("Takes Reservations: " + hasReservations() + "\n");
        sb.append("Takeout: " + hasTakeout() + "\n");
        sb.append("Student discounts: " + hasStudentDiscounts() + "\n");
        sb.append("Wifi: " + hasWifi() + "\n");
		
		return sb.toString();
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getPrice()
	{
		StringBuilder cost = new StringBuilder();
		for (int i = 0; i < price; i++)
		{
			cost.append("$");
		}

		return cost.toString();
	}
	
	public boolean isCashOnly()
	{
		return cashOnly;
	}
	
	public boolean hasDelivery()
	{
		return delivers;
	}
	
	public boolean hasReservations()
	{
		return reservations;
	}
	
	public boolean hasTakeout()
	{
		return takeout;
	}
	
	public boolean hasStudentDiscounts()
	{
		return discounts;
	}

	public boolean hasWifi()
	{
		return wifi;
	}
	
	public static void main(String[] args) 
	{
		RestaurantAmenities test = new RestaurantAmenities("Mcdonalds", 3, 1, 0, 0, 1, 1, 1);
		System.out.println(test);
	}
}