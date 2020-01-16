package com.callme.platform.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.callme.platform.R;
import com.callme.platform.common.dialog.PopTip;
import com.callme.platform.util.ResourcesUtil;

/**
 * 图片圆角描边说明
 *
 */
public class RoundedImageView extends ImageView {

	public static final String TAG = "RoundedImageView";
	public static final float DEFAULT_RADIUS = 20;
	public static final float DEFAULT_BORDER_WIDTH = 1f;
	/**
	 * ImageView的属性android:scaleType，即ImageView.setScaleType(ImageView.ScaleType
	 * )。android:scaleType是控制图片如何resized/moved来匹对ImageView的size。
	 * ImageView.ScaleType / android:scaleType值的意义区别： CENTER /center
	 * 按图片的原来size居中显示，当图片长/宽超过View的长/宽，则截取图片的居中部分显示 CENTER_CROP / centerCrop
	 * 按比例扩大图片的size居中显示，使得图片长(宽)等于或大于View的长(宽) CENTER_INSIDE / centerInside
	 * 将图片的内容完整居中显示，通过按比例缩小或原来的size使得图片长/宽等于或小于View的长/宽 FIT_CENTER / fitCenter
	 * 把图片按比例扩大/缩小到View的宽度，居中显示 FIT_END / fitEnd
	 * 把图片按比例扩大/缩小到View的宽度，显示在View的下部分位置 FIT_START / fitStart
	 * 把图片按比例扩大/缩小到View的宽度，显示在View的上部分位置 FIT_XY / fitXY 把图片不按比例扩大/缩小到View的大小显示
	 * MATRIX / matrix 用矩阵来绘制
	 */
	// 这个还是需要，这个是本来就是系统原生的，在这里定义主要是方便xml可配置
	private static final ScaleType[] SCALE_TYPES = { ScaleType.MATRIX,
			ScaleType.FIT_XY, ScaleType.FIT_START, ScaleType.FIT_CENTER,
			ScaleType.FIT_END, ScaleType.CENTER, ScaleType.CENTER_CROP,
			ScaleType.CENTER_INSIDE };

	private float cornerRadius = DEFAULT_RADIUS;
	private float borderWidth = DEFAULT_BORDER_WIDTH;
	// private ColorStateList borderColor =
	// ColorStateList.valueOf(getResources()
	// .getColor(RoundedDrawable.DEFAULT_BORDER_COLOR));

	private int borderColor = getResources().getColor(
			RoundedDrawable.DEFAULT_BORDER_COLOR);
	private boolean isOval = false;
	private boolean mutateBackground = false;

	private int mResource;
	private Drawable mDrawable;
	private Drawable mBackgroundDrawable;

	private ScaleType mScaleType;

	private PopTip mErrorPup;
	private String mErrorMsg;

	private Bitmap mErrorBitmap;
	private Drawable mFlagDrawable;
	private Paint mErrorPaint;
	private int leftPosition;
	private int mLabelOffsetX;
	private int mLabelOffsetY;
	private boolean mFlagVisible = false;

	public RoundedImageView(Context context) {
		super(context);
	}

	public RoundedImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.RoundedImageView, defStyle, 0);

		int index = a
				.getInt(R.styleable.RoundedImageView_android_scaleType, -1);
		if (index >= 0) {
			setScaleType(SCALE_TYPES[index]);
		} else {
			// default scaletype to FIT_CENTER
			setScaleType(ScaleType.FIT_CENTER);
		}

		cornerRadius = a.getDimensionPixelSize(
				R.styleable.RoundedImageView_corner_radius, -1);
		borderWidth = a.getDimensionPixelSize(
				R.styleable.RoundedImageView_border_width, -1);

		mFlagDrawable = a.getDrawable(R.styleable.RoundedImageView_flag_pic);
		mLabelOffsetX = Math.max(0, a.getDimensionPixelOffset(
				R.styleable.RoundedImageView_offset_toleft, -1));
		mLabelOffsetY = Math.max(0, a.getDimensionPixelOffset(
				R.styleable.RoundedImageView_offset_totop, -1));
		if (mFlagDrawable == null) {
//			mFlagDrawable = getResources().getDrawable(
//					R.drawable.directional_flag);
		}
		// don't allow negative values for radius and border
		if (cornerRadius < 0) {
			cornerRadius = DEFAULT_RADIUS;
		}
		if (borderWidth < 0) {
			borderWidth = DEFAULT_BORDER_WIDTH;
		}

		// borderColor = a
		// .getColorStateList(R.styleable.RoundedImageView_border_color);
		// if (borderColor == null) {
		// borderColor = ColorStateList
		// .valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR);
		// }

		mutateBackground = a.getBoolean(
				R.styleable.RoundedImageView_mutate_background, false);
		isOval = a.getBoolean(R.styleable.RoundedImageView_oval, false);
		borderColor = a.getColor(R.styleable.RoundedImageView_border_color,
				RoundedDrawable.DEFAULT_BORDER_COLOR);

		updateDrawableAttrs();
		updateBackgroundDrawableAttrs(true);
		a.recycle();

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mErrorPup != null) {
				if (mErrorPup.isShowing()) {
					mErrorPup.update();
				} else {
					mErrorPup.show();
				}
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.save();
		if (mErrorPup != null && mErrorBitmap != null) {
			canvas.drawBitmap(mErrorBitmap, leftPosition, 5, mErrorPaint);
		}
		if (mFlagDrawable != null && mFlagVisible) {
			mFlagDrawable.setBounds(0, 0, mFlagDrawable.getIntrinsicWidth(),
					mFlagDrawable.getIntrinsicHeight());
			canvas.translate(mLabelOffsetX, mLabelOffsetY);
			mFlagDrawable.draw(canvas);
		}
		canvas.restore();
	}

	public final void setErrorMsg(String error) {
		if (!TextUtils.isEmpty(error)) {
			setBorderColor(ResourcesUtil.getColor(R.color.common_btn_red_bg));
			mErrorMsg = error;
			mErrorPup = new PopTip(getContext(), this, error);
			mErrorBitmap = ResourcesUtil.getBitmap(R.drawable.icon_error);
			mErrorPaint = new Paint();
			leftPosition = getWidth() - mErrorBitmap.getWidth() - 10;
		} else {
			setBorderColor(ResourcesUtil.getColor(R.color.common_grey_bg_color));
			if (mErrorPup != null) {
				mErrorPup.dimiss();
				mErrorPup = null;
				mErrorMsg = null;
			}
			if (mErrorBitmap != null && !mErrorBitmap.isRecycled()) {
				mErrorBitmap.recycle();
			}
			mErrorPaint = null;
			mErrorBitmap = null;
		}
	}

	public final void setErrorMsg(String error, int width) {
		if (!TextUtils.isEmpty(error)) {
			setBorderColor(ResourcesUtil.getColor(R.color.common_btn_red_bg));
			mErrorMsg = error;
			mErrorPup = new PopTip(getContext(), this, error);
			mErrorBitmap = ResourcesUtil.getBitmap(R.drawable.icon_error);
			mErrorPaint = new Paint();
			leftPosition = width - mErrorBitmap.getWidth() - 10;
		} else {
			setBorderColor(ResourcesUtil.getColor(R.color.common_grey_bg_color));
			if (mErrorPup != null) {
				mErrorPup.dimiss();
				mErrorPup = null;
				mErrorMsg = null;
			}
			if (mErrorBitmap != null && !mErrorBitmap.isRecycled()) {
				mErrorBitmap.recycle();
			}
			mErrorPaint = null;
			mErrorBitmap = null;
		}
	}

	public final String getErrorMsg() {
		return mErrorMsg;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mErrorPup != null && mErrorPup.isShowing()) {
			mErrorPup.dimiss();
			mErrorPup = null;
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

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		invalidate();
	}

	/**
	 * Return the current scale type in use by this ImageView.
	 * 
	 * @attr ref android.R.styleable#ImageView_scaleType
	 * @see ScaleType
	 */
	@Override
	public ScaleType getScaleType() {
		return mScaleType;
	}

	/**
	 * Controls how the image should be resized or moved to match the size of
	 * this ImageView.
	 * 
	 * @param scaleType
	 *            The desired scaling mode.
	 * @attr ref android.R.styleable#ImageView_scaleType
	 */
	@Override
	public void setScaleType(ScaleType scaleType) {
		assert scaleType != null;

		if (mScaleType != scaleType) {
			mScaleType = scaleType;

			switch (scaleType) {
			case CENTER:
			case CENTER_CROP:
			case CENTER_INSIDE:
			case FIT_CENTER:
			case FIT_START:
			case FIT_END:
			case FIT_XY:
				super.setScaleType(ScaleType.FIT_XY);
				break;
			default:
				super.setScaleType(scaleType);
				break;
			}

			updateDrawableAttrs();
			updateBackgroundDrawableAttrs(false);
			invalidate();
		}
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		mResource = 0;
		mDrawable = RoundedDrawable.fromDrawable(drawable);
		updateDrawableAttrs();
		super.setImageDrawable(mDrawable);
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		mResource = 0;
		mDrawable = RoundedDrawable.fromBitmap(bm);
		updateDrawableAttrs();
		super.setImageDrawable(mDrawable);
	}

	@Override
	public void setImageResource(int resId) {
		if (mResource != resId) {
			mResource = resId;
			mDrawable = resolveResource();
			updateDrawableAttrs();
			super.setImageDrawable(mDrawable);
		}
	}

	@Override
	public void setImageURI(Uri uri) {
		super.setImageURI(uri);
		setImageDrawable(getDrawable());
	}

	private Drawable resolveResource() {
		Resources rsrc = getResources();
		if (rsrc == null) {
			return null;
		}

		Drawable d = null;

		if (mResource != 0) {
			try {
				d = rsrc.getDrawable(mResource);
			} catch (Exception e) {
				Log.w(TAG, "Unable to find resource: " + mResource, e);
				// Don't try again.
				mResource = 0;
			}
		}
		return RoundedDrawable.fromDrawable(d);
	}

	private void updateDrawableAttrs() {
		updateAttrs(mDrawable);
	}

	private void updateBackgroundDrawableAttrs(boolean convert) {
		if (mutateBackground) {
			if (convert) {
				mBackgroundDrawable = RoundedDrawable
						.fromDrawable(mBackgroundDrawable);
			}
			updateAttrs(mBackgroundDrawable);
		}
	}

	private void updateAttrs(Drawable drawable) {
		if (drawable == null) {
			return;
		}

		if (drawable instanceof RoundedDrawable) {
			((RoundedDrawable) drawable).setScaleType(mScaleType)
					.setCornerRadius(cornerRadius).setBorderWidth(borderWidth)
					.setBorderColor(borderColor).setOval(isOval);
		} else if (drawable instanceof LayerDrawable) {
			// loop through layers to and set drawable attrs
			LayerDrawable ld = ((LayerDrawable) drawable);
			for (int i = 0, layers = ld.getNumberOfLayers(); i < layers; i++) {
				updateAttrs(ld.getDrawable(i));
			}
		}
	}

	@Override
	@Deprecated
	public void setBackgroundDrawable(Drawable background) {
		mBackgroundDrawable = background;
		updateBackgroundDrawableAttrs(true);
		super.setBackgroundDrawable(mBackgroundDrawable);
	}

	public float getCornerRadius() {
		return cornerRadius;
	}

	public void setCornerRadius(int resId) {
		setCornerRadius(getResources().getDimension(resId));
	}

	public void setCornerRadius(float radius) {
		if (cornerRadius == radius) {
			return;
		}

		cornerRadius = radius;
		updateDrawableAttrs();
		updateBackgroundDrawableAttrs(false);
	}

	public float getBorderWidth() {
		return borderWidth;
	}

	/**
	 * 描边宽度
	 */
	public void setBorderWidth(int resId) {
		setBorderWidth(getResources().getDimension(resId));
	}

	public void setBorderWidth(float width) {
		if (borderWidth == width) {
			return;
		}

		borderWidth = width;
		updateDrawableAttrs();
		updateBackgroundDrawableAttrs(false);
		invalidate();
	}

	public int getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(int color) {
		if (borderColor == color) {
			return;
		}
		borderColor = color;
		updateDrawableAttrs();
		updateBackgroundDrawableAttrs(false);
		if (borderWidth > 0) {
			invalidate();
		}
	}

	// public ColorStateList getBorderColors() {
	// return borderColor;
	// }
	//
	// public void setBorderColor(ColorStateList colors) {
	// if (borderColor.equals(colors)) {
	// return;
	// }
	//
	// borderColor = (colors != null) ? colors : ColorStateList
	// .valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR);
	// updateDrawableAttrs();
	// updateBackgroundDrawableAttrs(false);
	// if (borderWidth > 0) {
	// invalidate();
	// }
	// }

	public boolean isOval() {
		return isOval;
	}

	public void setOval(boolean oval) {
		isOval = oval;
		updateDrawableAttrs();
		updateBackgroundDrawableAttrs(false);
		invalidate();
	}

	public boolean isMutateBackground() {
		return mutateBackground;
	}

	public void setMutateBackground(boolean mutate) {
		if (mutateBackground == mutate) {
			return;
		}

		mutateBackground = mutate;
		updateBackgroundDrawableAttrs(true);
		invalidate();
	}

	public void setFlagVisible(boolean visible) {
		if (mFlagVisible && visible || !mFlagVisible && !visible) {
			return;
		}
		mFlagVisible = visible;
		postInvalidate();
	}
}
