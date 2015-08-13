package jsonparseutil;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * This class is a utility class for json data parse using streaming model
 * 
 * @version 1.0 2015-08-07
 * @author Yang Yuke
 */
public final class JsonParseExUtilities {
	/**
	 * parse gson stream
	 * 
	 * @param in
	 *            input json data
	 * @param out
	 *            output json data
	 * @param matchRule
	 *            match rule
	 * @return matched JsonObject list
	 */
	public static ArrayList<JsonObject> GsonStreamParse(Reader in, Writer out, String nodeName, MatchRule matchRule,
			AbstractMap.SimpleImmutableEntry<String, Integer> changePair) throws IOException {
		JsonReader reader = new JsonReader(in);
		JsonWriter writer = new JsonWriter(out);
		writer.setIndent("  ");
		reader.setLenient(true);

		ArrayList<JsonObject> jObjList = new ArrayList<JsonObject>();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		reader.beginObject();
		writer.beginObject();

		while (reader.hasNext()) {
			JsonToken token = reader.peek();
			if (token.equals(JsonToken.NAME)) {
				String name = reader.nextName();
				writer.name(name);
				if (name.equals(nodeName)) {
					token = reader.peek();
					switch (token) {
					case BEGIN_ARRAY:
						JsonArray array = gson.fromJson(reader, JsonArray.class);
						jObjList.addAll(handleJsonArray(array, matchRule, changePair));
						gson.toJson(array, writer);
						break;
					case BEGIN_OBJECT:
						JsonObject obj = gson.fromJson(reader, JsonObject.class);
						jObjList.add(handleJsonObject(obj, matchRule, changePair));
						gson.toJson(obj, writer);
						break;
					default:
						outputJson(reader, writer);
						break;
					}
				} else {
					outputJson(reader, writer);
				}
			} else {
				outputJson(reader, writer);
			}
		}

		reader.endObject();
		writer.endObject();
		reader.close();
		writer.close();

		return jObjList;
	}

	/**
	 * parse handle JsonArray with match rule and use changepair to update some
	 * element
	 * 
	 * @param jsonArray
	 *            json array to be handled
	 * @param matchRule
	 *            match rule
	 * @param changePair
	 *            update the element has the same name as the key using value
	 * @return matched JsonObject list
	 */
	private static ArrayList<JsonObject> handleJsonArray(JsonArray jsonArray, MatchRule matchRule,
			AbstractMap.SimpleImmutableEntry<String, Integer> changePair) throws IOException {
		ArrayList<JsonObject> jObjList = new ArrayList<JsonObject>();
		for (JsonElement jElm : jsonArray) {
			if (jElm.isJsonObject()) {
				JsonObject jObj = handleJsonObject(jElm.getAsJsonObject(), matchRule, changePair);
				if (jObj != null) {
					jObjList.add(jObj);
				}
			}
		}

		return jObjList;
	}

	/**
	 * parse handle jsonObject with match rule and use changepair to update some
	 * element
	 * 
	 * @param jsonObject
	 *            json object to be handled
	 * @param matchRule
	 *            match rule
	 * @param changePair
	 *            update the element has the same name as the key using value
	 * @return matched JsonObject list
	 */
	private static JsonObject handleJsonObject(JsonObject jsonObject, MatchRule matchRule,
			AbstractMap.SimpleImmutableEntry<String, Integer> changePair) throws IOException {
		Gson gson = new Gson();
		MatchRule test = gson.fromJson(jsonObject, MatchRule.class);
		if (test.equals(matchRule)) {
			JsonObject jObj = jsonObject;
			String key = changePair.getKey();
			if (jObj.get(key) != null) {
				jObj.addProperty(key, changePair.getValue());
			}
			return jObj;
		}

		return null;
	}

	/**
	 * output json data from reader to writer
	 * 
	 * @param JsonReader
	 *            input json data
	 * @param JsonWriter
	 *            output json data
	 */
	static void outputJson(JsonReader reader, JsonWriter writer) throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonToken token = reader.peek();
		switch (token) {
		case BEGIN_ARRAY:
			JsonArray array = gson.fromJson(reader, JsonArray.class);
			gson.toJson(array, writer);
			break;
		case BEGIN_OBJECT:
			JsonObject obj = gson.fromJson(reader, JsonObject.class);
			gson.toJson(obj, writer);
			break;
		case STRING:
			String s = reader.nextString();
			writer.value(s);
			break;
		case NUMBER:
			String n = reader.nextString();
			writer.value(new BigDecimal(n));
			break;
		case BOOLEAN:
			boolean b = reader.nextBoolean();
			writer.value(b);
			break;
		case NULL:
			reader.nextNull();
			writer.nullValue();
			break;
		default:
			reader.skipValue();
			break;
		}
		writer.flush();
	}

}
