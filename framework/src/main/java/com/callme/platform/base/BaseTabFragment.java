package com.callme.platform.base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import android.widget.TextView;

import com.callme.platform.R;
import com.callme.platform.util.ResourcesUtil;
import com.callme.platform.widget.LazyViewPager;
import com.callme.platform.widget.LazyViewPager.OnPageChangeListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：tab fragment基类，只需子类调用addViews方法加入title和fragment即可,此tab不会预加载fragment
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */

public abstract class BaseTabFragment extends BaseFragment {
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_ROUND_POINT = 1;
    public final int TYPE_TAB_HAS_DRAWABLE = 2; // tab旁边有图标，具体位置根据传入的值决定

    protected LazyViewPager mViewPager;
    private ImageView mIvLine;
    private LinearLayout mTxtContent;

    private String[] mTxtTitles;
    private ArrayList<Boolean> mNeedRoundPoints;
    private Map<Integer, Fragment> mFragmentList;

    private int mType = TYPE_NORMAL;// 0为普通样式

    private float mEachText; // 每个字相差多大

    private int mEachItemWidth; // 每个tab宽度

    private TabFragmentPagerAdapter mAdapter;

    private int mCurrentPosition = 0;

    private boolean mIsNeedCache = false;

    public final int getCurrentPosition() {
        return mCurrentPosition;
    }

    /**
     * viewpager滑动回调
     *
     * @param position
     */
    public void onPageScroll(int position) {
    }

    /**
     * viewpager的高度按内容自适应
     */
    public void setNeedWrapContent(boolean need) {
        mViewPager.setNeedWrapContent(need);
    }

    public void setNeedCache(boolean mIsNeedCache) {
        this.mIsNeedCache = mIsNeedCache;
    }

    @Override
    public View getContainerView() {
        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.base_fragment_base_tab, null);
        mViewPager = view
                .findViewById(R.id.tab_content_view_pager);
        mIvLine = view.findViewById(R.id.tab_scroll_img);
        mTxtContent = view.findViewById(R.id.txt_content);
        return view;
    }

    @Override
    public void initData() {
        initDefaultView();
        init();
    }

    private void initDefaultView() {
        mEachText = ResourcesUtil.getDimension(R.dimen.px3);
        mFragmentList = new HashMap<Integer, Fragment>();
        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        mTxtContent.getViewTreeObserver().addOnPreDrawListener(
                new OnPreDrawListener() {

                    @Override
                    public boolean onPreDraw() {
                        mTxtContent.getViewTreeObserver()
                                .removeOnPreDrawListener(this);
                        changeText(getCurrentPosition());
                        return true;
                    }
                });
    }

    public final void setCurrentTab(int position) {
        getFragment(position);
        if (mViewPager != null && position >= 0
                && position < mViewPager.getAdapter().getCount()) {
            mViewPager.setCurrentItem(position);
            mCurrentPosition = position;
        }
    }

    public abstract void init();

    /**
     * 设置样式,在init()里使用
     */
    protected void setType(int type) {
        mType = type;
    }

    /**
     * 设置是否需要圆点
     */
    protected void setNeedRoundPoint(ArrayList<Boolean> isNeeds) {
        if (mType == TYPE_ROUND_POINT) {
            mNeedRoundPoints = isNeeds;
            for (int i = 0; i < mTxtContent.getChildCount(); i++) {
                if (mNeedRoundPoints != null) {
                    if (mNeedRoundPoints.get(i)) {
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
            TextView text = mTxtContent.getChildAt(positions[i])
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
     * 初始化无初始参数界面调用
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
            mAdapter = new TabFragmentPagerAdapter(getFragmentManager(), list);
            mViewPager.setAdapter(mAdapter);
        }
    }

    /**
     * 初始化无初始参数界面调用
     *
     * @param tabTxt
     * @param list
     * @param isNest 如果嵌入在其他的fragment里面就要传true
     */
    public void addViews(String[] tabTxt, List<Class<? extends Fragment>> list,
                         boolean isNest) throws Exception {
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
            mAdapter = new TabFragmentPagerAdapter(
                    isNest ? getChildFragmentManager() : getFragmentManager(),
                    list);
            mViewPager.setAdapter(mAdapter);
        }
    }

    /**
     * 初始化需传入参数的界面调用
     *
     * @param <T>
     * @param tabTxt
     * @param list
     * @param argList Integer:需要设置参数的Fragment位置 Map<String, T> String 方法名 T 参数值
     */
    public <T> void addViews(String[] tabTxt,
                             List<Class<? extends Fragment>> list,
                             Map<Integer, Map<String, T>> argList) throws Exception {
        if (tabTxt == null || tabTxt.length <= 0) {
            throw new Exception(" tabs text is null");
        } else if (list == null || list.size() == 0) {
            throw new Exception(" fragment is null");
        } else if (tabTxt.length != list.size()) {
            throw new Exception(
                    "tab text length is not equals fragment list size");
        } else if (argList == null || argList.size() == 0) {
            throw new IllegalArgumentException(
                    "method list is null or size is 0; arg list is null or size is 0");
        } else {
            mTxtTitles = tabTxt;
            createTabItem();
            setImageViewDefault();
            mAdapter = new TabFragmentPagerAdapter(getFragmentManager(), list,
                    argList);
            mViewPager.setAdapter(mAdapter);
        }
    }

    /**
     * 初始化无初始参数界面调用
     *
     * @param tabTxt
     * @param list
     * @param isNest 如果嵌入在其他的fragment里面就要传true
     */
    public <T> void addViews(String[] tabTxt,
                             List<Class<? extends Fragment>> list,
                             Map<Integer, Map<String, T>> argList, boolean isNest)
            throws Exception {
        if (tabTxt == null || tabTxt.length <= 0) {
            throw new Exception(" tabs text is null");
        } else if (list == null || list.size() == 0) {
            throw new Exception(" fragment is null");
        } else if (tabTxt.length != list.size()) {
            throw new Exception(
                    "tab text length is not equals fragment list size");
        } else if (argList == null || argList.size() == 0) {
            throw new IllegalArgumentException(
                    "method list is null or size is 0; arg list is null or size is 0");
        } else {
            mTxtTitles = tabTxt;
            createTabItem();
            setImageViewDefault();
            mAdapter = new TabFragmentPagerAdapter(
                    isNest ? getChildFragmentManager() : getFragmentManager(),
                    list, argList);
            mViewPager.setAdapter(mAdapter);
        }
    }

    public void updateText(int pos, String newTitle) {
        if (pos >= 0 && pos < mTxtTitles.length) {
            ((TextView) mTxtContent.getChildAt(pos).findViewById(
                    R.id.title_item_text)).setText(newTitle);
            setImagePosition(pos);
        }
    }

    public void changeText(int position) {
        for (int i = 0; i < mTxtTitles.length; i++) {
            TextView textView = mTxtContent.getChildAt(i)
                    .findViewById(R.id.title_item_text);
            if (position == i) {
                textView.setTextColor(ResourcesUtil
                        .getColor(R.color.common_btn_red_bg));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        ResourcesUtil.getDimension(R.dimen.font_34px));
            } else {
                textView.setTextColor(ResourcesUtil
                        .getColor(R.color.common_black));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        ResourcesUtil.getDimension(R.dimen.font_28px));
            }
        }
    }

    // 仅仅初始化的时候调用
    private void setImageViewDefault() {
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        LayoutParams params = (LayoutParams) mIvLine.getLayoutParams();
        mEachItemWidth = dm.widthPixels / mTxtTitles.length;

        if (mTxtTitles.length == 2) {
            params.width = mEachItemWidth
                    - ResourcesUtil.getDimensionPixelOffset(R.dimen.px60);
        } else {
            params.width = mEachItemWidth;
        }

        mIvLine.setLayoutParams(params);

        lastPosition = (mEachItemWidth - params.width) / 2;
        mCurrentPosition = 0;
        Animation animation = new TranslateAnimation(lastPosition,
                lastPosition, 0, 0);
        animation.setFillAfter(true);
        mIvLine.startAnimation(animation);
    }

    // 切换viewpager的时候调用
    private void setImagePosition(int position) {
        LayoutParams params = (LayoutParams) mIvLine.getLayoutParams();
        if (mTxtTitles.length == 2) {
            params.width = mEachItemWidth
                    - ResourcesUtil.getDimensionPixelOffset(R.dimen.px60);
        } else {
            params.width = mEachItemWidth;
        }
        int end = mEachItemWidth * position + (mEachItemWidth - params.width)
                / 2;
        mIvLine.setLayoutParams(params);

        Animation animation = new TranslateAnimation(lastPosition, end, 0, 0);
        animation.setFillAfter(true);
        animation.setDuration(300);
        mIvLine.startAnimation(animation);
        lastPosition = end;
        mCurrentPosition = position;
    }

    private int lastPosition = 0;

    private void createTabItem() {
        switch (mType) {
            case TYPE_NORMAL:
                createNormalType();
                break;
            case TYPE_ROUND_POINT:
                createRoundPointType();
                break;
        }
    }

    // 创建普通样式的tab
    private void createNormalType() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        for (int i = 0; i < mTxtTitles.length; i++) {
            View view = inflater.inflate(R.layout.base_layout_tab_title_item, null);
            TextView txtView = view
                    .findViewById(R.id.title_item_text);
            txtView.setText(mTxtTitles[i]);
            LayoutParams params = new LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1);
            view.setLayoutParams(params);
            final int position = i;
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (mIsNeedCache) {
                        if (mCurrentPosition == 0 && position == 2) {
                            mViewPager.setCurrentItem(1);
                            try {
                                Thread.sleep(80);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                    mViewPager.setCurrentItem(position);
                }
            });
            mTxtContent.addView(view);
        }
    }

    // 创建有圆点样式的tab
    private void createRoundPointType() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        for (int i = 0; i < mTxtTitles.length; i++) {
            View view = inflater.inflate(R.layout.base_layout_tab_title_item, null);
            TextView txtView = view
                    .findViewById(R.id.title_item_text);
            txtView.setText(mTxtTitles[i]);
            if (mNeedRoundPoints != null) {
                if (mNeedRoundPoints.get(i)) {
                    txtView.setCompoundDrawablePadding(getResources()
                            .getDimensionPixelSize(R.dimen.px10));
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
        Map<Integer, Map<String, T>> argList;

        public TabFragmentPagerAdapter(FragmentManager fm,
                                       List<Class<? extends Fragment>> list) {
            super(fm);
            listClass = list;
        }

        public TabFragmentPagerAdapter(FragmentManager fm,
                                       List<Class<? extends Fragment>> list,
                                       Map<Integer, Map<String, T>> args) {
            super(fm);
            listClass = list;
            argList = args;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment child = mFragmentList.get(position);
            if (child == null) {
                try {
                    Class<? extends Fragment> tempClass = listClass
                            .get(position);
                    try {
                        child = tempClass.newInstance();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    if (argList != null) {
                        Map<String, T> map = argList.get(position);
                        if (map != null && map.size() != 0) {
                            Iterator<Entry<String, T>> it = map.entrySet()
                                    .iterator();
                            while (it.hasNext()) {
                                Entry<String, T> temp = it.next();
                                String key = temp.getKey();
                                T value = temp.getValue();
                                try {
                                    Method method = tempClass.getMethod(key,
                                            value.getClass());
                                    try {
                                        method.invoke(child, value);
                                    } catch (IllegalArgumentException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                } catch (SecurityException e) {
                                    e.printStackTrace();
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    mFragmentList.put(position, child);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
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
            changeText(position);
            setImagePosition(position);
            onPageScroll(position);
        }

    }

    public final Fragment getFragment(int position) {
        return mAdapter != null ? mAdapter.getItem(position) : null;
    }

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
}
