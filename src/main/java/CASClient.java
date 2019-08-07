//----------------------------------------------------------------------
// CASClient.java
// Authors: Scott Karlin, Alex Halderman, Brian Kernighan, Bob Dondero 
//----------------------------------------------------------------------

import java.net.*;
import java.util.*;
import java.io.*;
import spark.Request;
import spark.Response;
import spark.Spark;

public class CASClient
{
	private String casUrl;

	public CASClient()
	{
		casUrl = "https://fed.princeton.edu/cas/";
	}

	public CASClient(String casUrl)
	{
		this.casUrl = casUrl;
	}

	// Authenticate the remote user, and return the user's NetID.
	// Upon the first call, redirect the browser to a login page to
	// complete the authentication process.  (The login page then
	// redirects the browser back to the current page.)
	// Upon subsequent calls, confirm that the login was successful
	// and return the NetID.  Do not return unless the user is
	// successfully authenticated.
	public String authenticate(Request req, Response res)
	{
		// If the request contains a login ticket, try to validate it.
		String ticket = req.queryParams("ticket");
		if (ticket != null)
		{  String netid = validate(ticket, req);
			if (netid != null)
				return netid;
		}

		// No valid ticket; redirect the browser to the login page
		// to get one.
		String loginUrl = casUrl;
		loginUrl += "login?service=";
		try { loginUrl += URLEncoder.encode(serviceURL(req), "UTF-8"); }
		catch (Exception e) { System.err.println(e); }

		res.redirect(loginUrl);
		Spark.halt();
		
		return null;
	}

	// Validate a login ticket by contacting the CAS server. If
	// valid, return the user's NetID; otherwise, return null.
	public String validate(String ticket, Request req)
	{
		try
		{  String valUrl = casUrl;
			valUrl += "validate?service=";
			valUrl += URLEncoder.encode(serviceURL(req), "UTF-8");
			valUrl += "&ticket=";
			valUrl += URLEncoder.encode(ticket, "UTF-8");

			URL url = new URL(valUrl);
			InputStream is = url.openStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			// Read two lines.  If the first line contains "yes", then
			// the second should contain the user's NetID.
			String line1 = br.readLine();
			if (line1 == null) { br.close(); return null; }
			if (! line1.contains("yes")) { br.close(); return null; }
			String line2 = br.readLine();
			if (line2 == null) { br.close(); return null; }
			br.close();
			return line2.trim();
		}
		catch (Exception e)
		{  System.err.println(e);
			return null;
		}
	}

	// Return the URL of the current page after stripping out the
	// "ticket" parameter added by the CAS server.
	public String serviceURL(Request req)
	{
		// Get the URL (without the query string).
		String url = req.url();
		if ((url == null) || (url.trim().equals("")))
			return "something is badly wrong";

		// Add the query string, except for the "ticket" parameter.
		String queryString = req.queryString();
		if (queryString == null)
			return url;
		String[] params = queryString.split("&");
		String separator = "?";
		for (String param : params)
			if (! param.startsWith("ticket="))
			{  url += separator + param;
				separator = "&";
			}
		return url;
	}

	public static void main(String[] args)
	{
		System.err.println("CASClient does not run standalone");
	}
}