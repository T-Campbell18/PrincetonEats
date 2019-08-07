public class LonLat
{
  private double lon;
	private double lat;

  public LonLat()
  {
    this.lon = -74.6527;
		this.lat = 40.3502;
  }
  public LonLat(double lon, double lat)
  {
    this.lon = lon;
		this.lat = lat;
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

  public String toString()
  {
    return  lat + "," + lon;
  }
}
