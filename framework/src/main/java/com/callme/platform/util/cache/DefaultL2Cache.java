package com.callme.platform.util.cache;

import com.callme.platform.util.FileUtil;
import com.callme.platform.util.IOUtils;
import com.callme.platform.util.LogUtil;
import com.callme.platform.util.Md5Util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;

/*
 * Description：2级缓存封装，磁盘缓存
 */

public class DefaultL2Cache<K, V> {
	private static final String TAG = "DefaultL2Cache";

	private long maxSize;
	protected long maxLifetime;
	private int cacheSize = 0;
	private String name;

	// L2 存放目录
	private File l2Dir;

	public DefaultL2Cache(String name, long maxsize, long maxlifetime) {
		this.name = name;
		this.maxSize = maxsize;
		this.maxLifetime = maxlifetime;
		this.l2Dir = new File(FileUtil.INTERNAL_CACHE_DIR, this.name);
		if (l2Dir != null && !l2Dir.exists()) {
			l2Dir.mkdirs();
		}
	}

	public void put(String key, Cacheable cacheable) {
		if (cacheable == null)
			return;

		LogUtil.d(TAG, "Put : " + key);

		String md5key = Md5Util.getMD5(key);
		byte[] data = cacheable.serialize();
		if (data != null)
			saveIntoDisk(md5key, data);
	}

	public Cacheable get(String key) {
		LogUtil.d(TAG, "Get : " + key);

		String md5key = Md5Util.getMD5(key);
		byte[] data = loadFromDisk(md5key);
		if (data == null)
			return null;

		LogUtil.d(TAG, "Len(" + data.length + ") : " + key);
		return newCacheableInstance(data);
	}

	public void remove(String key) {
		String md5key = Md5Util.getMD5(key);
		File file = new File(l2Dir, md5key);
		if (file.exists())
			file.delete();
	}

	public boolean containsKey(String key) {
		String md5key = Md5Util.getMD5(key);
		File file = new File(l2Dir, md5key);
		return file.exists();
	}

	/**
	 * 从 raw data 生成相应的 cacheable 对象
	 * 
	 * @param data
	 */
	private Cacheable newCacheableInstance(byte[] data) {
		if (data == null || data.length == 0)
			return null;

		Cacheable cacheable = new JsonData(new String(data));
		return cacheable;
	}

	/**
	 * 保存到文件系统
	 * 
	 * @param key
	 * @param data
	 */
	private void saveIntoDisk(String key, byte[] data) {
		LogUtil.d(TAG, "saveIntoDisk : " + key);

		File file = new File(l2Dir, key);
		FileOutputStream fos = null;
		try {
			file.createNewFile();
			fos = new FileOutputStream(file);

			fos.write(data, 0, data.length);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 从文件系统载入
	 * 
	 * @param imageName
	 * @return
	 */
	private byte[] loadFromDisk(String key) {
		byte[] data = null;
		FileInputStream fis = null;
		try {
			File file = new File(l2Dir, key);
			if (file.exists()) {
				LogUtil.d(TAG, "Found : " + file.getName());
				fis = new FileInputStream(file);
				data = IOUtils.toByteArray(fis);
			}
		} catch (Exception e) {
			LogUtil.d(TAG, e.getMessage());
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		return data;
	}

	/**
	 * 清理 L2 缓存
	 */
	public void shrink() {
		// 遍历 L2 文件，删除生存超时的，同时统计空间占用
		if (l2Dir == null || !l2Dir.isDirectory())
			return;

		File[] files = l2Dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile();
			}
		});

		long now = System.currentTimeMillis();
		long totalFileSize = 0;
		if (files != null && files.length > 0) {
			// 排序
			Arrays.sort(files, new FileComparator());
			long expireTime = now - maxLifetime;
			for (File file : files) {
				if (expireTime > file.lastModified())
					file.delete();
				else
					totalFileSize += file.length();
			}
		}

		// 维护空间占用，直至空余30%空间
		if (totalFileSize >= maxSize * .95) {
			long desiredSize = (long) (maxSize * .70);

			if (files != null) {
				for (int i = files.length - 1; i >= 0
						&& totalFileSize > desiredSize; i--) {
					File lastFile = files[i];
					if (lastFile != null) {
						long fileSize = lastFile.length();
						lastFile.delete();
						totalFileSize -= fileSize;
					}
				}
			}
		}

		now = System.currentTimeMillis() - now;
		LogUtil.d(TAG, "L2 Cache image was full, shrinked to 70% in " + now
				+ "ms.");
	}

	public void clear() {
		// 遍历 L2 文件，删除生存超时的，同时统计空间占用
		if (l2Dir == null || !l2Dir.isDirectory())
			return;

		File[] files = l2Dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile();
			}
		});

		if (files != null && files.length > 0) {
			for (File file : files) {
				file.delete();
			}
		}
	}

	private class FileComparator implements Comparator<File> {
		private Collator mCollator;

		public FileComparator() {
			mCollator = Collator.getInstance();
		}

		@Override
		public int compare(File file1, File file2) {
			long len1 = file1.length();
			long len2 = file2.length();

			if (len1 < len2) {
				return -1;
			} else if (len1 > len2) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public long getMaxLifetime() {
		return maxLifetime;
	}

	/**
	 * 设置缓存系统最大尺寸
	 */
	public void setMaxSize(long size) {
		this.maxSize = size;
	}

	/**
	 * 设置缓存系统的最大生命周期
	 */
	public void setMaxLifetime(long lifetime) {
		this.maxLifetime = lifetime;
	}
}
