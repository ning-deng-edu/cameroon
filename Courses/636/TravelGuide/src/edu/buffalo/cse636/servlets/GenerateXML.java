package edu.buffalo.cse636.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import edu.buffalo.cse636.mysql.MapMarker;
import edu.buffalo.cse636.mysql.MySqlConnector;

/**
 * Servlet implementation class GenerateXML
 */
@WebServlet("/GenerateXML")
public class GenerateXML extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public GenerateXML() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/xml");
		try {
			MySqlConnector sqlconnector = new MySqlConnector();
			MapMarker mapMarker = new MapMarker();
			Document doc = mapMarker.generateDoc(sqlconnector.readDataBase());

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(response.getWriter());
			
			transformer.transform(source, result);
			sqlconnector.close();
			 
		}
		catch (Exception e) {
			System.out.println(e + ": Unable to generate XML!");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
