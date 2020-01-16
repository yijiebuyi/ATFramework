package com.callme.platform.util.cache;

import java.util.Collection;
import java.util.Map;

/**
 * 常见对象的尺寸统计帮助类
 */
public class CacheSize {
	public static int sizeOfObject() {
		return 4;
	}

	public static int sizeOfString(String string) {
		if (string == null) {
			return 0;
		}
		return 4 + string.getBytes().length;
	}

	public static int sizeOfInt() {
		return 4;
	}

	public static int sizeOfChar() {
		return 2;
	}

	public static int sizeOfBoolean() {
		return 1;
	}

	public static int sizeOfLong() {
		return 8;
	}

	public static int sizeOfDouble() {
		return 8;
	}

	public static int sizeOfDate() {
		return 12;
	}

	public static int sizeOfMap(Map<String, String> map) {
		if (map == null) {
			return 0;
		}

		// Base map object -- should be something around this size.
		int size = 36;

		// Add in size of each value
		for (Map.Entry<String, String> entry : map.entrySet()) {
			size += sizeOfString(entry.getKey());
			size += sizeOfString(entry.getValue());
		}
		return size;
	}

	public static int sizeOfCollection(Collection list) {
		if (list == null) {
			return 0;
		}
		// Base list object (approximate)
		int size = 36;
		// Add in size of each value
		Object[] values = list.toArray();
		for (int i = 0; i < values.length; i++) {
			Object obj = values[i];
			if (obj instanceof String) {
				size += sizeOfString((String) obj);
			} else if (obj instanceof Long) {
				size += sizeOfLong() + sizeOfObject();
			} else {
				size += ((Cacheable) obj).getCachedSize();
			}
		}
		return size;
	}
}
