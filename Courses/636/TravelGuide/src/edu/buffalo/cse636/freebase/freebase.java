package edu.buffalo.cse636.freebase;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.google.gson.*;

/*
 * example of using this class
 * freebase fb=new freebase();
 * try{
 * HashMap<String,String> hs=fb.freeBaseInformation("New York City");
 * }
 * catch (Exception e){
 * e.printStackTrace();
 * }
 * */
public class freebase {

	private String apiKey="AIzaSyCizQnhxMCiLH-mAoQdFKb7rvGAVbof_98";
	private String searchService="https://www.googleapis.com/freebase/v1/search?query=";
	private String topicServiceHead="https://www.googleapis.com/freebase/v1/topic";
	private String placeUrlTail="?props=&lang=en&filter=%2Ftravel%2Ftravel_destination%2Ftourist_attractions&limit=30&";
	private String spotUrlTail="?filter=/common/topic/description";
	private String searchFilter="&filter=(any type:/location/citytown)";
	public String spotId=null;
	public String topicUrl=null;
	public String searchUrl=null;
	public HashMap<String,String> spotsAndIds=new HashMap<String,String>();
	public HashMap<String,String> spotsAndDescriptions=new HashMap<String,String>();
	Gson gson=new Gson();
	private String invalidCityNotification="Sorry, we can't find information of this city";
	public HashMap<String,String> getFreeBaseInformation(String cityName)
	{
		try {
			String cityId=freebaseGetCityId(cityName);
			if((cityId!=null)&&(!cityId.equals(invalidCityNotification)))
			{
			spotsAndIds=freebasePlaceTopic(cityId);
			Iterator spotsIter= spotsAndIds.entrySet().iterator();
			while(spotsIter.hasNext())
			{
				Map.Entry<String, String> entry=(Map.Entry<String, String>) spotsIter.next();
				String spot=entry.getKey();
				String spotId=entry.getValue();
				String spotDescription=spotDescription(spotId);
				spotsAndDescriptions.put(spot, spotDescription);
			}
			}
			else return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return spotsAndDescriptions;
	}
	public String freebaseGetCityId(String cityName) throws IOException
	{
		searchUrl=searchService+cityName+searchFilter;
		searchUrl=searchUrl.replaceAll(" ", "%20");

		URL searchCityIdUrl=new URL(searchUrl);
		BufferedReader buffer = new BufferedReader(
				new InputStreamReader(searchCityIdUrl.openStream()));
        String inputLine;
        StringBuffer sb=new StringBuffer();
        String mid_s=null;
        while ((inputLine=buffer.readLine())!=null)
        {
        	sb.append(inputLine);
        }
        inputLine=sb.toString();
        try{
        JsonParser jsParser=new JsonParser();
        JsonObject jsObject = (JsonObject)jsParser.parse(inputLine);
        JsonArray  values   = jsObject.getAsJsonArray("result");        
        if(values.size()>=1)
        {
        	JsonObject data=values.get(0).getAsJsonObject();
        	mid_s=data.get("mid").getAsString();
        	return mid_s;
        }
        }
       catch(Exception e){
        	mid_s=invalidCityNotification;
        }
		return mid_s;

	}
	public HashMap<String,String> freebasePlaceTopic(String cityId) throws IOException
	{
		HashMap <String, String> placesAndIds=new HashMap<String,String>();
		topicUrl=topicServiceHead+cityId+placeUrlTail+apiKey;
		URL searchUrl=new URL(topicUrl);
		BufferedReader buffer = new BufferedReader(
				new InputStreamReader(searchUrl.openStream()));
        String inputLine;
        StringBuffer sb=new StringBuffer();
        while ((inputLine=buffer.readLine())!=null)
        {

        	sb.append(inputLine);
        }
        inputLine=sb.toString();
        try{
        JsonParser jsParser = new JsonParser();
        JsonObject jsObject = (JsonObject)jsParser.parse(inputLine);
        JsonObject property = jsObject.getAsJsonObject("property");
        JsonObject tourist  = property.getAsJsonObject("/travel/travel_destination/tourist_attractions");
        JsonArray  values   = tourist.getAsJsonArray("values"); 
        for(int i=0;i<values.size();i++) {
            JsonObject data = values.get(i).getAsJsonObject();
            String text = data.get("text").getAsString();
            String placeId=data.get("id").getAsString();
            placesAndIds.put(text, placeId);
         }  	
        }
        catch(Exception e)
        {
        	placesAndIds.put("Sorry", "No spots information is provided for this city currently.");
        }
        return placesAndIds;
	}
	
	public String spotDescription(String spotID)
	{
		String spotDescription;
		try{
		topicUrl=topicServiceHead+spotID+spotUrlTail;
		URL searchUrl=new URL(topicUrl);
		BufferedReader buffer = new BufferedReader(
				new InputStreamReader(searchUrl.openStream()));        
        StringBuffer sb=new StringBuffer();
        while ((spotDescription=buffer.readLine())!=null)
        {

        	sb.append(spotDescription);
        }
        spotDescription=sb.toString();

        JsonParser jsParser = new JsonParser();
        JsonObject jsObject = (JsonObject)jsParser.parse(spotDescription);
        JsonObject property = jsObject.getAsJsonObject("property");
        JsonObject tourist  = property.getAsJsonObject("/common/topic/description");
        JsonArray  values   = tourist.getAsJsonArray("values"); 
        for(int i=0;i<values.size();i++) {
            JsonObject data = values.get(i).getAsJsonObject();
            spotDescription = data.get("value").getAsString();

        }
        return spotDescription; 
		}
		catch(Exception e){
			return spotDescription="Sorry, no description is provided for this spot currently";
		}
		
	}

}