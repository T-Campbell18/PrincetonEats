import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.*;

public class RDatabase
{
  private Connection connection;
  private TreeSet<String> tags;

  public RDatabase() {}

  public void connect() throws Exception
	{

    URI dbUri = new URI("postgres://erruducoppkkyg:81c228d60c816bb529d59da2d9579c6e5a5c73bfbcdb27bdbc7821de1e55fb1c@ec2-107-22-162-8.compute-1.amazonaws.com:5432/d5lec2gi2ul772");
    //URI dbUri = new URI(System.getenv("DATABASE_URL"));

    String username = dbUri.getUserInfo().split(":")[0];
    String password = dbUri.getUserInfo().split(":")[1];
    String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();

    connection = DriverManager.getConnection(dbUrl, username, password);

    createTagList();
  }

  public void disconnect() throws Exception
	{
		connection.close();
	}

  private void createTagList() throws Exception
	{
		tags = new TreeSet<String>();

		String stmtString = "SELECT lower(Tags) FROM Restaurants";
		PreparedStatement statement = connection.prepareStatement(stmtString);
		ResultSet tagResults = statement.executeQuery();

		while(tagResults.next())
		{
			String t = tagResults.getString(1);
			String[] ts = t.split(",");
			for (String x : ts)
			{
				if (x.contains("/"))
				{
					String[] xs = x.split("/");
					for (String y : xs)
					{
						tags.add(y);
					}
				}
				tags.add(x);
			}
		}
	}

  private static String replaceWildCard(String str)
	{
		if (str == null || str.isEmpty())
			return "%";
		str = str.replaceAll("_", "[_]");
		str = str.replaceAll("%", "[%]");
		return "%" + str + "%";
	}

  public LonLat getLoc(String r) throws Exception
	{
		LonLat l = new LonLat();
		String stmtStr = "SELECT * " +
		"FROM Location " +
		"WHERE Restaurants = ?";
		PreparedStatement statement = connection.prepareStatement(stmtStr);
		statement.setString(1, r);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
		{
			double lon = resultSet.getDouble("Lon");
			double lat = resultSet.getDouble("Lat");
			l.setLonLat(lon, lat);
		}
		return l;
	}

  public void addRest(String r) throws Exception
    {
      String stmtStr = "INSERT INTO Suggestions (Restaurant) VALUES (?) ";
  		PreparedStatement statement = connection.prepareStatement(stmtStr);
      statement.setString(1, r);
      statement.executeUpdate();
    }

	public ArrayList<String> getTags(String r) throws Exception
	{
		String stmtStr = "SELECT Tags " +
		"FROM Restaurants " +
		"WHERE Restaurant = ?";
		PreparedStatement statement = connection.prepareStatement(stmtStr);
		statement.setString(1, r);
		ResultSet resultSet = statement.executeQuery();
		String t = "";
		while (resultSet.next())
		{
			t = resultSet.getString("Tags");
		}
		String[] ts = t.split(",");
		ArrayList<String> tags = new ArrayList<String>();
		for (String i : ts)
		{
			tags.add(i);
		}
		return tags;
	}

  public ArrayList<Restaurant> search(String search) throws Exception
	{
    search = search.toLowerCase();
    search = search.trim();
    ArrayList<Restaurant> rest = new ArrayList<Restaurant>();
		boolean tag = tags.contains(search);

		String stmtStr = "SELECT * " +
		"FROM Restaurants " +
		"WHERE lower(Restaurant) LIKE ?";
		if (tag) stmtStr += " OR  lower(Tags) LIKE ?";
		PreparedStatement statement = connection.prepareStatement(stmtStr);
		statement.setString(1, replaceWildCard(search));
		if (tag) statement.setString(2, replaceWildCard(search));
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
		{
			String name = resultSet.getString("Restaurant");
			String addy = resultSet.getString("Address");
			String phone = resultSet.getString("Phone");
			String web = resultSet.getString("Website");
			String menu = resultSet.getString("Menu");
			String tags = resultSet.getString("Tags");
			String picture = resultSet.getString("Photo");
      double r = avgRating(name);
			LonLat loc = getLoc(name);
			rest.add(new Restaurant(name, addy, phone, menu, web, r, tags, picture, loc.getLon(), loc.getLat()));
		}
		return rest;
	}

	public Restaurant restaurantDetails(String search) throws Exception
	{
		Restaurant rest = null;

		String stmtStr = "SELECT * " +
		"FROM Restaurants " +
		"WHERE Restaurant LIKE ?";
		PreparedStatement statement = connection.prepareStatement(stmtStr);
		statement.setString(1, replaceWildCard(search));
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next())
		{
			String name = resultSet.getString("Restaurant");
			String addy = resultSet.getString("Address");
			String phone = resultSet.getString("Phone");
			String web = resultSet.getString("Website");
			String menu = resultSet.getString("Menu");
			String tags = resultSet.getString("Tags");
			String picture = resultSet.getString("Photo");
			double r = avgRating(name);
			rest = new Restaurant(name, addy, phone, menu, web, r, tags, picture);
		}
		return rest;
	}

	public RestaurantAmenities restaurantAmenities(String search) throws Exception
	{
		RestaurantAmenities rest = null;

		String stmtStr = "SELECT * " +
		"FROM RestaurantAmenities " +
		"WHERE Restaurant LIKE ?";
		PreparedStatement statement = connection.prepareStatement(stmtStr);
		statement.setString(1, replaceWildCard(search));
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next())
		{
			String name = resultSet.getString("Restaurant");
			int price = resultSet.getInt("Price");
			int cash = resultSet.getInt("CashOnly");
			int dlvrs = resultSet.getInt("Delivers");
			int res = resultSet.getInt("Reservations");
			int tkout = resultSet.getInt("Takeout");
			int dscnt = resultSet.getInt("Discounts");
			int wifi = resultSet.getInt("Wifi");
			rest = new RestaurantAmenities(name, price, cash, dlvrs, res, tkout, dscnt, wifi);
		}
		return rest;
	}

	public void addReview(Review r) throws Exception
	{
		String stmtStr = "INSERT INTO Reviews (Restaurant, Student, Rating, Review) VALUES (?, ?, ?, ?) ";
		PreparedStatement statement = connection.prepareStatement(stmtStr);
		statement.setString(1, r.getRestaurant());
		statement.setString(2, r.getName());
		statement.setInt(3, r.getRating());
		statement.setString(4, r.getReview());
		statement.executeUpdate();
	}

	public ArrayList<Review> getReviews(String restaurant) throws Exception
	{
		ArrayList<Review> views = new ArrayList<Review>();

		String stmtStr = "SELECT * " +
		"FROM Reviews " +
		"WHERE Restaurant = ?";
		PreparedStatement statement = connection.prepareStatement(stmtStr);
		statement.setString(1, restaurant);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
		{
			String name = resultSet.getString("Student");
			int rate = resultSet.getInt("Rating");
			String review = resultSet.getString("Review");
			views.add(new Review(restaurant, name, rate, review));
		}
		return views;
	}
	public double avgRating(String restaurant) throws Exception
	{
		ArrayList<Review> views = new ArrayList<Review>();
		int num = 0;
		double total = 0.0;

		String stmtStr = "SELECT * " +
		"FROM Reviews " +
		"WHERE Restaurant = ?";
		PreparedStatement statement = connection.prepareStatement(stmtStr);
		statement.setString(1, restaurant);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
		{
			int rate = resultSet.getInt("Rating");
			total += rate;
			num++;
		}
		if (num == 0)
			return 0;
		double n = total/num;
		n = Math.round(n * 100);
		n = n/100;
		return n;
	}

	public ArrayList<Restaurant> getSimilar(Restaurant r) throws Exception
	{

		ArrayList<Restaurant> same = new ArrayList<Restaurant>();

		for (String tag : r.getTags())
		{
			String stmtStr = "SELECT * " +
			"FROM Restaurants " +
			"WHERE Tags LIKE ?";
			PreparedStatement statement = connection.prepareStatement(stmtStr);
			statement.setString(1, replaceWildCard(tag));
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next())
			{
				String name = resultSet.getString("Restaurant");
				if (r.getName().equals(name))
					continue;
				String addy = resultSet.getString("Address");
				String phone = resultSet.getString("Phone");
				String web = resultSet.getString("Website");
				String menu = resultSet.getString("Menu");
				String tags = resultSet.getString("Tags");
				String picture = resultSet.getString("Photo");
				double rate = avgRating(name);
				Restaurant similar = new Restaurant(name, addy, phone, menu, web, rate, tags, picture);

				// Check if restaurant has been added because multiple tags shared
				boolean different = true;
				for (Restaurant currentList : same)
				{
					if (currentList.getName().equals(similar.getName())) different = false;
				}

				if (different) same.add(similar);
			}
		}
		Collections.sort(same, new RSort());
		if (same.size() > 5)
		{
			ArrayList<Restaurant> tmp = new ArrayList<Restaurant>(same.subList(0, 5));
			return tmp;
		}
		return same;
	}

	public ArrayList<Restaurant> filterByWifi(ArrayList<Restaurant> rs) throws Exception
	{
		ArrayList<Restaurant> filter = new ArrayList<Restaurant>();
		for (Restaurant r : rs)
		{
			String stmtStr = "SELECT Wifi from RestaurantAmenities where Restaurant = ?";
			PreparedStatement statement = connection.prepareStatement(stmtStr);
			statement.setString(1, r.getName());
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next())
			{
				int wifi = resultSet.getInt("Wifi");
				if (wifi == 1)
					filter.add(r);
			}
		}
		return filter;
	}

	public ArrayList<Restaurant> filterByTakeOut(ArrayList<Restaurant> rs) throws Exception
	{
		ArrayList<Restaurant> filter = new ArrayList<Restaurant>();
		for (Restaurant r : rs)
		{
			String stmtStr = "SELECT Takeout from RestaurantAmenities where Restaurant = ?";
			PreparedStatement statement = connection.prepareStatement(stmtStr);
			statement.setString(1, r.getName());
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next())
			{
				int t = resultSet.getInt("Takeout");
				if (t == 1)
					filter.add(r);
			}
		}
		return filter;
	}

  public ArrayList<Restaurant> filterByPrice(ArrayList<Restaurant> rs, int p1, int p2, int p3) throws Exception
	{
		ArrayList<Restaurant> filter = new ArrayList<Restaurant>();
		for (Restaurant r : rs)
		{
			String stmtStr = "SELECT Price from RestaurantAmenities where Restaurant = ?";
			PreparedStatement statement = connection.prepareStatement(stmtStr);
			statement.setString(1, r.getName());
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next())
			{
				int p = resultSet.getInt("Price");
				if (p == p1 ||p == p2 || p == p3)
					filter.add(r);
			}
		}
		return filter;
	}

	public ArrayList<Restaurant> filterByDelivers(ArrayList<Restaurant> rs) throws Exception
	{
		ArrayList<Restaurant> filter = new ArrayList<Restaurant>();
		for (Restaurant r : rs)
		{
			String stmtStr = "SELECT Delivers from RestaurantAmenities where Restaurant = ?";
			PreparedStatement statement = connection.prepareStatement(stmtStr);
			statement.setString(1, r.getName());
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next())
			{
				int d = resultSet.getInt("Delivers");
				if (d == 1)
					filter.add(r);
			}
		}
		return filter;
	}

	public static void main(String[] args) throws Exception
	{
		ArrayList<Restaurant> rest = null;
		ArrayList<Restaurant> filt = null;
		RestaurantAmenities restaurant = null;
		Restaurant test = null;

		Scanner in = new Scanner(System.in);
		while (in.hasNextLine())
		{
			String input = in.nextLine();
			try
			{
				Database database = new Database();
				database.connect();
				rest = database.search(input);
				filt = database.filterByWifi(rest);
				database.disconnect();
			}
			catch (Exception e)
			{
				System.err.println(e);
			}
			System.out.println("Restaurants: " + input);
			for (Restaurant r : rest)
				System.out.println(r);
			System.out.println("Filter!------------");
			for (Restaurant r : filt)
				System.out.println(r);
			System.out.println("***************************\n");
		}

	}
}
