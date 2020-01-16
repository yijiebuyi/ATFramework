package com.callme.platform.base;

import android.view.View;

import com.callme.platform.R;
import com.callme.platform.widget.pulltorefresh.PullToRefreshSwipeListView;

public abstract class BaseListActivity extends BaseActivity {
    protected PullToRefreshSwipeListView mListView;

    @Override
    protected View getContentView() {
        return inflate(R.layout.base_activity_list);
    }

    @Override
    protected void initSubView() {
        mListView = (PullToRefreshSwipeListView) findViewById(R.id.pull_to_refresh_list);
    }
}
