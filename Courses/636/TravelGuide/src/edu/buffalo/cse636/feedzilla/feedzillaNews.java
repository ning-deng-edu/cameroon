package edu.buffalo.cse636.feedzilla;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.alchemyapi.api.AlchemyAPI;
import com.alchemyapi.api.AlchemyAPI_KeywordParams;
import com.google.gson.*;
/*
 * example of using this class
 * feedzillaNews fz=new feedzillaNews();
 * try{
 * fz.getRecentNews("New York City");
 * String sentimentOfNews=fz.getSentimentOfNews();
 * HashMap <String,String> keywordsAndSentiments=fz.getKeyWordsAndSentiment();
 * ArrayList<String> NewsKeywords=fz.getNewsKeywords();
 * }
 * catch (Exception e){
 * e.printStackTrace();
 * }
 * 
 * String senti=fz.getSentimentOfNews();
 * ArrayList<String> keyWord=fz.getNewsKeywords();
 * HashMap<String,String> sentiAndWord=fz.getKeyWordsAndSentiment();
 * */
public class feedzillaNews {
	
	private String searchService="http://api.feedzilla.com/v1/categories/26/articles/search.json?q=";
	private String url=null;
	String queryDate="";
	String sentimentOfNews;
	ArrayList<String> newsKeywords=new ArrayList<String>();
	public HashMap<String,String> keywordsAndSentiment=new HashMap<String,String>();
	public String getSentimentOfNews(){
		return sentimentOfNews;
	}
	public ArrayList<String> getNewsKeywords(){
		return newsKeywords;
	}
	public HashMap<String,String> getKeyWordsAndSentiment(){
		return keywordsAndSentiment;
	}
	private String queryDate()
	{
		int year,month,day;
		Calendar cal=Calendar.getInstance();
		cal.add(Calendar.DATE, -7);
		year=cal.get(Calendar.YEAR);
		month=cal.get(Calendar.MONTH)+1;
		day=cal.get(Calendar.DATE);
		String date= String.valueOf(day);
		String month_s=String.valueOf(month);
		String year_s=String.valueOf(year);
		if(day<10)
		{
			date="0"+day;			
		}
		if(month<10)
		{
			month_s="0"+month;
		}
		queryDate=year_s+"-"+month_s+"-"+date;
		return queryDate;
		
	}
	public void getRecentNews(String cityName) throws Exception
	{
		String queryDate=queryDate();
		String queryCityName=cityName.replaceAll(" ","%20");
		String url=searchService+queryCityName+"&since="+queryDate;
		URL searchUrl=new URL(url);
		BufferedReader buffer = new BufferedReader(
				new InputStreamReader(searchUrl.openStream()));
        String inputLine;
        StringBuffer sb=new StringBuffer();
        while ((inputLine=buffer.readLine())!=null)
        {
        	sb.append(inputLine);
        }
        inputLine=sb.toString();
        JsonParser jsParser = new JsonParser();
        JsonObject jsObject = (JsonObject)jsParser.parse(inputLine);
        JsonArray  values   = jsObject.getAsJsonArray("articles"); 
        String totalSum="";
        for(int i=0;i<values.size();i++) {
            JsonObject data = values.get(i).getAsJsonObject();
            String summary = data.get("summary").getAsString();
            String title=data.get("title").getAsString();
            String newsUrl=data.get("url").getAsString();
            summary=summary.replaceAll(cityName+" City", "");
            summary=summary.replaceAll(cityName, "");
            totalSum=totalSum+title+summary+" ";
        }
        alchemyGetSentiment(totalSum);
        alchemyGetKeywords(totalSum);
        alchemyGetWordsAndSenti(totalSum);
	}
	public void alchemyGetSentiment(String sum) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException 
	{
		AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromFile();
        // Extract sentiment for a text string.
        Document doc = alchemyObj.TextGetTextSentiment(sum);
        Element rootElement=doc.getDocumentElement();
        getString("type",null,rootElement,"S");    
	}
	public void alchemyGetKeywords(String sum) throws FileNotFoundException, IOException, XPathExpressionException, SAXException, ParserConfigurationException
	{
		AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromFile();
		 Document doc = alchemyObj.TextGetRankedKeywords(sum);
		 Element rootElement=doc.getDocumentElement();
	        getString("text",null,rootElement,"K");
//	        System.out.println(getStringFromDocument(doc));
	}
	public void alchemyGetWordsAndSenti(String sum) throws FileNotFoundException, IOException, XPathExpressionException, SAXException, ParserConfigurationException
	{
		AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromFile();
		AlchemyAPI_KeywordParams keywordParams = new AlchemyAPI_KeywordParams();
        keywordParams.setSentiment(true);
        Document doc = alchemyObj.TextGetRankedKeywords(sum, keywordParams);
        Element rootElement=doc.getDocumentElement();
        getString("text","type",rootElement,"KS");
//        System.out.println(getStringFromDocument(doc));
	}
	// utility method
	protected void getString(String tagName1, String tagName2, Element element,String type) {
		if(type.equals("S")){//Get the sentiment 
			NodeList list = element.getElementsByTagName(tagName1);
			 if ((list != null) && (list.getLength() > 0)) {
            NodeList subList = list.item(0).getChildNodes();
            if (subList != null && subList.getLength() > 0) {
            	sentimentOfNews=subList.item(0).getNodeValue();
             }
        	}
		}     	
        	else if(type.equals("K")){//Get keywords
        		NodeList list = element.getElementsByTagName(tagName1);
        		 if ((list != null) && (list.getLength() > 0)) {
        		for(int i=0;i<list.getLength();i++)
        		{
        			NodeList keywordList=list.item(i).getChildNodes();
        			newsKeywords.add(keywordList.item(0).getNodeValue());
        		}
        		 }
        	}
        	else if(type.equals("KS")){
        		NodeList list1 = element.getElementsByTagName(tagName1);
        		NodeList list2 = element.getElementsByTagName(tagName2);
        		for(int j=0;j<list1.getLength();j++)
        		{
        			NodeList keywordList=list1.item(j).getChildNodes();
        			NodeList sentiList=list2.item(j).getChildNodes();
        			keywordsAndSentiment.put(keywordList.item(0).getNodeValue(), sentiList.item(0).getNodeValue());
        		}
        		
        	}
       
    }
	
    private static String getStringFromDocument(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);

            return writer.toString();
        } catch (TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public static void main(String[] args)
	 {
		 feedzillaNews fz=new feedzillaNews();
		 try {
			fz.getRecentNews("New York City");
			 String sentimentOfNews=fz.getSentimentOfNews();//the sentiment of all the summaries and titles of the news in recent 7 days
			 HashMap <String,String> keywordsAndSentiments=fz.getKeyWordsAndSentiment();//keywords and corresponding sentiments
			 ArrayList<String> NewsKeywords=fz.getNewsKeywords();//just list of keywords
			 System.out.println(sentimentOfNews);
			 for(int i=0;i<NewsKeywords.size();i++)
			 {
				 System.out.println(NewsKeywords.get(i));
			 }
			 Iterator Iter= keywordsAndSentiments.entrySet().iterator();
				while(Iter.hasNext())
				{
					Map.Entry<String, String> entry=(Map.Entry<String, String>) Iter.next();
					String keyword=entry.getKey();
					String sentiment=entry.getValue();
					System.out.println(keyword+":");
					System.out.println(sentiment);
					System.out.println();
				}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
}
