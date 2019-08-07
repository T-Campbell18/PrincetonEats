import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.io.File;
import java.util.*;

public class Database
{
	private static final String DATABASE_NAME = "src/main/resources/rest.sqlite";

	private Connection connection;
	private ArrayList<String> tags;

	public Database() {}

	public void connect() throws Exception
	{
		File databaseFile = new File(DATABASE_NAME);
		if (! databaseFile.isFile())
			throw new Exception("Database connection failed ");
		connection =
			DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);
//l
		createTagList();
	}

	private void createTagList() throws Exception
	{
		tags = new ArrayList<String>();

		String stmtString = "SELECT lower(Tags) FROM Restaurants";
		PreparedStatement statement = connection.prepareStatement(stmtString);
		ResultSet tagResults = statement.executeQuery();

		while(tagResults.next())
		{
			int commaIndex = 0;
			int nextIndex = 0;
			String group = tagResults.getString(1);
			int stringLength = group.length();

			if (group.contains(","))
				while (group.indexOf(",", commaIndex) != -1 && (commaIndex != stringLength))
				{
					nextIndex = group.indexOf(",", commaIndex + 1);
					if (nextIndex == -1) nextIndex = stringLength;
					if (commaIndex == 0) tags.add(group.substring(commaIndex, nextIndex));
					else tags.add(group.substring(commaIndex + 1, nextIndex));
					commaIndex = nextIndex;
				}
			else tags.add(group);
		}
	}

	public void disconnect() throws Exception
	{
		connection.close();
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

	public ArrayList<Restaurant> search(String search) throws Exception
	{
		ArrayList<Restaurant> rest = new ArrayList<Restaurant>();
		boolean tag = tags.contains(search.toLowerCase());

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
			// need to change this
			double r = avgRating(name);
			rest.add(new Restaurant(name, addy, phone, menu, web, r, tags, picture));
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
			// need to change this
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
				// need to change this
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

	public ArrayList<Restaurant> filterByPrice(ArrayList<Restaurant> rs, int price) throws Exception
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
				if (p == price)
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
