package com.callme.platform.base;

import java.util.List;

public class PageBean<D> extends BaseBean {

	private static final long serialVersionUID = 1L;
	
	public int page;
	public int pageSize;
	public List<D> rows;
	public Object extraData;
	public int error;
	public int total;
	public int notReadCount;
}
