/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：弹出式菜单
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
package com.callme.platform.common.dialog;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.callme.platform.R;
import com.callme.platform.util.ResourcesUtil;

import java.util.ArrayList;

public class CmPopupMenu extends PopupWindow {
    private ArrayList<String> mItemList;
    private ArrayList<Integer> mImgList;
    private Context mContext;
    private PopupWindow mPopupWindow;
    private ListView mListView;
    private View mRootView;
    private OnItemClickListener mListener;

    public CmPopupMenu(Context ctx) {
        super(ctx);
        init(ctx, 0);
    }

    public CmPopupMenu(Context ctx, int pix) {
        init(ctx, pix);
    }

    private void init(Context ctx, int pix) {
        mContext = ctx;
        mRootView = LayoutInflater.from(ctx).inflate(
                R.layout.base_popup_menu, null);
        mItemList = new ArrayList<String>();
        mImgList = new ArrayList<Integer>();
        mListView = mRootView.findViewById(R.id.listView);

        if (pix == 0)
            mListView.setAdapter(new PopupMenuAdapter());
        else
            mListView.setAdapter(new PopupMenuAdapter(pix));
        mPopupWindow = new PopupWindow(mRootView, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
    }

    public void setBg(int resId) {
        mRootView.findViewById(R.id.popup_view_content).setBackgroundResource(
                resId);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mListener.onItemClick(parent, view, position, id);
                dismiss();
            }

        });
    }

    public void addTextItems(String[] items) {
        for (String s : items)
            mItemList.add(s);
    }

    public void addImgItems(Integer[] imgItems) {
        for (Integer s : imgItems)
            mImgList.add(s);
    }

    public Object getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public void showAsDropDown(View parent) {
        mRootView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        final int listWidth = mListView.getMeasuredWidth();
        int offset = 0;
        Drawable rightIcon = null;// ResourcesUtil.getDrawable(R.drawable.message_normal);
        if (rightIcon != null) {
            offset = rightIcon.getIntrinsicWidth() / 2;
        }
        offset -= ResourcesUtil.getDimensionPixelOffset(R.dimen.px39);
        mPopupWindow.showAsDropDown(parent,
                -listWidth + parent.getMeasuredWidth() / 2 + offset, 0);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.update();
    }

    @Override
    public void showAsDropDown(View parent, int x, int y) {
        mPopupWindow.showAsDropDown(parent, x, y);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.update();
    }

    @Override
    public void dismiss() {
        mPopupWindow.dismiss();
    }

    private final class PopupMenuAdapter extends BaseAdapter {
        public final static int POP_DEFAULT_TYPE = 0;
        public final static int POP_PX24_TYPE = 1;
        private ItemHolder mHolder = null;
        private int mTextSizeType;

        public PopupMenuAdapter() {
            mTextSizeType = POP_DEFAULT_TYPE;
        }

        /**
         * 这里直接设置像素有问题。暂时默认了。
         *
         * @param pix
         */
        public PopupMenuAdapter(int pix) {
            mTextSizeType = POP_PX24_TYPE;
        }

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                if (mTextSizeType == POP_DEFAULT_TYPE)
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.base_popup_menu_item, null);
                else
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.base_popup_menu_item_24px, null);
                TextView menuItem = (TextView) convertView;
                menuItem.setText(mItemList.get(position));
                mHolder = new ItemHolder();
                mHolder.mMenuItem = menuItem;
                convertView.setTag(mHolder);
            } else {
                mHolder = (ItemHolder) convertView.getTag();
            }
            try {
                mHolder.mMenuItem.setText(mItemList.get(position));
                Drawable d = ResourcesUtil.getDrawable(mImgList.get(position));
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                mHolder.mMenuItem.setCompoundDrawables(d, null, null, null);
            } catch (Exception e) {

            }
            return convertView;
        }
    }

    private static class ItemHolder {
        TextView mMenuItem;
    }

}
