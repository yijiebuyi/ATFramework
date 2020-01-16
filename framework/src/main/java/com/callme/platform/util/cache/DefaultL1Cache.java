package com.callme.platform.util.cache;

import com.callme.platform.util.LogUtil;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/*
 * Description：1级缓存系统缺省实现,内存缓存
 * 本实现的算法：
 * 一个 HashMap 用于缓存对象的快速存取
 * 另有两个链表：lastAcceccList，按对象访问时间排序；ageList，按生命开始时间排序
 * 当一个缓存对象添加到缓存系统时，会先用 CacheObject 包装，同时填写以下信息：
 * <li> 对象尺寸(bytes)
 * <li> 指向 accessList 节点的指针
 * <li> 指向 ageList 节点的指针
 * CacheObject里保存链表节点指针的目的是避免频繁的线性扫描
 * 查询一个缓存对象时，首先执行一个 hash 查找，拿到 CacheObject，真正的目标对象就在
 * 其包装之中。随后对链表执行的操作是，将 accessList 中该对象移到链表首位，同时根据
 * 当前淘汰机制按需淘汰某些过期对象
 */

public class DefaultL1Cache<K, V> implements Map<K, V> {
	private static final String TAG = "DefaultL1Cache";

	/**
	 * 键值表
	 */
	protected Map<K, CacheObject<V>> map;

	/**
	 * 最近访问排序列表，最近靠前，最早靠后
	 */
	protected LinkedList lastAccessedList;

	/**
	 * 生命时间排序列表，最近靠前，最早靠后
	 */
	protected LinkedList ageList;

	/**
	 * 最大缓存条目数
	 */
	private long maxNum;

	/**
	 * 最大缓存尺寸
	 */
	private long maxSize;

	/**
	 * 最大生命周期
	 */
	protected long maxLifetime;

	/**
	 * 当前缓存尺寸
	 */
	private int cacheSize = 0;

	/**
	 * 命中成功率和命中失败率
	 */
	protected long cacheHits, cacheMisses = 0L;

	/**
	 * 缓存名字
	 */
	private String name;

	/**
	 * 创建一个指定最大尺寸和最大生命的缺省缓存实现
	 * 
	 * @param name
	 *            缓存名字
	 * @param maxSize
	 *            最大缓存尺寸(bytes)，-1代表无上限
	 * @param maxLifetime
	 *            最大生命周期，-1代表永不过期
	 */
	public DefaultL1Cache(String name, long maxnum, long maxsize,
			long maxlifetime) {
		this.name = name;
		this.maxNum = maxnum;
		this.maxSize = maxsize;
		this.maxLifetime = maxlifetime;

		map = new HashMap<K, CacheObject<V>>();

		lastAccessedList = new LinkedList();
		ageList = new LinkedList();
	}

	@Override
	public V put(K key, V value) {
		LogUtil.d(TAG, "Put : " + key);

		// 删除老的已存在的 entry
		V answer = remove(key);

		int objectSize = calculateSize(value);
		LogUtil.d(TAG, "Object Size : " + objectSize);

		// 如果缓存对象超过缓存系统尺寸上限，直接拒绝添加
		if (maxSize > 0 && objectSize > maxSize * .90) {
			LogUtil.d(TAG, "Cache: " + name + " -- object with key " + key
					+ " is too large to fit in cache. Size is " + objectSize);
			return value;
		}

		cacheSize += objectSize;

		// 缓存对象包装
		CacheObject<V> cacheObject = new CacheObject<V>(
				value, objectSize);

		map.put(key, cacheObject);

		// 添加 entry 到 accessList
		LinkedListNode lastAccessedNode = lastAccessedList.addFirst(key);

		// 保存 accessList 的节点引用，以便后期快速定位
		cacheObject.lastAccessedListNode = lastAccessedNode;

		// 添加 entry 到 ageList
		LinkedListNode ageNode = ageList.addFirst(key);
		ageNode.timestamp = System.currentTimeMillis();

		// 保存 ageList 的节点引用，以便后期快速定位
		cacheObject.ageListNode = ageNode;

		// 如果缓存大小/尺寸超限，触发 LRU 淘汰
		cullCache();

		return answer;
	}

	@Override
	public V get(Object key) {
		LogUtil.d(TAG, "Get: " + key);

		// 每次查询缓存对象前，首先清理超时对象
		deleteExpiredEntries();

		CacheObject<V> cacheObject = map.get(key);
		if (cacheObject == null) {
			// 查找对象失败，累加 miss
			cacheMisses++;
			LogUtil.d(TAG, "NotFound : " + key);
			return null;
		}

		// 查找对象成功，累加命中，同时累加访问次数
		cacheHits++;
		cacheObject.readCount++;

		// 维护 accessList 顺序，将本次访问的节点移至列表最前
		cacheObject.lastAccessedListNode.remove();
		lastAccessedList.addFirst(cacheObject.lastAccessedListNode);

		LogUtil.d(TAG, "Found : " + key);
		return cacheObject.object;
	}

	@Override
	public V remove(Object key) {
		LogUtil.d(TAG, "Remove : " + key);

		CacheObject<V> cacheObject = map.get(key);
		if (cacheObject == null) {
			return null;
		}

		if (cacheObject.object instanceof Cacheable) {
			((Cacheable) cacheObject.object).recycle();
		}

		map.remove(key);

		// 维护链表
		cacheObject.lastAccessedListNode.remove();
		cacheObject.ageListNode.remove();

		// 删除缓存对象上的链表节点引用
		cacheObject.ageListNode = null;
		cacheObject.lastAccessedListNode = null;

		// 维护缓存系统条目/尺寸
		cacheSize -= cacheObject.size;

		return cacheObject.object;
	}

	@Override
	public void clear() {
		Object[] keys = map.keySet().toArray();
		for (int i = 0; i < keys.length; i++) {
			remove(keys[i]);
		}

		// 重置所有容器
		map.clear();
		lastAccessedList.clear();
		lastAccessedList = new LinkedList();
		ageList.clear();
		ageList = new LinkedList();

		cacheSize = 0;
		cacheHits = 0;
		cacheMisses = 0;
	}

	@Override
	public int size() {
		// 清理超时对象
		deleteExpiredEntries();

		return map.size();
	}

	@Override
	public boolean isEmpty() {
		// 清理超时对象
		deleteExpiredEntries();

		return map.isEmpty();
	}

	@Override
	public Collection<V> values() {
		// 清理超时对象
		deleteExpiredEntries();

		return new CacheObjectCollection(map.values());
	}

	private final class CacheObjectCollection<V> implements Collection<V> {
		private Collection<CacheObject<V>> cachedObjects;

		private CacheObjectCollection(
				Collection<CacheObject<V>> cachedObjects) {
			this.cachedObjects = new ArrayList<CacheObject<V>>(cachedObjects);
		}

		@Override
		public int size() {
			return cachedObjects.size();
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public boolean contains(Object o) {
			Iterator<V> it = iterator();
			while (it.hasNext()) {
				if (it.next().equals(o)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Iterator<V> iterator() {
			return new Iterator<V>() {
				private final Iterator<CacheObject<V>> it = cachedObjects
						.iterator();

				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public V next() {
					if (it.hasNext()) {
						CacheObject<V> object = it.next();
						if (object == null) {
							return null;
						} else {
							return object.object;
						}
					} else {
						throw new NoSuchElementException();
					}
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public Object[] toArray() {
			Object[] array = new Object[size()];
			Iterator it = iterator();
			int i = 0;
			while (it.hasNext()) {
				array[i] = it.next();
			}
			return array;
		}

		@Override
		public <V> V[] toArray(V[] a) {
			Iterator<V> it = (Iterator<V>) iterator();
			int i = 0;
			while (it.hasNext()) {
				a[i++] = it.next();
			}
			return a;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			Iterator it = c.iterator();
			while (it.hasNext()) {
				if (!contains(it.next())) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean add(V o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends V> coll) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> coll) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> coll) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public boolean containsKey(Object key) {
		// 清理超时对象
		deleteExpiredEntries();

		return map.containsKey(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Iterator<? extends K> i = map.keySet().iterator(); i.hasNext();) {
			K key = i.next();
			V value = map.get(key);
			put(key, value);
		}
	}

	@Override
	public boolean containsValue(Object value) {
		// 清理超时对象
		deleteExpiredEntries();

		if (value == null) {
			return containsNullValue();
		}

		Iterator it = values().iterator();
		while (it.hasNext()) {
			if (value.equals(it.next())) {
				return true;
			}
		}
		return false;
	}

	private boolean containsNullValue() {
		Iterator it = values().iterator();
		while (it.hasNext()) {
			if (it.next() == null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		// 清理超时对象
		deleteExpiredEntries();

		final Map<K, V> result = new HashMap<K, V>();
		for (final Entry<K, CacheObject<V>> entry : map
				.entrySet()) {
			result.put(entry.getKey(), entry.getValue().object);
		}
		return result.entrySet();
	}

	@Override
	public Set<K> keySet() {
		// 清理超时对象
		deleteExpiredEntries();
		return new HashSet<K>(map.keySet());
	}

	/**
	 * 计算缓存对象尺寸
	 */
	private int calculateSize(Object object) {
		if (object instanceof Cacheable) {
			return ((Cacheable) object).getCachedSize();
		} else if (object instanceof String) {
			return CacheSize.sizeOfString((String) object);
		} else if (object instanceof Long) {
			return CacheSize.sizeOfLong();
		} else if (object instanceof Integer) {
			return CacheSize.sizeOfObject() + CacheSize.sizeOfInt();
		} else if (object instanceof Boolean) {
			return CacheSize.sizeOfObject() + CacheSize.sizeOfBoolean();
		} else if (object instanceof long[]) {
			long[] array = (long[]) object;
			return CacheSize.sizeOfObject() + array.length
					* CacheSize.sizeOfLong();
		} else if (object instanceof byte[]) {
			byte[] array = (byte[]) object;
			return CacheSize.sizeOfObject() + array.length;
		}
		// 其他对象采用序列化方法计算尺寸
		else {
			int size = 1;
			NullOutputStream out = null;
			ObjectOutputStream outObj = null;
			try {
				out = new NullOutputStream();
				outObj = new ObjectOutputStream(out);
				outObj.writeObject(object);
				size = out.size();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				if (outObj != null) {
					try {
						outObj.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {

				}
			}
			return size;
		}
	}

	/**
	 * 清理所有超时对象
	 */
	protected void deleteExpiredEntries() {
		if (maxLifetime <= 0)
			return;

		// 删除所有超时过期对象，直到遍历到一个未过期对象
		LinkedListNode node = ageList.getLast();
		if (node == null)
			return;

		long expireTime = System.currentTimeMillis() - maxLifetime;

		while (expireTime > node.timestamp) {
			remove(node.object);

			node = ageList.getLast();

			if (node == null)
				return;
		}
	}

	/**
	 * 如果缓存尺寸超限，开始 LRU 淘汰 “尺寸超限”的定义是当前尺寸达到最大尺寸上限的 97% LRU 淘汰后会腾出至少 10% 的空余空间
	 */
	protected final void cullCache() {
		LogUtil.d(TAG, "cullCache");

		boolean expiredObjHasClear = false;

		// 1. 缓存条目保障
		if (maxNum > 0) {
			// 清理超时对象
			deleteExpiredEntries();
			expiredObjHasClear = true;

			LogUtil.d(TAG, "mapsize : " + map.size());
			LogUtil.d(TAG, "maxCacheNum : " + maxNum);
			if (map.size() > maxNum) {
				long t = System.currentTimeMillis();
				do {
					LinkedListNode node = lastAccessedList.getLast();
					if (node == null)
						return;
					remove(lastAccessedList.getLast().object);
				} while (map.size() > maxNum);
				t = System.currentTimeMillis() - t;

				LogUtil.d(TAG, "Cache " + name
						+ " was overnum, shrinked to maxCacheNum in " + t
						+ "ms.");
			}
		}

		// 2. 缓存尺寸保障
		if (maxSize > 0) {
			int desiredSize = (int) (maxSize * .97);
			if (cacheSize >= desiredSize) {
				if (!expiredObjHasClear)
					deleteExpiredEntries();

				desiredSize = (int) (maxSize * .80);
				if (cacheSize > desiredSize) {
					long t = System.currentTimeMillis();
					do {
						remove(lastAccessedList.getLast().object);
					} while (cacheSize > desiredSize);
					t = System.currentTimeMillis() - t;

					LogUtil.d(TAG, "Cache " + name
							+ " was full, shrinked to 80% in " + t + "ms.");
				}
			}
		}
	}

	private static class CacheObject<V> {
		public V object;

		public int size;

		public LinkedListNode lastAccessedListNode;

		public LinkedListNode ageListNode;

		public int readCount = 0;

		public CacheObject(V object, int size) {
			this.object = object;
			this.size = size;
		}
	}

	/**
	 * 哑巴输出流，只用于统计对象大小
	 */
	private static class NullOutputStream extends OutputStream {

		int size = 0;

		@Override
		public void write(int b) throws IOException {
			size++;
		}

		@Override
		public void write(byte[] b) throws IOException {
			size += b.length;
		}

		@Override
		public void write(byte[] b, int off, int len) {
			size += len;
		}

		public int size() {
			return size;
		}
	}
}
