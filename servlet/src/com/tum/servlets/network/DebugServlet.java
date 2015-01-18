package com.tum.servlets.network;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class DebugServlet
 */
@WebServlet("/Debug")
public class DebugServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DebugServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String user  = request.getParameter("id");
		String type  = request.getParameter("type");
		String debug = request.getParameter("debug");
	    PrintWriter out = response.getWriter();
	    System.out.println("Debug doPost");
	    response.setContentType("text/html");
	    out.println("");
	    out.close();
	    System.out.println("id: "+user);
	    System.out.println("type: "+type);
	    System.out.println("debug: "+debug);
		if(user!=null && debug != null && type != null){
			String rootPath = getServletContext().getRealPath("/");
			String filePath = rootPath+"\\debug_"+user+"_"+type+"_"+System.currentTimeMillis()+".txt";
			FileOutputStream fos = new FileOutputStream(filePath);
			String debugText = URLEncoder.encode(debug,"UTF-8");
			if(fos!=null){
				fos.write(debug.getBytes());
				fos.close();
			}
		}
		
	}

}
