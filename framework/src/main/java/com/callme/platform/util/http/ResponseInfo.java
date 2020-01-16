package com.callme.platform.util.http;


import com.callme.platform.util.http.core.HttpEntity;
import com.callme.platform.util.http.core.HttpResponse;

import java.util.Map;

public final class ResponseInfo<T> {

    private final HttpResponse response;
    public T result;
    public final boolean resultFormCache;

    // status line
    public final int statusCode;
    public final String reasonPhrase;

    // entity
    public final long contentLength;
    public final String contentType;
    public final String contentEncoding;

    public  Map<String, String> getAllHeaders() {
        if (response == null) return null;
        return response.getAllHeaders();
    }

    public  String getHeader(String name) {
        if (response == null) return null;
        return response.getHeader(name);
    }

    public ResponseInfo(final HttpResponse response, T result, boolean resultFormCache) {
        this.response = response;
        this.result = result;
        this.resultFormCache = resultFormCache;

        if (response != null) {
            // status line
                statusCode = response.getResponseCode();
                reasonPhrase = response.getResponseMessage();

            // entity
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                contentLength = entity.getContentLength();
                contentType = entity.getContentType();
                contentEncoding = entity.getContentEncoding();
            } else {
                contentLength = 0;
                contentType = null;
                contentEncoding = null;
            }
        } else {
            // status line
            statusCode = 0;
            reasonPhrase = null;

            // entity
            contentLength = 0;
            contentType = null;
            contentEncoding = null;
        }
    }
}
