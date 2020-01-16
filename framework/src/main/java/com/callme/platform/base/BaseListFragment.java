package com.callme.platform.base;

import android.view.View;
import android.widget.ImageView;

import com.callme.platform.R;
import com.callme.platform.widget.pulltorefresh.PullToRefreshSwipeListView;

public abstract class BaseListFragment extends BaseFragment {
	protected PullToRefreshSwipeListView mListView;
	protected ImageView mPageTop;
	protected View mSpaceHolder;

	@Override
	public View getContainerView() {
		View view = View.inflate(getActivity(), R.layout.base_layout_fragment_list,
				null);
		mListView = view
				.findViewById(R.id.pull_to_refresh_list);

		mPageTop = view.findViewById(R.id.page_top);
		mSpaceHolder = view.findViewById(R.id.space_holder);
		return view;
	}
}
