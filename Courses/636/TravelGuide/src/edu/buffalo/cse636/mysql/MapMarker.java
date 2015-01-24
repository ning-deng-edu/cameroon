package edu.buffalo.cse636.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MapMarker {

	public Document generateDoc(ResultSet resultSet) throws SQLException, ParserConfigurationException{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("markers");
		doc.appendChild(rootElement);
		while (resultSet.next()) {
			Element marker = doc.createElement("marker");
			marker.setAttribute("name", resultSet.getString("name"));
			marker.setAttribute("lat", resultSet.getString("lat"));
			marker.setAttribute("lng", resultSet.getString("lng"));
			marker.setAttribute("type", resultSet.getString("type"));
			rootElement.appendChild(marker);
		}
		return doc;
	}
}
