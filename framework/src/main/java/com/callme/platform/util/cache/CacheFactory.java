package com.callme.platform.util.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheFactory {
	/**
	 * 尺寸 (bytes)
	 */
	public static final long KILO = 1024;
	public static final long MILLION = 1024 * KILO;

	/**
	 * 时间 (毫秒)
	 */
	public static final long SECOND = 1000;
	public static final long MINUTE = 60 * SECOND;
	public static final long HOUR = 60 * MINUTE;
	public static final long DAY = 24 * HOUR;
	public static final long WEEK = 7 * DAY;
	/**
	 * static cache name
	 */
	public static final String CACHE_JSON = "json";

	/**
	 * Storage for all caches that get created.
	 */
	private static Map<String, Cache> caches = new HashMap<String, Cache>();

	// 缺省一级缓存设置
	public static final long DEFAULT_L1_ENABLE = 1L;
	public static final long DEFAULT_L1_MAX_NUM = 5;
	public static final long DEFAULT_L1_MAX_SIZE = 2 * MILLION;
	public static final long DEFAULT_L1_MAX_LIFETIME = 7 * DAY;

	public static final long DEFAULT_L2_ENABLE = 1L;
	public static final long DEFAULT_L2_MAX_SIZE = 5 * MILLION;
	public static final long DEFAULT_L2_MAX_LIFETIME = 7 * DAY;

	/**
	 * 缓存系统属性表
	 */
	private static final Map<String, Long> cacheProps = new HashMap<String, Long>();

	private static final String TAG = "CacheFactory";

	static {
		// JSON缓存配置
		cacheProps.put("cache.json.l1enable", 1L);
		cacheProps.put("cache.json.l1num", 5L);
		cacheProps.put("cache.json.l1size", 2 * MILLION);
		cacheProps.put("cache.json.l1lifetime", DAY);
		cacheProps.put("cache.json.l2enable", 1L);
		cacheProps.put("cache.json.l2size", 5 * MILLION);
		cacheProps.put("cache.json.l2lifetime", 7 * DAY);
	}

	private CacheFactory() {
	}

	private static long getCacheProperty(String cacheName, String suffix,
			long defaultValue) {
		String propName = "cache." + cacheName + suffix;

		Long staticSetting = cacheProps.get(propName);

		return staticSetting == null ? defaultValue : staticSetting;
	}

	public static long getL1Enable(String cacheName) {
		return getCacheProperty(cacheName, ".l1enable", DEFAULT_L1_ENABLE);
	}

	public static long getL1MaxNum(String cacheName) {
		return getCacheProperty(cacheName, ".l1num", DEFAULT_L1_MAX_NUM);
	}

	public static long getL1MaxSize(String cacheName) {
		return getCacheProperty(cacheName, ".l1size", DEFAULT_L1_MAX_SIZE);
	}

	public static long getL1MaxLifetime(String cacheName) {
		return getCacheProperty(cacheName, ".l1lifetime",
				DEFAULT_L1_MAX_LIFETIME);
	}

	public static long getL2Enable(String cacheName) {
		return getCacheProperty(cacheName, ".l2enable", DEFAULT_L2_ENABLE);
	}

	public static long getL2MaxSize(String cacheName) {
		return getCacheProperty(cacheName, ".l2size", DEFAULT_L2_MAX_SIZE);
	}

	public static void setL2MaxSize(String cacheName, Long maxSize) {
		String propName = "cache." + cacheName + ".l2size";
		if (maxSize > DEFAULT_L2_MAX_SIZE) {
			cacheProps.put(propName, DEFAULT_L2_MAX_SIZE);
		} else {
			cacheProps.put(propName, maxSize);
		}
	}

	public static long getL2MaxLifetime(String cacheName) {
		return getCacheProperty(cacheName, ".l2lifetime",
				DEFAULT_L2_MAX_LIFETIME);
	}

	/**
	 * 返回系统的缓存列表
	 */
	public static synchronized Cache[] getAllCaches() {
		List<Cache> values = new ArrayList<Cache>();
		for (Cache cache : caches.values()) {
			values.add(cache);
		}
		return values.toArray(new Cache[values.size()]);
	}

	/**
	 * 按需创建缓存
	 */
	@SuppressWarnings("unchecked")
	public static synchronized Cache getCache(String name) {
		Cache cache = caches.get(name);
		if (cache != null)
			return cache;

		long l1Enable = CacheFactory.getL1Enable(name);
		long l1Num = CacheFactory.getL1MaxNum(name);
		long l1Size = CacheFactory.getL1MaxSize(name);
		long l1Lifetime = CacheFactory.getL1MaxLifetime(name);
		long l2Enable = CacheFactory.getL2Enable(name);
		long l2Size = CacheFactory.getL2MaxSize(name);
		long l2Lifetime = CacheFactory.getL2MaxLifetime(name);

		// 目前只有缺省实现，还没有多种实现的切换策略
		cache = new DefaultCacheImpl(name).enableL1(l1Enable, l1Num, l1Size,
				l1Lifetime).enableL2(l2Enable, l2Size, l2Lifetime);

		caches.put(name, cache);

		return cache;
	}

	/**
	 * 清理缓存
	 */
	public static synchronized void destroyCache(String name) {
		Cache cache = caches.remove(name);
		if (cache != null) {
			cache.clear();
		}
	}

	public static synchronized void clearCaches() {
		for (String cacheName : caches.keySet()) {
			Cache cache = caches.get(cacheName);
			cache.clear();
		}
	}
}
