package com.tum.servlets.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tum.servlets.DataService;

/**
 * Servlet implementation class CreateDatabase
 */
@WebServlet("/CreateDatabase")
public class CreateDatabase extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateDatabase() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean result = false;
		Connection connection = DataService.connect();
		PrintWriter out = response.getWriter();
        response.setContentType("text/html");
		if(connection!=null){
			String hashLenString = request.getParameter("hash_len");
			String updateString = request.getParameter("update");
			boolean updateDatabase = false;
			if(updateString==null){
				updateDatabase = false;
			}else if(updateString.equals("true")){
				updateDatabase = true;
			}
			System.out.println("hashLenString: "+hashLenString);
			System.out.println("updateString: "+updateString+" -> "+updateDatabase);
			if(hashLenString!=null){
				int hashLength = Integer.valueOf(hashLenString);
				if(hashLength > 0){
					DataService.setHashLength(hashLength);
					if(DataService.createDatabase(connection,updateDatabase)){
						out.println("Database created");
						System.out.println("Database created");
						result = true;
					}
				}
			}
			DataService.close(connection);
		}
		
		if(result == false){
			out.println("Error");
			System.out.println("Error");
		}
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
