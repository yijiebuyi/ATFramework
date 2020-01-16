package com.callme.platform.util.cache;

import java.util.Collection;

/*
 * 
 * Description：通用缓存系统接口
 * 
 * 通过唯一标识快速存取.
 * 缓存对象需要实现 Cacheable 接口，以便快速统计缓存对象大小. 这样的
 * 设计约定用于控制缓存尺寸快速膨胀以至过度内存开销。
 * 
 * 如果缓存尺寸超限，缓存系统会根据 LRU 规则淘汰相应的缓存对象. 因为淘汰机制是自动 触发的, 因此缓存对象进入缓存系统后，生存时间不受保证。
 * 
 * 所有的缓存对象都可以指定一个可选的最大生命期参数.一旦指定，缓存对象 在指定的生存时间后将被系统淘汰。
 * 
 * 所有的缓存接口操作都是线程安全的
 * 
 */

public interface Cache<K, V> {
	void put(String key, Cacheable value);

	Cacheable get(String key);

	Cacheable getL1Only(String key);

	void remove(String key);

	Collection values();

	boolean containsKey(String key);

	/**
	 * 获取缓存名
	 */
	String getName();

	/**
	 * 缓存清理
	 */
	void clear();

	/**
	 * 返回缓存的最大条目上限。如果缓存系统尺寸超过此上限，LRU 淘汰会被触发。 最大尺寸设为 -1，表示此缓存系统无条目上限
	 * 
	 * @return
	 */
	long getMaxNum();

	/**
	 * 返回缓存的最大尺寸上限。如果缓存系统尺寸超过此上限，LRU 淘汰会被触发。 最大尺寸设为 -1，表示此缓存系统无尺寸上限
	 */
	long getMaxSize();

	/**
	 * 返回缓存的最大生命上限，单位微秒 一旦超时，缓存对象会被自动淘汰 最大生命设为 -1，表示此缓存系统无生命上限
	 */
	long getMaxLifetime();

	/**
	 * 返回缓存尺寸 返回值只能反映一个比较粗的大概数字
	 */
	int getCacheSize();

	/**
	 * 返回缓存命中成功次数
	 * 
	 * 对缓存命中率的跟踪，可以评估缓存系统的工作效率，越高越好
	 */
	long getCacheHits();

	/**
	 * 返回缓存命中失败次数
	 */
	long getCacheMisses();
}
