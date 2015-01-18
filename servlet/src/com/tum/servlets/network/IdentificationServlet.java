
package com.tum.servlets.network;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tum.servlets.DataFactory;
import com.tum.servlets.DataService;


@WebServlet("/Identification")
public class IdentificationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private static boolean prepared = false;

	 public void init()  {
		 System.out.println("INIT!");   
		 prepared = initDatabase();
	 }
	 

		
	 public boolean initDatabase(){
		 Connection connection = DataService.connect();
		 if(connection!=null){
			 if(DataService.init(connection)){
				 DataService.close(connection);
				 return true;
			 }
			 DataService.close(connection);
		 }
		 return false;
	 }

	
    public IdentificationServlet() {
        super();
    
    }
   
    public void doGet(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        System.out.println("doGet");
        response.setContentType("text/html");
        out.println("IdentificationServlet");
        out.close();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(prepared==false){
			prepared = initDatabase();
		}
		if(prepared){
			DataService.rootPath = getServletContext().getRealPath("/");
			System.out.println(DataService.rootPath);
			//System.out.println("doPost: start!");
			long time = java.lang.System.currentTimeMillis();
			byte[] answer = DataFactory.newRequest(request);
			OutputStream out=response.getOutputStream();
			//System.out.println("write answer: "+answer.length);
			out.write(answer);
		    out.close();  
			//System.out.println("doPost: stop!");
			System.out.println((java.lang.System.currentTimeMillis()-time)+" ms");
		}
		else{
			System.out.println("Database not prepared!");   
		}

	}

}

