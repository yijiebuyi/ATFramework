package com.callme.platform.util.cache;

import com.callme.platform.util.LogUtil;

import java.util.Collection;

/*
 * Description：缺省 cache 实现，包括 L1 & L2
 */

public class DefaultCacheImpl<K, V> implements Cache<K, V> {
	private static final String TAG = "DefaultCacheImpl";

	private DefaultL1Cache<String, Cacheable> mL1cache;
	private DefaultL2Cache<String, Cacheable> mL2cache;

	private String mName;

	private boolean mL1Enable;
	private boolean mL2Enable;

	public DefaultCacheImpl(String name) {
		mName = name;
	}

	public DefaultCacheImpl enableL1(long l1Enable, long maxnum, long maxsize,
			long maxlifetime) {
		mL1Enable = l1Enable != 0L;
		if (mL1Enable) {
			mL1cache = new DefaultL1Cache<String, Cacheable>(mName, maxnum,
					maxsize, maxlifetime);
		}
		return this;
	}

	public DefaultCacheImpl enableL2(long l2Enable, long l2MaxSize,
			long l2MaxLifetime) {
		mL2Enable = l2Enable != 0L;
		if (mL2Enable) {
			mL2cache = new DefaultL2Cache<String, Cacheable>(mName, l2MaxSize,
					l2MaxLifetime);
			mL2cache.shrink();
		}
		return this;
	}

	@Override
	public synchronized void put(String key, Cacheable value) {
		LogUtil.d(TAG, "Put : " + key);

		if (mL1Enable) {
			mL1cache.put(key, value);
		}

		if (mL2Enable) {
			mL2cache.put(key, value);
		}

		LogUtil.d(TAG, this.toString());
	}

	@Override
	public synchronized Cacheable get(String key) {
		LogUtil.d(TAG, "Get : " + key);

		Cacheable cacheable = null;

		if (mL1Enable)
			cacheable = mL1cache.get(key);

		// L1 未命中，到L2获取并回填L1
		if (cacheable == null && mL2Enable) {
			cacheable = mL2cache.get(key);
			if (mL1Enable && cacheable != null) {
				mL1cache.put(key, cacheable);
			}
		}
		return cacheable;
	}

	/**
	 * 
	 * @param key
	 * @param l1only
	 * @return
	 */
	@Override
	public synchronized Cacheable getL1Only(String key) {
		LogUtil.d(TAG, "Get : " + key);

		Cacheable cacheable = null;

		if (mL1Enable)
			cacheable = mL1cache.get(key);

		return cacheable;
	}

	@Override
	public synchronized void remove(String key) {
		if (mL1Enable)
			mL1cache.remove(key);

		if (mL2Enable)
			mL2cache.remove(key);
	}

	@Override
	public synchronized void clear() {
		if (mL1Enable)
			mL1cache.clear();

		if (mL2Enable)
			mL2cache.clear();
	}

	@Override
	public Collection values() {
		if (mL1Enable)
			return mL1cache.values();
		else
			return null;
	}

	@Override
	public boolean containsKey(String key) {
		return (mL1Enable && mL1cache.containsKey(key))
				|| (mL2Enable && mL2cache.containsKey(key));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (mL1Enable) {
			sb.append("Cache Statistics : \n");
			sb.append("Name : " + mName + "\n");
			sb.append("L1 Num : " + mL1cache.size() + "\n\n");
		}
		return sb.toString();
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getMaxNum() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getMaxSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getMaxLifetime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCacheSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getCacheHits() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getCacheMisses() {
		// TODO Auto-generated method stub
		return 0;
	}
}
