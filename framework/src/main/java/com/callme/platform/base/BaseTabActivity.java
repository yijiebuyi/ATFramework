package com.callme.platform.base;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.callme.platform.R;
import com.callme.platform.util.ResourcesUtil;
import com.callme.platform.widget.LazyViewPager;
import com.callme.platform.widget.LazyViewPager.OnPageChangeListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：tab activity基类，只需子类调用addViews方法加入title和fragment即可,此tab不会预加载fragment
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */

public abstract class BaseTabActivity extends BaseActivity implements
        OnCheckedChangeListener {
    public final int TYPE_NORMAL = 0; // 普通顶部滑动tab,右边无红点提示
    public final int TYPE_ROUND_POINT = 1; // 普通顶部滑动tab,右边有红点提示
    public final int TYPE_TOP_NO_SCROLL = 2; // 顶部不允许滑动tab，只允许点击
    public final int TYPE_TOP_ROUND_NO_SCROLL = 3; // 顶部不允许滑动tab,并且有红点提示，只允许点击
    public final int TYPE_BUTTOM_NO_SCROLL = 4; // 底部tab，不允许滑动，和主界面一样
    public final int TYPE_TAB_HAS_DRAWABLE = 5; // tab旁边有图标，具体位置根据传入的值决定

    private LazyViewPager mViewPager;
    private ImageView mIvLine;
    private LinearLayout mTxtContent;

    private String[] mTxtTitles;
    private List<Boolean> mNeedRoundPoints;
    private Map<Integer, Fragment> mFragmentList;
    private int[] mTxtColors;
    private int[] mTxtDrawable;
    private LinearLayout mTopTabContent;
    private LinearLayout mButtomContent;
    private LinearLayout mTopFixedContent;
    private LinearLayout mHeaderContent;
    private RadioGroup mButtonTabs;
    private LinearLayout mButtomToolBar;

    private int mType = TYPE_NORMAL;// 0为普通样式

    private float mEachText; // 每个字相差多大

    private int mEachItemWidth; // 每个tab宽度
    private int mCurrentPosition;

    private TabFragmentPagerAdapter mAdapter;

    private int mStartDelta;// 用于保存红色横线动画的起始位置
    private int mInitPosition;// 初始化的时候红色横线的位置

    public void setInitPosition(int initPosition) {
        mInitPosition = initPosition;
    }

    /**
     * @param position
     */
    public void onPageChanged(int position) {
    }

    @Override
    protected View getContentView() {
        return inflate(R.layout.base_activity_base_tab, mBaseContent);
    }

    @Override
    protected boolean attachMergeLayout() {
        return true;
    }

    public LinearLayout getTopFixedContainer() {
        return mTopFixedContent;
    }

    public LinearLayout getHeaderContainer() {
        return mHeaderContent;
    }

    public LinearLayout getBottomToolBar() {
        return mButtomToolBar;
    }

    @Override
    protected void initSubView() {
        mEachText = ResourcesUtil.getDimension(R.dimen.px3);
        mViewPager = (LazyViewPager) findViewById(R.id.tab_content_view_pager);
        mIvLine = (ImageView) findViewById(R.id.tab_scroll_img);
        mTxtContent = (LinearLayout) findViewById(R.id.txt_content);
        mTopTabContent = (LinearLayout) findViewById(R.id.top_tab_content);
        mButtomContent = (LinearLayout) findViewById(R.id.buttom_content);
        mTopFixedContent = (LinearLayout) findViewById(R.id.top_fixed_content);
        mHeaderContent = (LinearLayout) findViewById(R.id.header_content);
        mButtonTabs = (RadioGroup) findViewById(R.id.buttom_tab_content);
        mButtomToolBar = (LinearLayout) findViewById(R.id.bottom_tool_bar);
        mButtonTabs.setOnCheckedChangeListener(this);
        mFragmentList = new HashMap<Integer, Fragment>();
        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        mTxtContent.getViewTreeObserver().addOnPreDrawListener(
                new OnPreDrawListener() {

                    @Override
                    public boolean onPreDraw() {
                        mTxtContent.getViewTreeObserver()
                                .removeOnPreDrawListener(this);
                        if (mType != TYPE_BUTTOM_NO_SCROLL) {
                            changeText(getPos());
                        }
                        return true;
                    }
                });
    }

    /**
     * 设置样式,在init()里使用
     */
    protected void setType(int type) {
        mType = type;
        // if (type == TYPE_BUTTOM_NO_SCROLL) {
        // android.widget.FrameLayout.LayoutParams params = new
        // android.widget.FrameLayout.LayoutParams(
        // android.view.ViewGroup.LayoutParams.MATCH_PARENT,
        // android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        // params.topMargin = 0;
        // mViewPager.setLayoutParams(params);
        // }
        if (type == TYPE_BUTTOM_NO_SCROLL || type == TYPE_TOP_ROUND_NO_SCROLL) {
            mViewPager.setSrollState(false);
        }
    }

    /**
     * 设置是否需要圆点
     */
    protected void setNeedRoundPoint(List<Boolean> isNeeds) {
        if (mType == TYPE_ROUND_POINT || mType == TYPE_TOP_ROUND_NO_SCROLL) {
            mNeedRoundPoints = isNeeds;
            for (int i = 0; i < mTxtContent.getChildCount(); i++) {
                if (mNeedRoundPoints != null) {
                    if (mNeedRoundPoints.get(i)) {
                        ((TextView) mTxtContent.getChildAt(i).findViewById(
                                R.id.title_item_text))
                                .setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                        0/*R.drawable.unread_point*/, 0);
                    } else {
                        ((TextView) mTxtContent.getChildAt(i).findViewById(
                                R.id.title_item_text))
                                .setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                        0, 0);
                    }
                }
            }
        }
    }

    /**
     * 设置tab右边的drawable,此方法调用必须在addView方法调用之后再调用 否则无法显示图标 调用顺序应该是在init中依次调用
     * addView()-->setType(int)-->setTabDrawable
     */
    protected void setTabDrawable(List<Integer> drawableIds, int[] positions) {
        if (drawableIds == null || positions == null
                || drawableIds.size() != positions.length) {
            throw new IllegalArgumentException("please check the args");
        }
        if (mType != TYPE_TAB_HAS_DRAWABLE) {
            throw new IllegalArgumentException(
                    "plese set type to TYPE_TAB_HAS_DRAWABLE int init()");
        }
        if (mTxtContent.getChildCount() == 0) {
            return;
        }
        for (int i = 0; i < positions.length; i++) {
            TextView text = (TextView) mTxtContent.getChildAt(positions[i])
                    .findViewById(R.id.title_item_text);
            if (text == null) {
                try {
                    throw new Exception("tab TextView is null");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            } else {
                text.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                        drawableIds.get(i), 0);
            }
        }
        setImageViewDefault();
    }

    /**
     * 初始化无初始参数界面调用 用于可滑动tab
     *
     * @param tabTxt
     * @param list
     */
    public void addViews(String[] tabTxt, List<Class<? extends Fragment>> list)
            throws Exception {
        if (tabTxt == null || tabTxt.length <= 0) {
            throw new Exception(" tabs text is null");
        } else if (list == null || list.size() == 0) {
            throw new Exception(" fragment is null");
        } else if (tabTxt.length != list.size()) {
            throw new Exception(
                    "tab text length is not equals fragment list size");
        } else {
            mTxtTitles = tabTxt;
            createTabItem();
            setImageViewDefault();
            mAdapter = new TabFragmentPagerAdapter(getSupportFragmentManager(),
                    list);
            mViewPager.setAdapter(mAdapter);
        }
    }

    /**
     * 初始化需传入参数的界面调用 用于可滑动tab
     *
     * @param <T>
     * @param tabTxt
     * @param list
     * @param data   Integer:需要设置参数的Fragment位置 Bundle 参数值
     */
    public <T> void addViews(String[] tabTxt,
                             List<Class<? extends Fragment>> list,
                             Map<Integer, Bundle> data) throws Exception {
        if (tabTxt == null || tabTxt.length <= 0) {
            throw new Exception(" tabs text is null");
        } else if (list == null || list.size() == 0) {
            throw new Exception(" fragment is null");
        } else if (tabTxt.length != list.size()) {
            throw new Exception(
                    "tab text length is not equals fragment list size");
        } else {
            mTxtTitles = tabTxt;
            createTabItem();
            setImageViewDefault();
            mAdapter = new TabFragmentPagerAdapter(getSupportFragmentManager(),
                    list, data);
            mViewPager.setAdapter(mAdapter);
        }
    }

    /**
     * 初始化无初始参数界面调用 用于不可滑动tab
     *
     * @param tabTxt
     * @param drawable  用于底部tab文字上面的图标的selector, 若没有图标，则可以传入null
     * @param txtColors tab文字对应的颜色，可以是selector，如果传入null，则使用默认的颜色
     * @param list
     */
    public void addViews(String[] tabTxt, int[] txtColors, int[] drawable,
                         List<Class<? extends Fragment>> list) throws Exception {
        if (tabTxt == null || tabTxt.length <= 0) {
            throw new NullPointerException(" tabs text is null");
        } else if (list == null || list.size() == 0) {
            throw new NullPointerException(" fragment is null");
        } else if (tabTxt.length != list.size()) {
            throw new Exception(
                    "tab text length is not equals fragment list size or tab text length is not equals drawable");
        } else {
            mTxtTitles = tabTxt;
            mTxtColors = txtColors;
            mTxtDrawable = drawable;
            createTabItem();
            mAdapter = new TabFragmentPagerAdapter(getSupportFragmentManager(),
                    list);
            mViewPager.setAdapter(mAdapter);
        }
    }

    /**
     * 初始化有初始参数界面调用 用于不可滑动tab
     *
     * @param tabTxt
     * @param drawable  用于底部tab文字上面的图标的selector, 若没有图标，则可以传入null
     * @param txtColors tab文字对应的颜色，可以是selector，如果传入null，则使用默认的颜色
     * @param list
     */
    public <T> void addViews(String[] tabTxt, int[] txtColors, int[] drawable,
                             List<Class<? extends Fragment>> list,
                             Map<Integer, Bundle> data) throws Exception {
        if (tabTxt == null || tabTxt.length <= 0) {
            throw new NullPointerException(" tabs text is null");
        } else if (list == null || list.size() == 0) {
            throw new NullPointerException(" fragment is null");
        } else if (tabTxt.length != list.size()) {
            throw new Exception(
                    "tab text length is not equals fragment list size or tab text length is not equals drawable");
        } else {
            mTxtTitles = tabTxt;
            mTxtColors = txtColors;
            mTxtDrawable = drawable;
            createTabItem();
            mAdapter = new TabFragmentPagerAdapter(getSupportFragmentManager(),
                    list, data);
            mViewPager.setAdapter(mAdapter);
        }
    }

    public void updateText(int pos, String newTitle) {
        if (pos >= 0 && pos < mTxtTitles.length) {
            mTxtTitles[pos] = newTitle;
            createTabItem();
            changeText(pos);
            setImagePosition(pos);
        }
    }

    public void changeText(int position) {
        for (int i = 0; i < mTxtTitles.length; i++) {
            TextView textView = (TextView) mTxtContent.getChildAt(i)
                    .findViewById(R.id.title_item_text);
            if (position == i) {
                textView.setTextColor(ResourcesUtil
                        .getColor(R.color.blue_4e92f7));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        ResourcesUtil.getDimension(R.dimen.font_34px));
            } else {
                textView.setTextColor(ResourcesUtil
                        .getColor(R.color.gray_999999));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        ResourcesUtil.getDimension(R.dimen.font_28px));
            }
        }
    }

    // 仅仅初始化的时候调用
    private void setImageViewDefault() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        LayoutParams params = (LayoutParams) mIvLine.getLayoutParams();
        mEachItemWidth = dm.widthPixels / mTxtTitles.length;

        if (mTxtTitles.length == 2) {
            params.width = mEachItemWidth
                    - ResourcesUtil.getDimensionPixelOffset(R.dimen.px60);
        } else {
            params.width = mEachItemWidth;
        }

        mIvLine.setLayoutParams(params);

        int itemSpacing = 0;
        itemSpacing = (mEachItemWidth - params.width) / 2;

        int endDelta = itemSpacing + mInitPosition * mEachItemWidth;
        Animation animation = new TranslateAnimation(endDelta, endDelta, 0, 0);
        animation.setFillAfter(true);
        mIvLine.startAnimation(animation);
        mStartDelta = endDelta;
    }

    // 切换viewpager的时候调用
    private void setImagePosition(int position) {
        LayoutParams params = (LayoutParams) mIvLine.getLayoutParams();
        int endDelta = mEachItemWidth * position
                + (mEachItemWidth - params.width) / 2;
        Animation animation = new TranslateAnimation(mStartDelta, endDelta, 0,
                0);
        animation.setFillAfter(true);
        animation.setDuration(300);
        mIvLine.startAnimation(animation);
        mCurrentPosition = position;
        mStartDelta = endDelta;
    }

    private void createTabItem() {
        switch (mType) {
            case TYPE_NORMAL:
                createNormalTab();
                break;
            case TYPE_ROUND_POINT:
                createRoundPointType();
                break;
            case TYPE_TOP_NO_SCROLL:
                createNormalTab();
                setImageViewDefault();
                break;
            case TYPE_TOP_ROUND_NO_SCROLL:
                createRoundPointType();
                setImageViewDefault();
                break;
            case TYPE_BUTTOM_NO_SCROLL:
                mTopTabContent.setVisibility(View.GONE);
                mButtomContent.setVisibility(View.VISIBLE);
                createButtomTabItems();
                break;
        }
    }

    private void createButtomTabItems() {
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < mTxtTitles.length; i++) {
            RadioButton child = (RadioButton) inflater.inflate(
                    R.layout.base_layout_buttom_radiobutton, null);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
            child.setLayoutParams(params);
            child.setId(i);
            child.setText(mTxtTitles[i]);
            if (mTxtDrawable != null && mTxtDrawable.length > i) {
                child.setCompoundDrawablesWithIntrinsicBounds(0,
                        mTxtDrawable[i], 0, 0);
            }
            if (mTxtColors != null && mTxtColors.length > i) {
                child.setTextColor(ResourcesUtil.getColor(mTxtColors[i]));
            }
            if (i == 0) {
                child.performClick();
            }
            mButtonTabs.addView(child);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (canChange(checkedId)) {
            mViewPager.setCurrentItem(checkedId);
            mCurrentPosition = checkedId;
        } else {
            setCurrentBottomTab(mCurrentPosition);
        }
    }

    public boolean canChange(int position) {
        return true;
    }

    private void setCurrentBottomTab(int position) {
        int count = mButtonTabs.getChildCount();
        for (int i = 0; i < count; i++) {
            RadioButton radio = (RadioButton) mButtonTabs.getChildAt(i);
            if (i == position) {
                radio.performClick();
                break;
            }
        }
    }

    // 创建普通样式的tab
    private void createNormalTab() {
        mTxtContent.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < mTxtTitles.length; i++) {
            View view = inflater.inflate(R.layout.base_layout_tab_title_item, null);
            TextView txtView = (TextView) view
                    .findViewById(R.id.title_item_text);
            txtView.setText(mTxtTitles[i]);
            LayoutParams params = new LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1);
            view.setLayoutParams(params);
            final int position = i;
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    mViewPager.setCurrentItem(position);
                }
            });
            mTxtContent.addView(view);
        }
    }

    // 创建有圆点样式的tab
    private void createRoundPointType() {
        mTxtContent.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < mTxtTitles.length; i++) {
            View view = inflater.inflate(R.layout.base_layout_tab_title_item, null);
            TextView txtView = (TextView) view
                    .findViewById(R.id.title_item_text);
            txtView.setText(mTxtTitles[i]);
            if (mNeedRoundPoints != null) {
                if (mNeedRoundPoints.get(i)) {
                    txtView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                            0, 0);
                }
            }
            LayoutParams params = new LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1);
            view.setLayoutParams(params);
            final int position = i;
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    mViewPager.setCurrentItem(position);
                }
            });
            mTxtContent.addView(view);
        }
    }

    private class TabFragmentPagerAdapter<T> extends FragmentPagerAdapter {
        List<Class<? extends Fragment>> listClass;
        Map<Integer, Bundle> mData;

        public TabFragmentPagerAdapter(FragmentManager fm,
                                       List<Class<? extends Fragment>> list) {
            super(fm);
            listClass = list;
        }

        public TabFragmentPagerAdapter(FragmentManager fm,
                                       List<Class<? extends Fragment>> list, Fragment fragment) {
            super(fragment.getChildFragmentManager());
            listClass = list;
        }

        public TabFragmentPagerAdapter(FragmentManager fm,
                                       List<Class<? extends Fragment>> list, Map<Integer, Bundle> data) {
            super(fm);
            listClass = list;
            mData = data;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment child = mFragmentList.get(position);
            if (child == null) {
                child = createNewFragment(position, listClass.get(position),
                        mData == null ? null : mData.get(position));
            }
            return child;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position,
                                   Object object) {
            super.setPrimaryItem(container, position, object);
            mFragmentList.put(position, (Fragment) object);
        }

        @Override
        public int getCount() {
            return listClass.size();
        }
    }

    private <T> Fragment createNewFragment(int position, Class<? extends Fragment> cls, Bundle bundle) {
        try {
            Fragment fragment = cls.newInstance();
            fragment.setArguments(bundle);
            mFragmentList.put(position, fragment);
            return fragment;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private class MyOnPageChangeListener implements OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (mType != TYPE_BUTTOM_NO_SCROLL) {
                changeText(position);
                setImagePosition(position);
            } else {
                setCurrentBottomTab(position);
            }
            onPageChanged(position);
        }

    }

    public final Fragment getFragment(int position) {
        return mAdapter != null ? mAdapter.getItem(position) : null;
    }

    // public final Fragment getFragment(int position) {
    // return mFragmentList.get(position);
    // }

    public final boolean isFragmentListNull() {
        return mFragmentList == null || mFragmentList.size() == 0;
    }

    public final int getFragmentSize() {
        if (mFragmentList == null) {
            return 0;
        } else {
            return mFragmentList.size();
        }
    }

    public final void setViewPagerLimit(int size) {
        mViewPager.setOffscreenPageLimit(size);
    }

    public final void setTabBgAlpha(int alpha) {
        switch (mType) {
            case TYPE_NORMAL:
            case TYPE_ROUND_POINT:
            case TYPE_TOP_NO_SCROLL:
                mTxtContent.getBackground().setAlpha(alpha);
                break;
            case TYPE_BUTTOM_NO_SCROLL:
                mButtonTabs.getBackground().setAlpha(alpha);
                break;
            default:
                break;
        }
    }

    public final int getPos() {
        return mCurrentPosition;
    }

    public final void setCurrentTab(int position) {
        if (position >= 0 && position < mViewPager.getAdapter().getCount()) {
            mViewPager.setCurrentItem(position);
        }
    }

    /**
     * 是否需要隐藏顶部tab栏
     *
     * @param visibility
     */
    public final void setTopTabVisibility(int visibility) {
        mTopTabContent.setVisibility(visibility);
    }

    /**
     * 是否需要隐藏底部tab栏
     *
     * @param visibility
     */
    public final void setBottomTabVisibility(int visibility) {
        mButtomContent.setVisibility(visibility);
    }
}
