/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.callme.platform.util.http.core;

import android.util.Log;

import com.callme.platform.util.IOUtils;
import com.callme.platform.util.http.RequestParams;
import com.callme.platform.util.http.core.Request.Method;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;


/*
 * 
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：http访问层HttpURLConnection实现类
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class HurlStack extends HttpStack {

	private static final String HERDER_CONTENT_LENGTH = "Content-Length";

	/**
	 * An interface for transforming URLs before use.
	 */
	public interface UrlRewriter {
		/**
		 * Returns a URL to use instead of the provided one, or null to indicate
		 * this URL should not be used at all.
		 */
        String rewriteUrl(String originalUrl);
	}

	private final UrlRewriter mUrlRewriter;
	private SSLSocketFactory mSslSocketFactory;
	private HttpURLConnection connection;
	private int retry;

	public void setRetry(int retry){
		this.retry = retry;
	}

	public void setSslSocketFactory(SSLSocketFactory ssf){
		this.mSslSocketFactory = ssf;
	}
	public HurlStack() {
		this(null);
	}

	/**
	 * @param urlRewriter
	 *            Rewriter to use for request URLs
	 */
	public HurlStack(UrlRewriter urlRewriter) {
		this(urlRewriter, null);
	}

	/**
	 * @param urlRewriter
	 *            Rewriter to use for request URLs
	 * @param sslSocketFactory
	 *            SSL factory to use for HTTPS connections
	 */
	public HurlStack(UrlRewriter urlRewriter, SSLSocketFactory sslSocketFactory) {
		mUrlRewriter = urlRewriter;
		mSslSocketFactory = sslSocketFactory;
		retry = 1;
	}


	private HttpResponse doRequest(Request request) throws IOException{
		String url = request.getUrl();
		HashMap<String, String> map = new HashMap<String, String>();
		// 接口请求前的回调，请求前的处理
		if (requestInterceptor != null) {
			requestInterceptor.process(request);
		}
		map.putAll(request.getHeaders());
		if (mUrlRewriter != null) {
			String rewritten = mUrlRewriter.rewriteUrl(url);
			if (rewritten == null) {
				throw new IOException("URL blocked by rewriter: " + url);
			}
			url = rewritten;
		}
		URL parsedUrl = new URL(url);
		connection = openConnection(parsedUrl, request);
		//检测是否是上传文件，上传文件需要添加Content-Length为文件的长度
		RequestParams param = request.getmParams();
		if(param != null && !param.isStringParam()){//上传文件
			if(map.containsKey(HERDER_CONTENT_LENGTH)){
				map.remove(HERDER_CONTENT_LENGTH);
			}
			
			map.put(HERDER_CONTENT_LENGTH, String.valueOf(param.getUploadFileLength()));
		}
		// 添加请求头
		for (String headerName : map.keySet()) {
			connection.addRequestProperty(headerName, map.get(headerName));
		}
		setConnectionParametersForRequest(connection, request);
		int responseCode = -1;
		String responseMsg = "";
		try {
			responseCode = connection.getResponseCode();
			responseMsg = connection.getResponseMessage();
		} catch (IOException e) {
			responseCode = connection.getResponseCode();
			responseMsg = connection.getResponseMessage();
		}
		HttpResponse response = new HttpResponse(responseCode, responseMsg);
		response.setEntity(entityFromConnection(connection));
		// 获取请求返回的header放入response
		for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
			if (header.getKey() != null) {
				List<String> values = header.getValue();
				if (values != null) {
					StringBuilder builder = new StringBuilder();
					for (String value : values) {
						builder.append(value);
						builder.append(';');
					}
					if (builder.charAt(builder.length() - 1) == ';') {
						builder.deleteCharAt(builder.length() - 1);
					}

					response.addHeader(header.getKey(), builder.toString());
				}

			}
		}

		// 得到接口返回后的回调，请求后的处理
		if (responseInterceptor != null) {
			responseInterceptor.process(response, connection);
		}
		return response;

	}
	@Override
	public HttpResponse performRequest(Request request) throws IOException{
		if(retry > 0){
			for (int time = 0; time < retry;time++){
				HttpResponse re = doRequest(request);
				if (re.getResponseCode() > 0){
					return re;
				}
			}
		}

		return doRequest(request);
	}

	/**
	 * Initializes an {@link HttpEntity} from the given
	 * {@link HttpURLConnection}.
	 * 
	 * @param connection
	 * @return an HttpEntity populated with data from <code>connection</code>.
	 */
	private static HttpEntity entityFromConnection(HttpURLConnection connection) {
		HttpEntity entity = new HttpEntity();
		InputStream inputStream;
		try {
			inputStream = connection.getInputStream();
		} catch (IOException ioe) {
			inputStream = connection.getErrorStream();
		}
		entity.setContent(inputStream);
		entity.setContentLength(connection.getContentLength());
		entity.setContentEncoding(connection.getContentEncoding());
		entity.setContentType(connection.getContentType());
		return entity;
	}

	/**
	 * Create an {@link HttpURLConnection} for the specified {@code url}.
	 */
	protected HttpURLConnection createConnection(URL url) throws IOException {
		return (HttpURLConnection) url.openConnection();
	}

	/**
	 * Opens an {@link HttpURLConnection} with parameters.
	 * 
	 * @param url
	 * @return an open connection
	 * @throws Exception
	 */
	private HttpURLConnection openConnection(URL url, Request request) throws IOException {

		// 当请求为https的处理
		if ("https".equals(url.getProtocol())) {
			if(mSslSocketFactory != null){
				HttpsURLConnection.setDefaultSSLSocketFactory(mSslSocketFactory);
			}

		}

		HttpURLConnection connection = createConnection(url);
		
		int timeoutMs = request.getTimeoutMs();
		connection.setConnectTimeout(timeoutMs);
		connection.setReadTimeout(timeoutMs);
		connection.setUseCaches(false);
		connection.setDoInput(true);

		return connection;
	}

	static void setConnectionParametersForRequest(HttpURLConnection connection, Request request) throws IOException{
		switch (request.getMethod()) {
		case Method.GET:
			connection.setRequestMethod("GET");
			break;
		case Method.POST:
			connection.setRequestMethod("POST");
			addBodyIfExists(connection, request);
			break;
		default:
		}
	}

	/**
	 * 添加post请求的参数数据
	 * @param connection
	 * @param request
	 * @throws IOException
	 */
	private static void addBodyIfExists(HttpURLConnection connection, Request request) throws IOException{
		RequestParams param = request.getmParams();
		byte[] body = request.getBody();
		if(param != null && !param.isStringParam()){//上传文件
			body = param.formatUploadParam();
		}
		
		if (body != null) {
			try{//此处当connection已经连接时再调用setDoOutput方法会抛IllegalStateException异常，
				//HttpURLConnection类中无法检测是否连接，用异常处理方式跳过这个设定
				connection.setDoOutput(true);
			}catch (IllegalStateException e){
				Log.w("com.callme.platform.util.http.core.HurlStack", "connection.setDoOutput IllegalStateException!");
				e.printStackTrace();
			}
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.write(body);
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * 断开请求
	 */
	public void disconnect(){
		if(connection != null){
			connection.disconnect();
		}
	}
}
