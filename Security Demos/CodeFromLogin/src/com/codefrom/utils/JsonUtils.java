package com.codefrom.utils;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class JsonUtils {

	public JsonUtils() {
	}

	/**
	 * Gson - 根据json字符串和class返回List<T>
	 * 
	 * @param json
	 * @param clazz
	 * @return
	 */
	public static <T> List<T> toListByGson(String json, Class<T> classOfT) {

		// Gson g = new Gson();
		Gson g = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
				.create();
		List<JsonObject> jsonObjs = g.fromJson(json,
				new TypeToken<List<JsonObject>>() {
				}.getType());

		ArrayList<T> listOfT = new ArrayList<T>();
		for (JsonObject jsonObj : jsonObjs) {
			listOfT.add((T) new Gson().fromJson(jsonObj, classOfT));
		}

		return listOfT;
	}

	/**
	 * Gson - 根据json字符串和class返回Object
	 * 
	 * @param json
	 * @param classOfT
	 * @return
	 */
	public static <T> Object toObjectByGson(String json, Class<T> classOfT) {
		Gson g = new Gson();
		return g.fromJson(json, classOfT);
	}

	/**
	 * FastJson - 根据json字符串和class返回List<T>
	 * 
	 * @param json
	 * @param classOfT
	 * @return
	 */
	public static <T> List<T> toListByFastJson(String json, Class<T> classOfT) {
		return JSON.parseArray(json, classOfT);
	}

	/**
	 * FastJson - 根据json字符串和class返回Object
	 * 
	 * @param json
	 * @param classOfT
	 * @return
	 */
	public static <T> Object toObjectByFastJson(String json, Class<T> classOfT) {
		return JSON.parseObject(json, classOfT);
	}

	/**
	 * Gson - 根据list生成json字符串
	 * 
	 * @param list
	 * @return
	 */
	public static <T> String fromListByGson(List<T> list) {
		// Gson g = new Gson();
		// Gson g = new
		// GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		Gson g = new GsonBuilder().disableHtmlEscaping().create();
		String json = g.toJson(list);
		return json;
	}

	public static <T> String fromObjectByGson(Object o) {
		// Gson g = new Gson();
		// 使用GsonBuilder创建Gson对象，disableHtmlEscaping可以避免将'转换成\u0027这样的问题
		Gson g = new GsonBuilder().disableHtmlEscaping().create();
		String json = g.toJson(o);
		return json;
	}

	public static <T> String fromListByFastJson(List<T> list) {
		// 使用 SerializerFeature.DisableCircularReferenceDetect
		// 禁止循环使用，如果不添加，会出现"$ref"这种东西
		String json = JSON.toJSONString(list,
				SerializerFeature.DisableCircularReferenceDetect);
		// String json = JSON.toJSONString(list);
		return json;
	}

	public static <T> String fromObjectByFastJson(Object o) {
		String json = JSON.toJSONString(o,
				SerializerFeature.DisableCircularReferenceDetect);
		return json;
	}

	public static void main(String[] args) {
		// ProjectBean p = new ProjectBean();
		// p.setTitle("this is title");
		// p.setTags(new String[]{"tag1","tag2"});
		// p.setAuthor("I'm the author");
		//
		// List<ProjectBean> l = new ArrayList<ProjectBean>();
		// l.add(p);
		// l.add(p);
		//
		// System.out.println(fromObjectByGson(p));
		// System.out.println(fromObjectByFastJson(p));
		// System.out.println(fromListByGson(l));
		// System.out.println(fromListByFastJson(l));
		//
		// ProjectBean p1 = (ProjectBean) toObjectByGson(fromObjectByGson(p),
		// ProjectBean.class);
		// ProjectBean p2 = (ProjectBean)
		// toObjectByFastJson(fromObjectByGson(p), ProjectBean.class);
		// ProjectBean p3 = (ProjectBean)
		// toObjectByGson(fromObjectByFastJson(p), ProjectBean.class);
		// ProjectBean p4 = (ProjectBean)
		// toObjectByFastJson(fromObjectByFastJson(p), ProjectBean.class);
		// p1.print();
		// p2.print();
		// p3.print();
		// p4.print();

	}
}
