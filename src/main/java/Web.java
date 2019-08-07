import spark.Spark;
import spark.Request;
import spark.Response;
import spark.template.velocity.VelocityTemplateEngine;
import spark.ModelAndView;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;
import java.text.DateFormat;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class Web
{
	private static String index(Request req, Response res)
		{
			ArrayList<Restaurant> rests = null;
			try
			{
				RDatabase database = new RDatabase();
				database.connect();
				rests = database.search("");
				database.disconnect();
			}
			catch (Exception e)
			{
				System.err.println(e);
			}
			Collections.sort(rests, new RSort());
			Map<String, Object> model = new HashMap<>();
			model.put("rest", rests.get(0));
			int i = (int)(Math.random()*rests.size());
			model.put("rest2", rests.get(i));
			ModelAndView mv = new ModelAndView(model, "index.vtl");
			return new VelocityTemplateEngine().render(mv);
		}

		private static String searchResults(Request req, Response res)
		{
			String s = req.queryParams("search");

			ArrayList<Restaurant> rests = null;
			try
			{
				RDatabase database = new RDatabase();
				database.connect();
				rests = database.search(s);
				if (rests.size() == 1)
				{
					res.redirect("/restaurant?r=" + rests.get(0).getName());
				}
				database.connect();
			}
		catch (Exception e)
		{
			System.err.println(e);
		}

			Map<String, Object> model = new HashMap<>();
			model.put("s", s);
			ModelAndView mv = new ModelAndView(model, "searchResults.vtl");
			return new VelocityTemplateEngine().render(mv);
		}

		private static String searchPage(Request req, Response res)
		{
		String tags = req.queryParams("search");
		String p1 = req.queryParams("p1");
		String p2 = req.queryParams("p2");
		String p3 = req.queryParams("p3");
		String to = req.queryParams("to");
		String wifi = req.queryParams("wifi");
		String d = req.queryParams("d");
		String sort = req.queryParams("sort");


		int p11 = 0;
		int p22 = 0;
		int p33 = 0;
		if (p1.contains("1"))
			p11 = 1;
		if (p2.contains("1"))
			p22 = 2;
		if (p3.contains("1"))
			p33 = 3;


		ArrayList<Restaurant> rests = null;
		try
		{
			RDatabase database = new RDatabase();
			database.connect();
			rests = database.search(tags);
			if (p1.equals("1"))
				rests = database.filterByPrice(rests, p11,p22,p33);
			if (p2.equals("1"))
				rests = database.filterByPrice(rests, p11,p22,p33);
			if (p3.equals("1"))
				rests = database.filterByPrice(rests, p11,p22,p33);
			if (to.equals("1"))
				rests = database.filterByTakeOut(rests);
			if (wifi.equals("1"))
				rests = database.filterByWifi(rests);
			if (d.equals("1"))
				rests = database.filterByDelivers(rests);
			if (sort.equals("0"))
				Collections.sort(rests, new RSort());
			if (sort.equals("1"))
				Collections.sort(rests, new DSort());
			database.disconnect();
		}
		catch (Exception e)
		{
			System.err.println(e);
		}

		if (rests.isEmpty())
		{
			return "<br> <h5>No Matches<a href=\"index\"> Search Again</a> </h5>";
		}
			String map = "<div class=\"container\"><center><div id=\"mapid\" style=\" height: 400px;\"></div> </center></div>" +
		"<script> var mymap = L.map('mapid'); " +

		"L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpejY4NXVycTA2emYycXBndHRqcmZ3N3gifQ.rJcFIG214AriISLbB6B5aw', { maxZoom: 15, attribution: 'Map data &copy; <a href=\"https://www.openstreetmap.org/\">OpenStreetMap</a> contributors, ' + '<a href=\"https://creativecommons.org/licenses/by-sa/2.0/\">CC-BY-SA</a>, ' + 'Imagery © <a href=\"https://www.mapbox.com/\">Mapbox</a>', id: 'mapbox.streets' }).addTo(mymap);";

		map += "var markerArray = []; markerArray.push(L.marker([" + 40.3467 +","+ -74.6554+"])); var locations = [";
		for (Restaurant r : rests)
		{
			LonLat tmp = null;
			try
			{
				RDatabase database = new RDatabase();
				database.connect();
				tmp = database.getLoc(r.getName());
				database.disconnect();
			}
			catch (Exception e){System.err.println(e);}

			map += "[\""+ r.getName()+"\"," + tmp + "],";


			//map += "L.marker([" + tmp.getLat() +"," + tmp.getLon()+"]).addTo(mymap).bindPopup(\""+ r.getName()+ "\");";

		}
		map = map.substring(0, map.length() - 1);
		map += "]; var home = L.ExtraMarkers.icon({icon: 'glyphicon-star', markerColor : 'orange'});" +
		"marker = new L.marker([" + 40.3467 +","+ -74.6554+"], {icon: home}).addTo(mymap);";
		map += " for (var i = 0; i < locations.length; i++) {var redMarker = L.ExtraMarkers.icon({icon: 'fa-number', number: i +1});" +
		"marker = new L.marker([locations[i][1],locations[i][2]], {icon: redMarker}).bindPopup(locations[i][0]).addTo(mymap); markerArray.push(L.marker([locations[i][1],locations[i][2]]));}";
		map += "var popup = L.popup(); var group = L.featureGroup(markerArray); mymap.fitBounds(group.getBounds(),{padding: [25,25]});</script>";

		String list = "<div class=\"container\">";
			list += "<div class=\"row\">";
			int x = 1;
			for (Restaurant r : rests)
			{
				list += "<div class=\"col-sm-6 col-lg-4\">";
				list += "<div class=\"card h-100\">";
				list += "<img class=\"card-img-top\" src= \" "+ "pics/" + r.getPic() + "\" alt=\"Card image cap\">";
				list += "<div class=\"card-body\">";
				list += "<h3 class=\"card-title\">" + x +" - " + "<a href=\"restaurant?r=" + r.getName()+ "\">"+ r.getName() + "</a></h3>";
				if (r.getRating() != 0)
					list += "<h5 class=\"card-title\">" +r.getRating() + "</h5>";
				list += "</div> </div> </div>";
				x++;
			}
			list += "</div> </div>";
		return map.concat(list);
		}

		private static String restaurant(Request req, Response res)
		{
			String r = req.queryParams("r");

			Restaurant rest = null;
			RestaurantAmenities amenities = null;
			ArrayList<Review> reviews = null;
			ArrayList<Restaurant> similar = null;
			ArrayList<String> tags = null;
			LonLat loc = null;
			double rating = 0.0;
			try
			{
				RDatabase database = new RDatabase();
				//RDatabase rdb = new RDatabase();
				//rdb.connect();
				database.connect();
				rest = database.restaurantDetails(r);
				amenities = database.restaurantAmenities(r);
				reviews = database.getReviews(r);
				rating = database.avgRating(r);
				//reviews = rdb.getReviews(r);
				//rating = rdb.avgRating(r);
				similar = database.getSimilar(rest);
				loc = database.getLoc(rest.getName());
				tags = database.getTags(r);
				database.disconnect();
				//rdb.disconnect();
			}
			catch (Exception e)
			{
				System.err.println(e);
			}

			Map<String, Object> model = new HashMap<>();
			model.put("rating", rating);
			model.put("rest", rest);
			model.put("amenities", amenities);
			model.put("reviews", reviews);
			model.put("similar", similar);
			model.put("lon", loc.getLon());
			model.put("lat", loc.getLat());
			model.put("tags", tags);
			ModelAndView mv = new ModelAndView(model, "restaurant.vtl");
			return new VelocityTemplateEngine().render(mv);
		}

		private static String review(Request req, Response res)
		{
			String r = req.queryParams("r");
			Map<String, Object> model = new HashMap<>();
			CASClient casClient = new CASClient();
			String username = casClient.authenticate(req, res);
			model.put("r", r);
			model.put("username", username);
			ModelAndView mv = new ModelAndView(model, "review.vtl");
			return new VelocityTemplateEngine().render(mv);
		}

		private static String afterReview(Request req, Response res)
		{
			String username = req.queryParams("name");
			String r = req.queryParams("r");
			int rating = Integer.parseInt(req.queryParams("selected_rating"));
			String re = req.queryParams("review");

			Review review = new Review(r, username, rating, re);
			try
			{
				RDatabase database = new RDatabase();
				//RDatabase rdb = new RDatabase();
				database.connect();
				//rdb.connect();
				database.addReview(review);
				//rdb.addReview(review);
				database.disconnect();
				//rdb.disconnect();
			}
			catch (Exception e)
			{
				System.err.println(e);
			}

			res.redirect("/restaurant?r=" + r);
			return "";
		}


		public static String addPage(Request req, Response res)
		{
			Map<String, Object> model = new HashMap<>();
			CASClient casClient = new CASClient();
			String username = casClient.authenticate(req, res);
			model.put("username", username);
			ModelAndView mv = new ModelAndView(model, "add.vtl");
			return new VelocityTemplateEngine().render(mv);
		}

		private static String suggest(Request req, Response res)
		{
			String name = req.queryParams("name");
			try
			{
				RDatabase database = new RDatabase();
				//RDatabase rdb = new RDatabase();
				database.connect();
				//rdb.connect();
				database.addRest(name);
				//rdb.addReview(review);
				database.disconnect();
				//rdb.disconnect();
			}
			catch (Exception e)
			{
				System.err.println(e);
			}
			res.redirect("/index");
			return "";
		}



	static int getHerokuAssignedPort() {
			ProcessBuilder processBuilder = new ProcessBuilder();
			if (processBuilder.environment().get("PORT") != null) {
				return Integer.parseInt(processBuilder.environment().get("PORT"));
			}
			return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
		}

	public static void main(String[] args)
	{
		Spark.port(getHerokuAssignedPort());

		Spark.staticFileLocation("/sauce");

		Spark.get("/",
			(req, res) -> index(req, res)
		);

		Spark.get("/index",
			(req, res) -> index(req, res)
		);

		Spark.get("/searchResults",
			(req, res) -> searchResults(req, res)
		);

		Spark.get("/searchPage",
			(req, res) -> searchPage(req, res)
		);

		Spark.get("/restaurant",
			(req, res) -> restaurant(req, res)
		);

		Spark.get("/review",
			(req, res) -> review(req, res)
		);

		Spark.post("/restaurant",
			(req, res) -> afterReview(req, res)
		);

		Spark.get("/addPage",
			(req, res) -> addPage(req, res)
		);

		Spark.post("/suggest",
			(req, res) -> suggest(req, res)
		);
	}
}
