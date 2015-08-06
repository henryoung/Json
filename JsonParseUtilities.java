import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * This class is a utility class for json data parse.
 * @version 1.0 2015-08-05
 * @author Yang Yuke
 */
public final class JsonParseUtilities 
{
	/**
	 * search elements with specified rule.
	 * @param filename name of json data file to be searched
	 * @param nodeName search under node element which name is nodeName
	 * @param  matchRule match rule
	 * @param actionRule used to update matched node Property , null or empty will do nothing
	 * @return searched nodes matched specified rule
	 */
	public static ArrayList<JsonElement> searcheNode(String fileName, String nodeName, Map<String, String> matchRule, Map<String, String> actionRule)
	{		
		ArrayList<JsonElement> jElmList = new ArrayList<JsonElement>(); 
		
		if (nodeName ==null || nodeName.isEmpty())
		{
			return null;
		}
		
		try
		{
			JsonReader reader = new JsonReader(new FileReader(fileName));
			reader.setLenient(true);
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
			JsonObject jObj = gson.fromJson(reader, JsonObject.class);
		
		    jElmList = getMatchedNode(gson, jObj.get(nodeName), nodeName, matchRule);	
		    
		    for (JsonElement temp : jElmList)
			{
				for (String key : actionRule.keySet())
				{
					String value = actionRule.get(key);	
					
					if(temp.getAsJsonObject().has(key))
					{
						temp.getAsJsonObject().addProperty(key, value);
					}
				}
			}
		    
		    String outputFilename = actionRule.get("OUT_FILE");
			
			if (outputFilename != null && !outputFilename.isEmpty())
			{
				String prettyJsonString = gson.toJson(jObj);
				FileWriter fw = new FileWriter(outputFilename);
				fw.write(prettyJsonString);
				fw.close();
			}
		    reader.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			   
			System.out.println(e.toString());
		}
		catch (IOException e)
		{
			 e.printStackTrace();
			   
			 System.out.println(e.toString());
		}
		
		return jElmList;
	}
	
	/**
	 * count element with specified key and value.
	 * @param jElmList json element use to be counted
	 * @param Key key of element
	 * @param Value value of element
	 * @return counted number
	 */
	public static int countElement(ArrayList<JsonElement> jElmList, String Key, String Value)
	{
		int countNum = 0;
		if (jElmList == null)
		{
			return countNum;
		}
		for (JsonElement temp : jElmList)
		{
			Gson gson = new Gson();
			Map<String, String> jsonString = gson.fromJson(temp, new TypeToken<Map<String, String>>(){}.getType());	
			
			if (jsonString == null || jsonString.isEmpty())
			{
				continue;
			}
			
			if (jsonString.get(Key).equals(Value))
			{
				++countNum;
			}
		}
		return countNum;
	}
	
	/**
	 * get matched node with specified match rule.
	 * @param jElm json element to be searched
	 * @param matchRule match rule used for search
	 * @return matched nodes with the rule
	 */
	private static ArrayList<JsonElement> getMatchedNode(Gson gson, JsonElement jElm, String nodeName, Map<String, String> matchRule)
	{
		ArrayList<JsonElement> jElmList = new ArrayList<JsonElement>();
		if (matchRule == null 
			||matchRule.isEmpty() 
			|| jElm.isJsonNull())
		{
			jElmList.add(jElm);
			return jElmList;
		}
		
		if (jElm.isJsonPrimitive())
		{
			for (String key : matchRule.keySet()) 
			{
			    String value = matchRule.get(key);
			    if (!key.equals(nodeName) || !value.equals(((JsonPrimitive)jElm).getAsString()))
			    {
			    	return null;
			    }
			}
			
			jElmList.add(jElm);			
			return jElmList;
		}
		
		if (jElm.isJsonArray())
		{
			for (JsonElement temp : (JsonArray)jElm) 
			{				
				if (temp.isJsonArray())
				{
					jElmList.addAll(getMatchedNode(gson, temp, nodeName, matchRule));
				}
				
				if (!temp.isJsonObject())
				{
					continue;
				}
				
				Map<String, String> jsonString = gson.fromJson(temp, new TypeToken<Map<String, String>>(){}.getType());	
				
				if (jsonString == null || jsonString.isEmpty())
				{
					continue;
				}
				
				boolean isMatched = true;
				
				for (String key : matchRule.keySet())
				{
					String value = matchRule.get(key);
					
					if (!jsonString.get(key).equals(value))
					{
						isMatched = false;
						break;
					}
				}
				
				if (isMatched)
				{
					jElmList.add(temp);
				}				
			}
		}
		
		return jElmList;
	}
}
