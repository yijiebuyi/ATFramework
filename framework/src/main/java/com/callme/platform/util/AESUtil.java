package com.callme.platform.util;

import android.util.Base64;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {
	private final static String CHAR_SET = "utf-8";
	private final static String KEY_ALGORITHM = "AES";// 算法名称
	private final static String ALGORITHM = "AES/CBC/PKCS7Padding";// 加解密算法/模式/填充方式
	private Key key;
	private Cipher cipher;
	private final static byte[] IV = { 0x30, 0x31, 0x30, 0x32, 0x30, 0x33,
			0x30, 0x34, 0x30, 0x35, 0x30, 0x36, 0x30, 0x37, 0x30, 0x38 };

	private void init(byte[] keyBytes) throws NoSuchAlgorithmException,
			NoSuchPaddingException {
		// 如果密钥不足16位，那么就补足
		int base = 16;
		if (keyBytes.length % base != 0) {
			int groups = keyBytes.length / base
					+ (keyBytes.length % base != 0 ? 1 : 0);
			byte[] temp = new byte[groups * base];
			Arrays.fill(temp, (byte) 0);
			System.arraycopy(keyBytes, 0, temp, 0, keyBytes.length);
			keyBytes = temp;
		}
		// 转化成JAVA的密钥格式
		key = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
		cipher = Cipher.getInstance(ALGORITHM);
	}

	/**
	 * 加密方法
	 * 
	 * @param contentBytes
	 *            原始数据
	 * @param keyBytes
	 *            加密密钥
	 * @return
	 * @throws Exception
	 */
	private byte[] encryptBytes(byte[] contentBytes, byte[] keyBytes)
			throws Exception {
		init(keyBytes);
		cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV));
		return cipher.doFinal(contentBytes);
	}

	/**
	 * 解密方法
	 * 
	 * @param encryptedBytes
	 *            密文
	 * @param keyBytes
	 *            解密密钥
	 * @return
	 * @throws Exception
	 */
	private byte[] decryptBytes(byte[] encryptedBytes, byte[] keyBytes)
			throws Exception {
		init(keyBytes);
		cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV));
		return cipher.doFinal(encryptedBytes);
	}

	public static String encrypt(byte[] contentBytes, byte[] keyBytes)
			throws Exception {
		byte[] bytes = new AESUtil().encryptBytes(contentBytes, keyBytes);
		return Base64.encodeToString(bytes, 0);
	}

	/**
	 * 加密
	 * 
	 * @param content
	 *            原始数据
	 * @param key
	 *            秘钥
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String content, String key) throws Exception {
		byte[] contentBytes = content.getBytes(CHAR_SET);
		byte[] keyBytes = key.getBytes(CHAR_SET);
		return encrypt(contentBytes, keyBytes);
	}

	public static String decrypt(String encryptedData, byte[] keyBytes)
			throws Exception {
		byte[] encryptedBytes = Base64.decode(encryptedData, 0);
		byte[] bytes = new AESUtil().decryptBytes(encryptedBytes, keyBytes);
		return new String(bytes, CHAR_SET);
	}

	/**
	 * 解密
	 * 
	 * @param encryptedData
	 *            密文
	 * @param key
	 *            秘钥
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String encryptedData, String key)
			throws Exception {
		byte[] keyBytes = key.getBytes(CHAR_SET);
		return decrypt(encryptedData, keyBytes);
	}
}
