package com.callme.platform.widget.pulltorefresh;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.callme.platform.util.CmHttpUtil;
import com.callme.platform.util.CmRequestListener;
import com.callme.platform.util.FileUtil;
import com.callme.platform.util.http.HttpHandler;
import com.callme.platform.util.http.HttpUtil;
import com.callme.platform.widget.pulltorefresh.PullToRefreshBase.Mode;

import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

/**
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司 版权所有
 * <p>
 * 功能描述：带有缓存的数据适配器 首先读取本地缓存，再从网络请求数据(不支持分页,不支持下拉刷新)
 * <p>
 * 修改人： 修改描述： 修改日期
 *
 * @param <B> 数据对象bean
 * @param <H> item对象 holder
 */
public abstract class AbsSwipeCacheAdapter<B, H> extends AbsSwipeAdapter<B, H> {
    private static final int REQUEST_FAILED = -1;
    private static final int REQUEST_SUCC = 0;

    private String mCacheRequestId;
    private String mHttpRequestId;
    private String mCachePath;

    private OnDataLoadListener mDataLoadListener;

    public AbsSwipeCacheAdapter(Context context,
                                PullToRefreshSwipeListView listView,
                                AbsSwipeCacheRequestParams requestParams) {
        super(context, listView);
        mParams = requestParams.httpReqParams;
        mUrl = requestParams.url;
        mCachePath = requestParams.cachePath;
        mDataLoadListener = requestParams.dataLoadListener;

        // 禁止滑动刷新
        mListView.setMode(Mode.DISABLED);

        initData(mCachePath);
    }

    /**
     * 优先从本地缓存取，从服务器读取后再刷新当前页面
     *
     * @param cachePath
     */
    private void initData(final String cachePath) {
        mCacheRequestId = UUID.randomUUID().toString();
        if (mDataLoadListener != null) {
            mDataLoadListener.onDataLoadStrat(mCacheRequestId);
        }

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                return FileUtil.readFileByLines(cachePath);
            }

            @Override
            protected void onPostExecute(String result) {
                if (!TextUtils.isEmpty(result)) {
                    // 存在缓存，先用缓存刷新界面
                    List<B> data = onGetDataSucc(result);
                    if (data != null && !data.isEmpty()) {
                        fillData(onGetDataSucc(result));
                    }
                }

                // 从服务器上获取新数据
                mHttpRequestId = getDataFromSever(mUrl);
                if (mDataLoadListener != null) {
                    mDataLoadListener.onHttpLoading(mHttpRequestId);
                }
            }

        }.execute();
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (mDataLoadListener != null) {
                mDataLoadListener.onDataLoadFinish(mCacheRequestId,
                        mHttpRequestId);
            }

            // 删除多余的EmptyView
            removeEmptyView();

            // 刷新界面数据
            if (msg.arg1 == REQUEST_FAILED) {
                addFailedView();
            } else {
                String result = (String) msg.obj;
                fillData(onGetDataSucc(result));
            }
        }
    };

    private String getDataFromSever(String url) {
        if (TextUtils.isEmpty(mUrl)) {
            return null;
        }
        if (!TextUtils.isEmpty(mHttpRequestId)) {
            HttpHandler httpHandler = HttpUtil.mHandlerMap.get(mHttpRequestId);
            if (httpHandler != null) {
                httpHandler.cancel();
                HttpUtil.mHandlerMap.remove(mHttpRequestId);
            }
        }

        return CmHttpUtil.getInstance(mContext).get(mUrl, mParams,
                new CmRequestListener<JSONObject>(mContext) {
                    @Override
                    public void onReSendReq() {
                        // 登录态过期后重新登录后再次发出请求
                        getDataFromSever(mUrl);
                    }

                    @Override
                    public void onSuccess(JSONObject response) {
                        Message msg = Message.obtain();
                        Object data = response == null ? null : response
                                .opt("Data");

                        String result = data == null ? null : data.toString();
                        FileUtil.cacheStringToFile(result, mCachePath);
                        msg.obj = result;
                        msg.arg1 = REQUEST_SUCC;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onFailure(int exceptionCode, String response) {
                        Message msg = Message.obtain();
                        msg.arg1 = REQUEST_FAILED;
                        mHandler.sendMessage(msg);
                    }

                });
    }

    @Override
    protected void doRequest() {
        getDataFromSever(mUrl);
    }

    @Override
    public void doRefresh() {
        initData(mCachePath);
    }

    private void removeEmptyView() {
        mListView.removeEmptyView(getEmptyView());
        mListView.removeEmptyView(getFailedView());
    }

    private void fillData(List<B> datas) {
        boolean empty = datas == null || datas.isEmpty();
        if (mDataLoadListener != null) {
            mDataLoadListener.onDataLoaded(!empty);
        }

        if (empty) {
            //addEmptyView();
        } else {
            setListData(datas, true);
        }
    }

    @Override
    protected void onSuccess(String data) {
    }

    public abstract List<B> onGetDataSucc(String result);

    public interface OnDataLoadListener {
        void onDataLoadStrat(String handlerId);

        void onHttpLoading(String httpRequestId);

        void onDataLoadFinish(String cacheRequestId, String httpRequestId);

        void onDataLoaded(boolean hasDatas);
    }
}
