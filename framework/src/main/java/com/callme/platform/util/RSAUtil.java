package com.callme.platform.util;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

/**
 * @功能描述： RSA非对称加密
 * @作者 mikeyou
 * @创建日期：2017/10/6
 * 
 * @修改人： 
 * @修改描述：
 * @修改日期
 */
public class RSAUtil {
	private final static String KEY_ALGORITHM = "RSA";
	private final static String ALGORITHM = "RSA/ECB/PKCS1Padding";// algorigthm/mode/fill_mode
	private final static String CHAR_SET = "utf-8";
	
	public final static String MODULUS = "r064mYANMiiUeFUvePpIQchAxv8J/zJERUrNFQ2FV5Q3fSDp1nlxj3dHf8pIZeYl1ivoigY745iV16Nk/"
			+ "2afID3RDPOUNSGaBG5Euc0p6iyV/oiAkg0vH8m46F2tbBvAoGnT05Ia88BDNtCH0BbGHtiDaCFr6JaRX47zsiJGHpM=";
	public final static String EXPONENT = "AQAB";
	
	/**
	 * * 生成密钥对 *
	 * 
	 * @return KeyPair *
	 * @throws EncryptException
	 */
	public static KeyPair generateKeyPair() throws Exception {
		try {
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
			final int KEY_SIZE = 1024;//1024 used for normal securities
			keyPairGen.initialize(KEY_SIZE, new SecureRandom());
			KeyPair keyPair = keyPairGen.generateKeyPair();
			return keyPair;
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	/**
	 * 获取公钥
	 * @param key
	 * @return
	 */
	public static RSAPublicKeySpec getRSAPublicKeySpec(Key key) {
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			return keyFactory.getKeySpec(key, RSAPublicKeySpec.class);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	/**
	 * 获取私钥
	 * @param key
	 * @return
	 */
	public static RSAPrivateKeySpec getRSAPrivateKeySpec(Key key) {
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			return keyFactory.getKeySpec(key, RSAPrivateKeySpec.class);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	/**
	 * Generate public key by (modulus, exponent)
	 * @param modulus
	 * @param exponent
	 * @return publicKey
	 * @throws IOException
	 */
	public static PublicKey generatePublicKey(BigInteger modulus, BigInteger exponent) throws IOException{
		try {
		    //Get Public Key
		    RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, exponent);
		    KeyFactory fact = KeyFactory.getInstance(KEY_ALGORITHM);
		    PublicKey publicKey = fact.generatePublic(rsaPublicKeySpec);
		    		    
		    return publicKey;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * @param modulus The modulus without decoding by base64
	 * @param exponent The exponent without decoding by base64
	 * @return
	 * @throws IOException
	 */
	public static PublicKey generatePublicKey(String modulus, String exponent) throws IOException{
		return generatePublicKey(new BigInteger(modulus), new BigInteger(modulus));
	}
	
	/**
	 * @param modulus The modulus with decoding by base64
	 * @param exponent The exponent without decoding by base64
	 * @param base64
	 * @return
	 * @throws IOException
	 */
	public static PublicKey generatePublicKeyBase64(String modulus, String exponent) throws IOException{
		byte[] m = Base64.decode(modulus, Base64.DEFAULT);
		byte[] e = Base64.decode(exponent, Base64.DEFAULT);
		return generatePublicKey(new BigInteger(m), new BigInteger(e));
	}
	
	/**
	 * Generate private key by (modulus, exponent)
	 * @param modulus
	 * @param exponent
	 * @return PrivateKey
	 * @throws IOException
	 */
	public static PrivateKey generatePrivateKey(BigInteger modulus, BigInteger exponent) throws IOException{
		try {
		    //Get Private Key
		    RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(modulus, exponent);
		    KeyFactory fact = KeyFactory.getInstance(KEY_ALGORITHM);
		    PrivateKey privateKey = fact.generatePrivate(rsaPrivateKeySpec);
		    		    
		    return privateKey;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static PrivateKey generatePrivateKey(String modulus, String exponent) throws IOException{
		return generatePrivateKey(new BigInteger(modulus), new BigInteger(modulus));
	}

	/**
	 * Encrypt Data by block
	 * @param key
	 * @param data
	 * @throws IOException
	 */
	public static byte[] encryptByBlock(PublicKey pk, byte[] data) throws Exception {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, pk);
			int blockSize = cipher.getBlockSize();
			int outputSize = cipher.getOutputSize(data.length);
			int leavedSize = data.length % blockSize;
			int blocksSize = leavedSize != 0 ? data.length / blockSize + 1
					: data.length / blockSize;
			byte[] raw = new byte[outputSize * blocksSize];
			int i = 0;
			while (data.length - i * blockSize > 0) {
				if (data.length - i * blockSize > blockSize) {
					cipher.doFinal(data, i * blockSize, blockSize, raw, i
							* outputSize);
				} else{
					cipher.doFinal(data, i * blockSize, data.length - i
							* blockSize, raw, i * outputSize);
				}
				i++;
			}
			return raw;
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public static byte[] encryptByBlock(PublicKey pk, String data) throws Exception {
		if (data == null) {
			return null;
		}
		
		return encryptByBlock(pk, data.getBytes(CHAR_SET));
	}

	/**
	 * Decrypt Data by block
	 * @param key
	 * @param data
	 * @throws IOException
	 */
	public static byte[] decryptByBlock(PrivateKey pk, byte[] raw) throws Exception {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, pk);
			int blockSize = cipher.getBlockSize();
			ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
			int j = 0;

			while (raw.length - j * blockSize > 0) {
				bout.write(cipher.doFinal(raw, j * blockSize, blockSize));
				j++;
			}
			return bout.toByteArray();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public static byte[] decryptByBlock(PrivateKey pk, String raw) throws Exception {
		if (raw == null) {
			return null;
		}
		
		return decryptByBlock(pk, raw.getBytes(CHAR_SET));
	}
	
	/**
	 * Encrypt byte Data
	 * @param key
	 * @param data
	 * @throws IOException
	 */
	public static byte[] encryptByte(PublicKey pubKey, byte data[]) {
		byte[] encryptedData = null;
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			encryptedData = cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		return encryptedData;
	}
	
	public static byte[] encryptByte(PublicKey pubKey, String data) {
		if (data == null) {
			return null;
		}
		
		try {
			return encryptByte(pubKey, data.getBytes(CHAR_SET));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Encrypt String Data
	 * @param key
	 * @param data
	 * @throws IOException
	 */
	public static String encrypt(PublicKey pubKey, byte[] data) {
		try {
			return new String(encryptByte(pubKey, data), CHAR_SET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String encrypt(PublicKey pubKey, String data) {
		try {
			return new String(encryptByte(pubKey, data), CHAR_SET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Decrypt byte Data
	 * @param key
	 * @param data
	 * @throws IOException
	 */
	public static byte[] decryptByte(PrivateKey privateKey, byte[] data) {
		byte[] descryptedData = null;
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			descryptedData = cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		return descryptedData;
	}
	
	public static byte[] decryptByte(PrivateKey privateKey, String data) {
		if (data == null) {
			return null;
		}
		
		try {
			return decryptByte(privateKey, data.getBytes(CHAR_SET));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Decrypt String Data
	 * @param key
	 * @param data
	 * @throws IOException
	 */
	public static String decrypt(PrivateKey privateKey, byte[] data) {
		try {
			return new String(decryptByte(privateKey, data), CHAR_SET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String decrypt(PrivateKey privateKey, String data) {
		try {
			return new String(decryptByte(privateKey, data), CHAR_SET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
