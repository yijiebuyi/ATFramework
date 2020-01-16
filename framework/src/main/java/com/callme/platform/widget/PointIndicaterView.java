package com.callme.platform.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import androidx.viewpager.widget.ViewPager;

import com.callme.platform.R;
import com.callme.platform.util.ResourcesUtil;


public class PointIndicaterView extends LinearLayout implements
		ViewPager.OnPageChangeListener,
		OnItemSelectedListener {

	private int mPointNum = 0;
	private int mCurrentPoint = 0;
	private boolean mIsLoop = false;

	private float mInterval = 1.25f;// 点与点之间的间隔
	private float mSize = 12f;// 高与宽的大小

	private ViewPager mPager = null;// 外部传进来的viewpager
	private Gallery mGallery = null;// 外部传进来的Gallery
	private OnPageChangeListener mListen = null;
	private OnItemSelectedListener mGalleryListen = null;

	private final int PAGER_NUM = 300;// 为了支持循环显示，欺骗系统

	public interface OnItemSelectedListener {
		void onItemSelected(AdapterView<?> parent, View view,
                            int position, long id);

		void onNothingSelected(AdapterView<?> parent);
	}

	public interface OnPageChangeListener {
		void onPageScrolled(int i, float f, int j);

		void onPageSelected(int i);

		void onPageScrollStateChanged(int i);
	}

	public PointIndicaterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		setOrientation(HORIZONTAL);
	}

	private void initViewWhenSet() {
		if (mPointNum == 0 || mPointNum == 1) {
			setVisibility(View.INVISIBLE);
			return;
		}
		removeAllViews();
		for (int i = 0; i < mPointNum; i++) {
			addView(initPointView(), ResourcesUtil.dip2px(mSize),
					ResourcesUtil.dip2px(mSize));
		}
		setCurrentPoint(mCurrentPoint);
		setVisibility(View.VISIBLE);
	}

	private View initPointView() {
		ImageView img = new ImageView(getContext());
		img.setScaleType(ScaleType.CENTER);
		img.setImageDrawable(getResources()
				.getDrawable(R.drawable.point_normal));
		img.setPadding(ResourcesUtil.dip2px(mInterval),
				ResourcesUtil.dip2px(mInterval),
				ResourcesUtil.dip2px(mInterval),
				ResourcesUtil.dip2px(mInterval));
		return img;
	}

	private void setPointPosition(int position) {
		if (getVisibility() != View.VISIBLE || position + 1 > getChildCount()) {
			return;
		}
		for (int i = 0; i < getChildCount(); i++) {
			if (position == i) {
				((ImageView) getChildAt(i)).setImageDrawable(getResources()
						.getDrawable(R.drawable.point_selected));
			} else {
				((ImageView) getChildAt(i)).setImageDrawable(getResources()
						.getDrawable(R.drawable.point_normal));
			}
		}
	}

	public void setViewPager(ViewPager pager, int totalNum, int currentNum,
			boolean needLoop, OnPageChangeListener pagerListen) {
		if (pager == null) {
			return;
		}
		if (totalNum == 0 || totalNum == 1) {
			mIsLoop = false;
		} else {
			mIsLoop = needLoop;
		}
		mCurrentPoint = currentNum;
		mPager = pager;
		mPager.setOnPageChangeListener(this);
		mListen = pagerListen;
		setPointNummber(totalNum);
		if (mIsLoop) {
			mPager.setCurrentItem(mPointNum * PAGER_NUM + mCurrentPoint);
		} else {
			mPager.setCurrentItem(mCurrentPoint);
		}
	}

	public void setGallery(Gallery pager, int totalNum, int currentNum,
			boolean needLoop, OnItemSelectedListener pagerListen) {
		if (pager == null) {
			return;
		}
		if (totalNum == 0 || totalNum == 1) {
			mIsLoop = false;
		} else {
			mIsLoop = needLoop;
		}
		mCurrentPoint = currentNum;
		mGallery = pager;
		mGallery.setOnItemSelectedListener(this);
		mGalleryListen = pagerListen;
		setPointNummber(totalNum);
		if (mIsLoop) {
			mGallery.setSelection(mPointNum * PAGER_NUM + mCurrentPoint, false);
		} else {
			mGallery.setSelection(mCurrentPoint, false);
		}
	}

	private void setPointNummber(int num) {
		mPointNum = num;
		initViewWhenSet();
	}

	public int getPointNummber() {
		return mPointNum;
	}

	private void setCurrentPoint(int indicate) {
		mCurrentPoint = indicate;
		setPointPosition(mCurrentPoint);
	}

	public int getCurrentPoint() {
		return mCurrentPoint;
	}

	@Override
	public void onPageScrolled(int i, float f, int j) {
		if (mListen != null) {
			if (!mIsLoop) {
				mListen.onPageScrolled(i, f, j);
			} else {
				mListen.onPageScrolled(i % mPointNum, f, j);
			}
		}
	}

	@Override
	public void onPageSelected(int i) {
		if (!mIsLoop) {
			setCurrentPoint(i);
		} else {
			setCurrentPoint(i % mPointNum);
		}
		if (mListen != null) {
			mListen.onPageSelected(i);
		}
	}

	@Override
	public void onPageScrollStateChanged(int i) {
		if (mListen != null) {
			if (!mIsLoop) {
				mListen.onPageScrollStateChanged(i);
			} else {
				mListen.onPageScrollStateChanged(i % mPointNum);
			}
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if (!mIsLoop) {
			setCurrentPoint(position);
		} else {
			setCurrentPoint(position % mPointNum);
		}
		if (mGalleryListen != null) {
			if (!mIsLoop) {
				mGalleryListen.onItemSelected(parent, view, position, id);
			} else {
				mGalleryListen.onItemSelected(parent, view, position
						% mPointNum, id);
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		if (mGalleryListen != null) {
			mGalleryListen.onNothingSelected(parent);
		}
	}

}
