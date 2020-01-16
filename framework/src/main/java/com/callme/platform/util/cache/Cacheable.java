package com.callme.platform.util.cache;

public interface Cacheable extends java.io.Serializable {
	byte TYPE_FILE = 1;

	/**
	 * 返回对象类型
	 * 
	 * @return
	 */
    int getCachedType();

	/**
	 * 返回对象的大概尺寸
	 * 
	 * 缓存对象尺寸用来估算缓存系统占用空间大小
	 */
    int getCachedSize();

	/**
	 * 缓存对象自清理回收
	 */
    void recycle();

	/**
	 * 序列化
	 */
    byte[] serialize();
}
