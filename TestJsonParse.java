import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import jsonparseutil.ConcurrentStreamingParser;
import jsonparseutil.JsonParseExUtilities;
import jsonparseutil.JsonParseUtilities;
import jsonparseutil.MatchRule;

/**
 * This program parse json data from a local file.
 * 
 * @version 1.0 2015-08-05
 * @author Yang Yuke
 */
public class TestJsonParse
{
	public static void main(String[] args)
	{
		int i = 1;
		float time1 = 0;
		float time2 = 0;
		while (i != 0)
		{
			if (args.length != 2 && args.length != 0)
			{
				System.out.println(
						"Unrecognized Input Param!\nPlease Input 2 Params:\nFirst is Json Data File to be Parsed\nSecond is Json Rule File to be Applied for Parse");
				System.out.println(
						"Input 0 Params to Use Default : \nJson Data File : responseDetailsOfSalesorderItem.json\nJson Rule File : search_rule.json\n");

				return;
			}

			//String jsonDataFilename = "responseDetailsOfSalesorderItem.json";
			String jsonDataFilename = "bigdata.json";
			String jsonRuleFilename = "search_rule.json";	
			
			long start = System.currentTimeMillis();
			ConcurrentStreamingParser csp = new ConcurrentStreamingParser(jsonDataFilename);
			csp.Parse();
			long end = System.currentTimeMillis();
			System.out.println("Concurrent Streaming Method Excute 1 time = " + (float) (end - start) / 1000 + "s");

	
			if (args.length == 2)
			{
				jsonDataFilename = args[0];
				jsonRuleFilename = args[1];
			}
			long startTime = 0;
			long endTime = 0;

			/*String search_node_name = null;
			Map<String, String> jsonMatchRuleMap = null;
			Map<String, String> jsonActionRuleMap = null;
			String countRuleKey = null;
			String countRuleValue = null;

			SearchRule searchRule = new SearchRule();

			try
			{
				JsonReader reader = new JsonReader(new FileReader(jsonRuleFilename));
				reader.setLenient(true);
				Gson gson = new Gson();
				searchRule = gson.fromJson(reader, SearchRule.class);
				reader.close();
			} catch (FileNotFoundException e)
			{
				e.printStackTrace();

				System.out.println(e.toString());
			} catch (IOException e)
			{

			}

			startTime = System.currentTimeMillis();
			// System.out.println("=======Json Parse Method 1 Start=======");

			search_node_name = searchRule.getSEARCH_NODE_NAME();
			jsonMatchRuleMap = searchRule.getMATCH_RULEE();
			jsonActionRuleMap = searchRule.getACTION_RULEE();
			countRuleKey = searchRule.getCOUNT_RULE_KEY();
			countRuleValue = searchRule.getCOUNT_RULE_VALUE();

			// System.out.println("Search Node Name : " + search_node_name);
			// System.out.println("Macth Rule : " + jsonMatchRuleMap);
			// System.out.println("Action Rule : " + jsonActionRuleMap);
			ArrayList<JsonElement> jElmList = new ArrayList<JsonElement>();
			jElmList = JsonParseUtilities.searcheNode(jsonDataFilename, search_node_name, jsonMatchRuleMap,
					jsonActionRuleMap);
			if (jElmList != null && !jElmList.isEmpty())
			{
				// System.out.println("Search Result :");
				for (JsonElement temp : jElmList)
				{
					// System.out.println(temp);
				}
			} else
			{
				System.out.println("SubNode is not exist!");
			}

			// System.out.println("Count Rule : " + countRuleKey + " = " +
			// countRuleValue);
			int elementNum = JsonParseUtilities.countElement(jElmList, countRuleKey, countRuleValue);
			// System.out.println("Conuted Num = " + elementNum);
			// System.out.println("Output Parsed Data to " +
			// jsonActionRuleMap.get("OUT_FILE") + "!");

			// System.out.println("=======Json Parse Method 1 End=======");
			endTime = System.currentTimeMillis();
			time1 += (float) (endTime - startTime);
			// System.out.println("Json Parse Method 1 Excute Time : " +
			// (float)(endTime-startTime)/1000);*/

			startTime = System.currentTimeMillis();
			// System.out.println("=======Json Parse Method 2 Start=======");
			try
			{
				Reader in = new FileReader(jsonDataFilename);
				// Writer out = new OutputStreamWriter(System.out, "UTF-8");
				Writer out = new FileWriter("out.json");
				MatchRule test = new MatchRule();
				test.setPARTN_ROLE("AG");
				test.setSD_DOC("0000000151");
				AbstractMap.SimpleImmutableEntry<String, Integer> pair = new AbstractMap.SimpleImmutableEntry<>(
						"LEVEL_NR", 100);
				ArrayList<JsonObject> jObjList = JsonParseExUtilities.GsonStreamParse(in, out, "ORDER_PARTNERS_OUT",
						test, pair);
				// System.out.println(jObjList.toString());
				int countedNum = 0;
				for (JsonObject temp : jObjList)
				{
					JsonElement jElm = temp.get("SD_DOC");
					if (jElm != null)
					{
						String value = jElm.getAsString();
						String regx = "0000000151";
						if (value.equals(regx))
						{
							++countedNum;
						}
					}
				}
				// System.out.println(countedNum);
				// System.out.println("Output Parsed Data to out.json!");
			} catch (IOException e)
			{
				e.printStackTrace();

				System.out.println(e.toString());
			}

			// System.out.println("=======Json Parse Method 2 End=======");
			endTime = System.currentTimeMillis();
			time2 += (float) (endTime - startTime);
			// System.out.println("Json Parse Method 2 Excute Time : " +
			// (float)(endTime-startTime)/1000);
			--i;
		}
		//System.out.println("Object Method Exute 1000 times = " + time1 / 1000 + "s");
		System.out.println("Streaming Method Exute 1 time = " + time2 / 1000 + "s");
	}
	
	

	/**
	 * This class help parse search rule file formatted as json.
	 * 
	 * @version 1.0 2015-08-05
	 * @author Yang Yuke
	 */
	public static class SearchRule
	{
		private String SEARCH_NODE_NAME = null;
		private String COUNT_RULE_KEY = null;
		private String COUNT_RULE_VALUE = null;
		private Map<String, String> MATCH_RULE = null;
		private Map<String, String> ACTION_RULE = null;

		public String getSEARCH_NODE_NAME()
		{
			return SEARCH_NODE_NAME;
		}

		public void setSEARCH_NODE_NAME(String SEARCH_NODE_NAME)
		{
			this.SEARCH_NODE_NAME = SEARCH_NODE_NAME;
		}

		public String getCOUNT_RULE_KEY()
		{
			return COUNT_RULE_KEY;
		}

		public void setCOUNT_RULE_KEY(String COUNT_RULE_KEY)
		{
			this.COUNT_RULE_KEY = COUNT_RULE_KEY;
		}

		public String getCOUNT_RULE_VALUE()
		{
			return COUNT_RULE_VALUE;
		}

		public void setCOUNT_RULE_VALUE(String COUNT_RULE_VALUE)
		{
			this.COUNT_RULE_VALUE = COUNT_RULE_VALUE;
		}

		public Map<String, String> getMATCH_RULEE()
		{
			return MATCH_RULE;
		}

		public void setMATCH_RULE(Map<String, String> MATCH_RULE)
		{
			this.MATCH_RULE = MATCH_RULE;
		}

		public Map<String, String> getACTION_RULEE()
		{
			return ACTION_RULE;
		}

		public void setACTION_RULE(Map<String, String> ACTION_RULE)
		{
			this.ACTION_RULE = ACTION_RULE;
		}
	}
	

}
