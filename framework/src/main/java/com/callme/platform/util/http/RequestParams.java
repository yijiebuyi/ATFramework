package com.callme.platform.util.http;


import com.callme.platform.util.EncryptUtil;
import com.callme.platform.util.EncryptUtil.RsaDataBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * 
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class RequestParams implements Serializable {
    private static String ENCODING = "UTF-8";

    public static String USER_TOKEN = ""; // 用户token
    protected ConcurrentHashMap<String, Object> urlParams;
    protected ConcurrentHashMap<String, FileWrapper> fileParams;
    protected List<Object> singleParams;

    private static final char NAME_VALUE_CONNECT = '&';
    private static final String NAME_VALUE_SEPARATOR = "=";

    public RequestParams() {
        init();
    }

    public RequestParams(String key, String value) {
        init();
        put(key, value);
    }

    private void init() {
        urlParams = new ConcurrentHashMap<String, Object>();
        fileParams = new ConcurrentHashMap<String, FileWrapper>();
        singleParams = new ArrayList<Object>();
    }

    public void put(Object value) {
        if (value != null) {
            singleParams.add(value);
        }
    }

    /**
     * @param key
     * @param value
     */
    public void put(String key, String value) {
        if (key != null && value != null) {
            urlParams.put(key, value);
        }
    }

    /**
     * @param key
     * @param value
     */
    public void put(String key, Object value) {
        if (key != null && value != null) {
            urlParams.put(key, value);
        }
    }

    /**
     * @param key
     * @param file
     */
    public void put(String key, File file) {
        try {
            put(key, new FileInputStream(file), file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a file to the request.
     *
     * @param key         the key name for the new param.
     * @param file        the file to add.
     * @param contentType the content type of the file, eg. application/json
     * @throws FileNotFoundException throws if wrong File argument was passed
     */
    public void put(String key, File file, String contentType)
            throws FileNotFoundException {
        if (key != null && file != null) {
            fileParams.put(key, new FileWrapper(file, contentType));
        }
    }

    /**
     * @param key
     * @param stream
     * @param fileName
     */
    public void put(String key, InputStream stream, String fileName) {
        put(key, stream, fileName, null);
    }

    /**
     * @param key
     * @param stream
     * @param fileName
     * @param contentType
     */
    public void put(String key, InputStream stream, String fileName,
                    String contentType) {
        if (key != null && stream != null) {
            fileParams.put(key, new FileWrapper(stream, fileName, contentType));
        }
    }

    /**
     * 获取数据
     *
     * @param key
     * @return
     */
    public Object get(String key) {
        if (key != null) {
            return urlParams.get(key);
        }
        return null;
    }

    /**
     * 删除数据
     *
     * @param key
     */
    public void remove(String key) {
        urlParams.remove(key);
        fileParams.remove(key);
    }

    /**
     * 获取get请求的参数，?x=1&y=2的形式
     *
     * @return
     */
    public String formatGetParam(boolean isGetReq) {
        if (fileParams != null && fileParams.size() > 0) {
            return null;
        }
        if (urlParams != null) {
            final StringBuilder result = new StringBuilder();
            if (isGetReq) {
                result.append("?");
            }
            for (Entry<String, Object> entry : urlParams
                    .entrySet()) {
                final String encodedName = encodeFormFields(entry.getKey(),
                        ENCODING);
                final String encodedValue = encodeFormFields(entry.getValue(),
                        ENCODING);

                result.append(encodedName);
                if (encodedValue != null) {
                    result.append(NAME_VALUE_SEPARATOR);
                    result.append(encodedValue);
                }

                result.append(NAME_VALUE_CONNECT);
            }

            result.append("token");
            result.append(NAME_VALUE_SEPARATOR);
            result.append(USER_TOKEN);

            if (result.charAt(result.length() - 1) == NAME_VALUE_CONNECT) {
                result.deleteCharAt(result.length() - 1);
            }

            if (result.charAt(result.length() - 1) == '?') {
                result.deleteCharAt(result.length() - 1);
            }
            return result.toString();
        }

        return null;
    }

    private String encodeFormFields(final Object content, final String charset) {
        if (content == null) {
            return null;
        }
        try {
            return URLEncoder.encode(String.valueOf(content), charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成post参数，二进制形式
     *
     * @return
     */
    public byte[] formatPostParam() {
        if (fileParams != null && fileParams.size() > 0) {
            return null;
        } else if (urlParams != null && urlParams.size() > 0) {
            Object obj = urlParams.get(HttpUtil.KEY_RSA_ENCRYPT);
            boolean needRsaEncrypt = false;
            if (obj instanceof Boolean) {
                needRsaEncrypt = (Boolean) obj;
                urlParams.remove(HttpUtil.KEY_RSA_ENCRYPT);
            }

            if (needRsaEncrypt) {
                //RSA encrypt
                JSONObject p = new JSONObject(urlParams);
                //{"d":"{\"UserAccount\":\"xiaoming\",\"Password\":\"123456\"}"}
                RsaDataBean srcData = EncryptUtil.buildRsaData(p.toString());
                String rsaData = EncryptUtil.getRsaTrasData(srcData);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    baos.write(rsaData.getBytes());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                return baos.toByteArray();
            } else {
                JSONObject p = new JSONObject(urlParams);
                return p.toString().getBytes();
            }
        } else if (singleParams != null && singleParams.size() > 0) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] res;
            try {
                for (Object obj : singleParams) {
                    JSONObject p;
                    try {
                        p = new JSONObject(obj.toString());
                        baos.write(p.toString().getBytes());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                res = baos.toByteArray();
            } finally {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return res;
        }

        return null;
    }

    public int getUploadFileLength() {
        if (fileParams != null && fileParams.size() > 0) {
            Set<Entry<String, FileWrapper>> sets = fileParams.entrySet();
            Iterator<Entry<String, FileWrapper>> it = sets.iterator();
            if (it.hasNext()) {
                Entry<String, FileWrapper> item = it.next();
                FileWrapper file = item.getValue();
                InputStream in = file.inputStream;
                if (in != null) {
                    try {
                        return in.available();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return 0;
    }

    /**
     * 生成文件上传时的参数
     *
     * @return
     */
    public byte[] formatUploadParam() {
        if (fileParams != null && fileParams.size() > 0) {
            Set<Entry<String, FileWrapper>> sets = fileParams.entrySet();
            Iterator<Entry<String, FileWrapper>> it = sets.iterator();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream in = null;
            int len = 0;
            byte[] buf = new byte[4 * 1024];
            try {
                while (it.hasNext()) {// 暂时支持单个文件上传
                    Entry<String, FileWrapper> item = it.next();
                    FileWrapper file = item.getValue();
                    in = file.inputStream;
                    if (in != null) {
                        try {
                            while ((len = in.read(buf)) != -1) {
                                baos.write(buf, 0, len);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                byte[] res = baos.toByteArray();
                return res;
            } finally {
                try {
                    baos.close();
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return null;
    }

    public boolean isStringParam() {
        return !(fileParams != null && fileParams.size() > 0);
    }

    private static class FileWrapper {
        public InputStream inputStream;
        public String fileName;
        public String contentType;

        public FileWrapper(InputStream inputStream, String fileName,
                           String contentType) {
            this.inputStream = inputStream;
            this.fileName = fileName;
            this.contentType = contentType;
        }

        public FileWrapper(File file, String contentType) {
            try {
                inputStream = new FileInputStream(file);
                fileName = file.getAbsolutePath() + File.separator
                        + file.getName();
                this.contentType = contentType;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        public String getFileName() {
            if (fileName != null) {
                return fileName;
            } else {
                return "nofilename";
            }
        }
    }

}