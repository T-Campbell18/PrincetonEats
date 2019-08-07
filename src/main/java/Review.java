public class Review
{
	private String restaurant;
	private String name;
	private int rating;
	private String review;
	
	public Review(String r, String n, int rate, String v)
	{
		restaurant = r;
		name = n;
		rating = rate;
		review = v;
	}
	
	public String getRestaurant(){return restaurant;}
	public String getName() {return name;}
	public int getRating() {return rating;}
	public String getReview() {return review;}
	public static void main(String[] args) 
	{
		
	}
}