package com.meltmedia.draper;

import java.io.*;
import javax.servlet.http.*;
import javax.servlet.*;
import org.apache.catalina.servlets.DefaultServlet;

public class TestServlet extends DefaultServlet implements ContentServlet 
{
	protected String pathPrefix = "/static";

	/*public void doGet (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException 
	{

		PrintWriter out = res.getWriter();

		out.println("Hello, world!");
		out.close();
	}*/

	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		if (config.getInitParameter("pathPrefix") != null)
		{
			pathPrefix = config.getInitParameter("pathPrefix");
		}
	}	

	protected String getRelativePath(HttpServletRequest req)
	{
		return pathPrefix + super.getRelativePath(req);
	}
	
	public void changeContentPath(String newPath)
	{
		//TODO do stuff to change the path in the config
	}
}


