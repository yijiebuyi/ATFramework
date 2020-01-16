package com.callme.platform.util.http;

import android.os.SystemClock;
import android.text.TextUtils;

import com.callme.platform.util.http.core.HttpEntity;
import com.callme.platform.util.http.core.HttpResponse;
import com.callme.platform.util.http.core.HurlStack;
import com.callme.platform.util.http.core.Request;

import java.io.File;

public class HttpHandler<T> extends PriorityAsyncTask<Object, Object, Void> implements RequestCallBackHandler {

	private final static int UPDATE_START = 1;
	private final static int UPDATE_LOADING = 2;
	private final static int UPDATE_FAILURE = 3;
	private final static int UPDATE_SUCCESS = 4;

	private boolean isUploading = true;

	private RequestCallBack<T> callback;
	private State state = State.WAITING;
	private HurlStack hurlStack;
	private String charset;
	private String handlerId;
	private boolean needCache;

	private String requestUrl;

	private String fileSavePath = null;
	private boolean isDownloadingFile = false;
	private boolean autoResume = false; // Whether the downloading could
										// continue from the point of
										// interruption.
	private boolean autoRename = false;

	private Request request;

	private String requestMethod;

	public HttpHandler(HurlStack hurlStack, String charset, RequestCallBack<T> callback, String id, boolean needCache) {
		this.hurlStack = hurlStack;
		this.callback = callback;
		this.charset = charset;
		this.handlerId = id;
		this.needCache = needCache;
	}

	public State getState() {
		return state;
	}

	public void setRequestCallBack(RequestCallBack<T> callback) {
		this.callback = callback;
	}

	public RequestCallBack<T> getRequestCallBack() {
		return this.callback;
	}

	@Override
	protected Void doInBackground(Object... params) {
		if (this.state == State.CANCELLED || params == null || params.length == 0) {
			return null;
		}

		if (params.length > 3) {// 此状态为下载文件
			fileSavePath = String.valueOf(params[1]);
			isDownloadingFile = fileSavePath != null;
			autoResume = (Boolean) params[2];
			autoRename = (Boolean) params[3];
		}

		try {
			if (this.state == State.CANCELLED) {
				return null;
			}
			request = (Request) params[0];

			requestUrl = request.getUrl();
			if (callback != null) {
				callback.setRequestUrl(requestUrl);
			}

			this.publishProgress(UPDATE_START);
			lastUpdateTime = SystemClock.uptimeMillis();

			ResponseInfo<T> responseInfo = sendRequest(request);

			if (responseInfo != null) {
				this.publishProgress(UPDATE_SUCCESS, responseInfo);
				return null;
			}
		} catch (Exception e) {
			this.publishProgress(UPDATE_FAILURE, e, e.getMessage());
		}
		return null;
	}

	private ResponseInfo<T> sendRequest(Request req) throws Exception {

		while (true) {
			if (autoResume && isDownloadingFile) {
				File downloadFile = new File(fileSavePath);
				long fileLen = 0;// 支持断点续传
				if (downloadFile.isFile() && downloadFile.exists()) {
					fileLen = downloadFile.length();
				}
				if (fileLen > 0) {
					req.addHeader("RANGE", "bytes=" + fileLen + "-");
				}
			}
			try {
				// 缓存查询
				requestMethod = request.getMethodName();
				if (needCache && HttpUtil.sHttpCache.isEnabled(requestMethod)) {
					String result = HttpUtil.sHttpCache.get(requestUrl);
					if (!TextUtils.isEmpty(result)) {
						return new ResponseInfo<T>(null, (T) result, true);
					}
				}
				ResponseInfo<T> responseInfo = null;
				if (!isCancelled()) {
					HttpResponse response = null;
					response = hurlStack.performRequest(req);
					responseInfo = handleResponse(response);
				}
				return responseInfo;
			} catch (Throwable e) {
				Exception exception = new Exception(e);
				exception.initCause(e);
				exception.printStackTrace();
				throw exception;
			}

		}
	}

	private ResponseInfo<T> handleResponse(HttpResponse response) throws Exception {
		if (response == null) {
			throw new Exception("response is null");
		}

		if (isCancelled()) {
			return null;
		}

		int statusCode = response.getResponseCode();

		HttpEntity httpEntity = response.getEntity();
		Object result = null;

		if (statusCode < 300) {// 访问成功,获取数据
			if (httpEntity != null) {
				isUploading = false;
				if (isDownloadingFile) {// 文件下载
					autoResume = autoResume && isSupportRange(response);
					String responseFileName = autoRename ? null : null;// 如果Content-Disposition中有描述下载文件名，需要用给的filename命名下载后的文件，暂不支持
					FileDownloadHandler downloadHandler = new FileDownloadHandler();
					result = downloadHandler.handleEntity(httpEntity, this, fileSavePath, autoResume, responseFileName);
				} else {// json字符串数据获取
					StringDownloadHandler stringDownloadHandler = new StringDownloadHandler();
					result = stringDownloadHandler.handleEntity(httpEntity, this, charset);
					// 数据缓存设置
					if (needCache && HttpUtil.sHttpCache.isEnabled(requestMethod)) {
						HttpUtil.sHttpCache.put(requestUrl, (String) result);
					}
				}
			}
			return new ResponseInfo<T>(response, (T) result, false);
		} else if (statusCode == 301 || statusCode == 302) {// 服务器重定向
			String location = response.getHeader("Location");
			if (location != null) {
				request.setRedirectUrl(location);
				this.sendRequest(request);
			}
		} else {// 失败
			StringDownloadHandler stringDownloadHandler = new StringDownloadHandler();
			String message = stringDownloadHandler.handleEntity(httpEntity, this, charset);
			if (TextUtils.isEmpty(message)) {
				message = response.getResponseMessage();
			}
			publishProgress(UPDATE_FAILURE, statusCode, message);
		}

		return null;
	}

	private boolean isSupportRange(HttpResponse response) {
		if (response == null)
			return false;

		String acceptRange = response.getHeader("Accept-Ranges");
		if (acceptRange != null) {
			return "bytes".equals(acceptRange);
		}

		String contentRange = response.getHeader("Content-Range");
		if (contentRange != null) {
			return contentRange.startsWith("bytes");
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onProgressUpdate(Object... values) {
		if (this.state == State.CANCELLED || values == null || values.length == 0 || callback == null)
			return;

		switch ((Integer) values[0]) {
		case UPDATE_START:
			this.state = State.STARTED;
			callback.onStart();
			break;

		case UPDATE_LOADING:
			if (values.length != 3) {
				return;
			}

			this.state = State.LOADING;
			callback.onLoading(Long.valueOf(String.valueOf(values[1])), Long.valueOf(String.valueOf(values[2])),
					isUploading);
			break;

		case UPDATE_FAILURE:
			if (values.length != 3)
				return;

			this.state = State.FAILURE;
			if (values[1] instanceof ResponseInfo<?>) {
				callback.onFailure(((ResponseInfo<T>) values[1]).statusCode, (String) values[2]);
			} else if (values[1] instanceof Integer) {
				callback.onFailure(Integer.parseInt(values[1].toString()), (String) values[2]);
			} else if (values[1] instanceof Exception) {
				callback.onFailure(-1000, ((Exception) values[1]).getMessage());
			}

			// 取消当前失败的任务并移除
			if (HttpUtil.mHandlerMap.get(handlerId) != null) {
				HttpUtil.mHandlerMap.get(handlerId).cancel();
				HttpUtil.mHandlerMap.remove(handlerId);
			}
			break;

		case UPDATE_SUCCESS:
			if (values.length != 2)
				return;

			this.state = State.SUCCESS;
			callback.onSuccess((ResponseInfo<T>) values[1]);
			// 取消当前失败的任务并移除
			if (HttpUtil.mHandlerMap.get(handlerId) != null) {
				HttpUtil.mHandlerMap.get(handlerId).cancel();
				HttpUtil.mHandlerMap.remove(handlerId);
			}
			break;
		default:
			break;
		}
	}

	private long lastUpdateTime;

	@Override
	public boolean updateProgress(long total, long current, boolean forceUpdateUI) {
		if (callback != null && this.state != State.CANCELLED) {
			if (forceUpdateUI) {
				this.publishProgress(UPDATE_LOADING, total, current);
			} else {
				long currTime = SystemClock.uptimeMillis();
				if (currTime - lastUpdateTime >= callback.getRate()) {
					lastUpdateTime = currTime;
					this.publishProgress(UPDATE_LOADING, total, current);
				}
			}
		}
		return this.state != State.CANCELLED;
	}

	@Override
	public void cancel() {
		this.state = State.CANCELLED;
		if (hurlStack != null) {
			try {
				hurlStack.disconnect();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		if (!this.isCancelled()) {
			try {
				this.cancel(true);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		if (callback != null) {
			callback.onCancelled();
		}
		hurlStack = null;
	}

	public enum State {
		WAITING(0), STARTED(1), LOADING(2), FAILURE(3), CANCELLED(4), SUCCESS(5);
		private int value = 0;

		State(int value) {
			this.value = value;
		}

		public static State valueOf(int value) {
			switch (value) {
			case 0:
				return WAITING;
			case 1:
				return STARTED;
			case 2:
				return LOADING;
			case 3:
				return FAILURE;
			case 4:
				return CANCELLED;
			case 5:
				return SUCCESS;
			default:
				return FAILURE;
			}
		}

		public int value() {
			return this.value;
		}
	}
}
