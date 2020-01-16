package com.callme.platform.util.cache;


/*
 * Description：file cache facade of json http response
 * 
 */

public class JsonCacheImp {
	private static final String TAG = "JsonCacheImp";

	/**
	 * 缓存json数据
	 * 
	 * @return
	 */
	public void put(String url, String data) {
		if (url == null || data == null)
			return;

		Cache cache = CacheFactory.getCache(CacheFactory.CACHE_JSON);
		cache.put(url, new JsonData(data));

	}

	/**
	 * 获取对象缓存
	 * 
	 * @param url
	 * @return
	 */
	public String get(String url) {
		if (url == null)
			return null;

		Cache cache = CacheFactory.getCache(CacheFactory.CACHE_JSON);
		JsonData data = ((JsonData) cache.get(url));
		if (data != null) {
			return data.data;
		}
		return null;
	}

	public void remove(String url) {
		if (url == null)
			return;

		Cache cache = CacheFactory.getCache(CacheFactory.CACHE_JSON);
		cache.remove(url);
	}

	/**
	 * 指定URL是否缓存(1/2级)
	 * 
	 * @param url
	 * @return
	 */
	public boolean hasCached(String url) {
		boolean hasCache = false;
		if (url == null)
			return false;

		Cache cache = CacheFactory.getCache(CacheFactory.CACHE_JSON);
		if (cache.containsKey(url)) {
			hasCache = true;
		} 
		return hasCache;
	}

	public void clear() {
		Cache cache = CacheFactory.getCache(CacheFactory.CACHE_JSON);
		cache.clear();
	}

}
