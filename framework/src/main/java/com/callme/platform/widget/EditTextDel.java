package com.callme.platform.widget;


import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import com.callme.platform.R;
import com.callme.platform.common.dialog.PopTip;
import com.callme.platform.util.ResourcesUtil;

/**
 * @版权 Copyright©2017 重庆呼我出行网络科技有限公司
 * @功能描述 右边带有清除数据功能按钮的EditText, 该EditText不能设置drawableRight
 * @作者 mikeyou
 * @创建时间
 * @修改人
 * @修改描述
 * @修改日期
 */
public class EditTextDel extends EditText {
    private Drawable mDelDrawable;
    private Drawable mErrorDrawable;
    private PopTip mErrorPup;
    private boolean isNeedDelete = true;

//	public final static int JUDGE_TYPE_DEFAULT = 0;
//	public final static int JUDGE_TYPE_PADDING = 1;
//
//	private int judgeType = JUDGE_TYPE_DEFAULT;

    public EditTextDel(Context context) {
        super(context);
        init();
    }

    public EditTextDel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditTextDel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mDelDrawable = getResources().getDrawable(
                R.drawable.image_btn_gray_close_icon);
        try {
            setCompoundDrawablePadding((int) ResourcesUtil
                    .getDimension(R.dimen.px20));
        } catch (Exception e) {

        }
        addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mErrorPup != null) {
                    mErrorPup.dimiss();
                    mErrorPup = null;
                    setErrorMsg(null);
                }
                setDrawable();
            }
        });
        setDrawable();
    }

    private void setDrawable() {
        Drawable[] drawable = getCompoundDrawables();
        if (mErrorPup != null) {
            setCompoundDrawablesWithIntrinsicBounds(drawable[0], drawable[1],
                    mErrorDrawable, drawable[3]);
        } else if (isNeedDelete && length() > 0 && isFocused() && isEnabled()) {
            setCompoundDrawablesWithIntrinsicBounds(drawable[0], drawable[1],
                    mDelDrawable, drawable[3]);
        } else {
            setCompoundDrawablesWithIntrinsicBounds(drawable[0], drawable[1],
                    null, drawable[3]);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setDrawable();
    }

    public void setEdtText(String text) {
        setText(text);
    }

    /**
     * 是否需要右侧的删除按钮
     *
     * @param isNeedDel
     */
    public void isNeedDelete(boolean isNeedDel) {
        isNeedDelete = isNeedDel;
    }

    public final void setErrorMsg(String error) {
        if (!TextUtils.isEmpty(error)) {
            mErrorPup = new PopTip(getContext(), this, error);
            mErrorDrawable = getResources().getDrawable(R.drawable.icon_error);
            if (isFocused()) {
                mErrorPup.show();
            }
            setTextColor(0xfff00500);
            setDrawable();
        } else {
            setTextColor(0xff222222);
            if (mErrorPup != null) {
                mErrorPup.dimiss();
                mErrorPup = null;
            }
        }
    }

    /**
     * 判断错误是否修改
     *
     * @return
     */
    public final boolean isErrorChange() {
        return mErrorPup == null;
    }

//	public int getJudgeType() {
//		return judgeType;
//	}

    /**
     * 设置判断是否点击到了删除按钮的方式
     *
     * @param judgeType 判断方式
     */
//	public void setJudgeType(int judgeType) {
//		this.judgeType = judgeType;
//	}
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && isFocused()
                && isEnabled()) {
            if (mDelDrawable != null && mErrorPup == null && isNeedDelete) {
//				switch (judgeType) {
//				case JUDGE_TYPE_DEFAULT:
//					int x = (int) event.getRawX();
//					int y = (int) event.getRawY();
//					Rect rect = new Rect();
//					getGlobalVisibleRect(rect);
                // int height = rect.bottom - rect.top;
                // rect.top = rect.bottom - height / 2 -
                // mDelDrawable.getIntrinsicHeight() / 2 - 25;
                // rect.bottom = rect.top +
                // mDelDrawable.getIntrinsicHeight() + 25;
//					rect.left = rect.right - mDelDrawable.getIntrinsicWidth()
//							- 35;
//					rect.right = rect.right + 25;
//					if (rect.contains(x, y)) {
//						setText("");
//					}
//					break;
//				case JUDGE_TYPE_PADDING:
                if ((event.getX() > (getWidth() - getTotalPaddingRight()))
                        && (event.getX() < (getWidth() - getPaddingRight()))) {
                    setText("");
                }
//					break;
//				default:
//					break;
//				}
            } else if (mErrorPup != null) {
                if (mErrorPup.isShowing()) {
                    mErrorPup.update();
                } else {
                    mErrorPup.show();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused && mErrorPup != null) {
            mErrorPup.show();
        }
        setDrawable();
    }

    // @Override
    // protected void onSelectionChanged(int selStart, int selEnd) {
    // super.onSelectionChanged(selStart, selEnd);
    // if(mErrorPup != null && mErrorPup.isShowing()){
    // mErrorPup.update();
    // }
    // }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mErrorPup != null && mErrorPup.isShowing()) {
            mErrorPup.dimiss();
            mErrorPup = null;
        }
    }

}
