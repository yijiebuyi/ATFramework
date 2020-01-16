/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.callme.platform.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.CompoundButton;

import com.callme.platform.R;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.util.FloatProperty;

/**
 * A Switch is a two-state toggle switch widget that can select between two
 * options. The user may drag the "thumb" back and forth to choose the selected
 * option, or simply tap to toggle as if it were a checkbox.
 *
 */
public class Switch extends CompoundButton {
	private static final int THUMB_ANIMATION_DURATION = 250;

	private static final int TOUCH_MODE_IDLE = 0;
	private static final int TOUCH_MODE_DOWN = 1;
	private static final int TOUCH_MODE_DRAGGING = 2;
	private static final int TOUCH_MODE_CLICK = 3;

	private Drawable mThumbDrawable;
	private Drawable mTrackDrawable;
	private int mThumbTextPadding;
	private CharSequence mTextOn;
	private CharSequence mTextOff;
	private boolean mShowText;

	private int mTouchMode;
	private int mTouchSlop;
	private float mTouchX;
	private float mTouchY;
	private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
	private int mMinFlingVelocity;

	private float mThumbPosition;

	/**
	 * Width required to draw the switch track and thumb. Includes padding and
	 * optical bounds for both the track and thumb.
	 */
	private int mSwitchWidth;

	/**
	 * Height required to draw the switch track and thumb. Includes padding and
	 * optical bounds for both the track and thumb.
	 */
	private int mSwitchHeight;

	/**
	 * Width of the thumb's content region. Does not include padding or optical
	 * bounds.
	 */
	private int mThumbWidth;

	/** Left bound for drawing the switch track and thumb. */
	private int mSwitchLeft;

	/** Top bound for drawing the switch track and thumb. */
	private int mSwitchTop;

	/** Right bound for drawing the switch track and thumb. */
	private int mSwitchRight;

	/** Bottom bound for drawing the switch track and thumb. */
	private int mSwitchBottom;

	private TextPaint mTextPaint;
	private ColorStateList mTextColors;
	private Layout mOnLayout;
	private Layout mOffLayout;
	private ObjectAnimator mPositionAnimator;

	private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

	/**
	 * Construct a new Switch with default styling.
	 *
	 * @param context
	 *            The Context that will determine this widget's theming.
	 */
	public Switch(Context context) {
		this(context, null);
	}

	/**
	 * Construct a new Switch with default styling, overriding specific style
	 * attributes as requested.
	 *
	 * @param context
	 *            The Context that will determine this widget's theming.
	 * @param attrs
	 *            Specification of attributes that should deviate from default
	 *            styling.
	 */
	public Switch(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Construct a new Switch with a default style determined by the given theme
	 * attribute, overriding specific style attributes as requested.
	 *
	 * @param context
	 *            The Context that will determine this widget's theming.
	 * @param attrs
	 *            Specification of attributes that should deviate from the
	 *            default styling.
	 * @param defStyleAttr
	 *            An attribute in the current theme that contains a reference to
	 *            a style resource that supplies default values for the view.
	 *            Can be 0 to not look for defaults.
	 */
	public Switch(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Construct a new Switch with a default style determined by the given theme
	 * attribute or style resource, overriding specific style attributes as
	 * requested.
	 *
	 * @param context
	 *            The Context that will determine this widget's theming.
	 * @param attrs
	 *            Specification of attributes that should deviate from the
	 *            default styling.
	 * @param defStyleAttr
	 *            An attribute in the current theme that contains a reference to
	 *            a style resource that supplies default values for the view.
	 *            Can be 0 to not look for defaults.
	 * @param defStyleRes
	 *            A resource identifier of a style resource that supplies
	 *            default values for the view, used only if defStyleAttr is 0 or
	 *            can not be found in the theme. Can be 0 to not look for
	 *            defaults.
	 */
	public Switch(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr);

		mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

		final Resources res = getResources();
		mTextPaint.density = res.getDisplayMetrics().density;

		final TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.Switch, defStyleAttr, defStyleRes);
		mThumbDrawable = a.getDrawable(R.styleable.Switch_thumb);
		if (mThumbDrawable != null) {
			mThumbDrawable.setCallback(this);
		}
		mTrackDrawable = a.getDrawable(R.styleable.Switch_track);
		if (mTrackDrawable != null) {
			mTrackDrawable.setCallback(this);
		}
		mTextOn = a.getText(R.styleable.Switch_textOn);
		mTextOff = a.getText(R.styleable.Switch_textOff);
		mShowText = a.getBoolean(R.styleable.Switch_showText, true);
		mThumbTextPadding = a.getDimensionPixelSize(
				R.styleable.Switch_thumbTextPadding, 0);

		ColorStateList colors;
		int ts;

		colors = a.getColorStateList(R.styleable.Switch_switchTextColor);
		if (colors != null) {
			mTextColors = colors;
		} else {
			// If no color set in TextAppearance, default to the view's
			// textColor
			mTextColors = getTextColors();
		}

		ts = a.getDimensionPixelSize(R.styleable.Switch_switchTextSize, 0);
		if (ts != 0) {
			if (ts != mTextPaint.getTextSize()) {
				mTextPaint.setTextSize(ts);
				requestLayout();
			}
		}

		a.recycle();

		final ViewConfiguration config = ViewConfiguration.get(context);
		mTouchSlop = config.getScaledTouchSlop();
		mMinFlingVelocity = config.getScaledMinimumFlingVelocity();

		// Refresh display with current params
		refreshDrawableState();
		setChecked(isChecked());
	}

	@Override
	public void setChecked(boolean checked) {
		if (checked) {
			mThumbPosition = 1;
		} else {
			mThumbPosition = 0;
		}
		super.setChecked(checked);
	}

	/**
	 * Set the horizontal padding around the text drawn on the switch itself.
	 *
	 * @param pixels
	 *            Horizontal padding for switch thumb text in pixels
	 *
	 * @attr ref android.R.styleable#Switch_thumbTextPadding
	 */
	public void setThumbTextPadding(int pixels) {
		mThumbTextPadding = pixels;
		requestLayout();
	}

	/**
	 * Get the horizontal padding around the text drawn on the switch itself.
	 *
	 * @return Horizontal padding for switch thumb text in pixels
	 *
	 * @attr ref android.R.styleable#Switch_thumbTextPadding
	 */
	public int getThumbTextPadding() {
		return mThumbTextPadding;
	}

	/**
	 * Set the drawable used for the track that the switch slides within.
	 *
	 * @param track
	 *            Track drawable
	 *
	 * @attr ref android.R.styleable#Switch_track
	 */
	public void setTrackDrawable(Drawable track) {
		if (mTrackDrawable != null) {
			mTrackDrawable.setCallback(null);
		}
		mTrackDrawable = track;
		if (track != null) {
			track.setCallback(this);
		}
		requestLayout();
	}

	/**
	 * Set the drawable used for the track that the switch slides within.
	 *
	 * @param resId
	 *            Resource ID of a track drawable
	 *
	 * @attr ref android.R.styleable#Switch_track
	 */
	public void setTrackResource(int resId) {
		setTrackDrawable(getContext().getResources().getDrawable(resId));
	}

	/**
	 * Get the drawable used for the track that the switch slides within.
	 *
	 * @return Track drawable
	 *
	 * @attr ref android.R.styleable#Switch_track
	 */
	public Drawable getTrackDrawable() {
		return mTrackDrawable;
	}

	/**
	 * Set the drawable used for the switch "thumb" - the piece that the user
	 * can physically touch and drag along the track.
	 *
	 * @param thumb
	 *            Thumb drawable
	 *
	 * @attr ref android.R.styleable#Switch_thumb
	 */
	public void setThumbDrawable(Drawable thumb) {
		if (mThumbDrawable != null) {
			mThumbDrawable.setCallback(null);
		}
		mThumbDrawable = thumb;
		if (thumb != null) {
			thumb.setCallback(this);
		}
		requestLayout();
	}

	/**
	 * Set the drawable used for the switch "thumb" - the piece that the user
	 * can physically touch and drag along the track.
	 *
	 * @param resId
	 *            Resource ID of a thumb drawable
	 *
	 * @attr ref android.R.styleable#Switch_thumb
	 */
	public void setThumbResource(int resId) {
		setThumbDrawable(getContext().getResources().getDrawable(resId));
	}

	/**
	 * Get the drawable used for the switch "thumb" - the piece that the user
	 * can physically touch and drag along the track.
	 *
	 * @return Thumb drawable
	 *
	 * @attr ref android.R.styleable#Switch_thumb
	 */
	public Drawable getThumbDrawable() {
		return mThumbDrawable;
	}

	/**
	 * Returns the text displayed when the button is in the checked state.
	 *
	 * @attr ref android.R.styleable#Switch_textOn
	 */
	public CharSequence getTextOn() {
		return mTextOn;
	}

	/**
	 * Sets the text displayed when the button is in the checked state.
	 *
	 * @attr ref android.R.styleable#Switch_textOn
	 */
	public void setTextOn(CharSequence textOn) {
		mTextOn = textOn;
		requestLayout();
	}

	/**
	 * Returns the text displayed when the button is not in the checked state.
	 *
	 * @attr ref android.R.styleable#Switch_textOff
	 */
	public CharSequence getTextOff() {
		return mTextOff;
	}

	/**
	 * Sets the text displayed when the button is not in the checked state.
	 *
	 * @attr ref android.R.styleable#Switch_textOff
	 */
	public void setTextOff(CharSequence textOff) {
		mTextOff = textOff;
		requestLayout();
	}

	/**
	 * Sets whether the on/off text should be displayed.
	 *
	 * @param showText
	 *            {@code true} to display on/off text
	 * @attr ref android.R.styleable#Switch_showText
	 */
	public void setShowText(boolean showText) {
		if (mShowText != showText) {
			mShowText = showText;
			requestLayout();
		}
	}

	/**
	 * @return whether the on/off text should be displayed
	 * @attr ref android.R.styleable#Switch_showText
	 */
	public boolean getShowText() {
		return mShowText;
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mShowText) {
			if (mOnLayout == null) {
				mOnLayout = makeLayout(mTextOn);
			}

			if (mOffLayout == null) {
				mOffLayout = makeLayout(mTextOff);
			}
		}

		mThumbWidth = mThumbDrawable.getIntrinsicWidth();

		mSwitchWidth = mTrackDrawable.getIntrinsicWidth();
		mSwitchHeight = mTrackDrawable.getIntrinsicHeight();
		setMeasuredDimension(mSwitchWidth, mSwitchHeight);
	}

	private Layout makeLayout(CharSequence text) {
		final CharSequence transformed = text;

		return new StaticLayout(transformed, mTextPaint, (int) Math.ceil(Layout
				.getDesiredWidth(transformed, mTextPaint)),
				Layout.Alignment.ALIGN_NORMAL, 1.f, 0, false);
	}

	/**
	 * @return true if (x, y) is within the target area of the switch thumb
	 */
	private boolean hitThumb(float x, float y) {
		if (mThumbDrawable == null) {
			return false;
		}

		// Relies on mTempRect, MUST be called first!
		final int thumbOffset = getThumbOffset();

		final int thumbTop = mSwitchTop - mTouchSlop;
		final int thumbLeft = mSwitchLeft + thumbOffset - mTouchSlop;
		final int thumbRight = thumbLeft + mThumbWidth + mTouchSlop;
		final int thumbBottom = mSwitchBottom + mTouchSlop;
		return x > thumbLeft && x < thumbRight && y > thumbTop
				&& y < thumbBottom;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		mVelocityTracker.addMovement(ev);
		final int action = ev.getActionMasked();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			final float x = ev.getX();
			final float y = ev.getY();
			if (hitThumb(x, y)) {
				mTouchMode = TOUCH_MODE_DOWN;
				mTouchX = x;
				mTouchY = y;
			} else {
				mTouchMode = TOUCH_MODE_CLICK;
			}
			return true;
		}

		case MotionEvent.ACTION_MOVE: {
			switch (mTouchMode) {
			case TOUCH_MODE_IDLE:
				// Didn't target the thumb, treat normally.
				break;

			case TOUCH_MODE_DOWN: {
				final float x = ev.getX();
				final float y = ev.getY();
				if (Math.abs(x - mTouchX) > mTouchSlop
						|| Math.abs(y - mTouchY) > mTouchSlop) {
					mTouchMode = TOUCH_MODE_DRAGGING;
					getParent().requestDisallowInterceptTouchEvent(true);
					mTouchX = x;
					mTouchY = y;
					return true;
				}
				break;
			}

			case TOUCH_MODE_DRAGGING: {
				final float x = ev.getX();
				final int thumbScrollRange = getThumbScrollRange();
				final float thumbScrollOffset = x - mTouchX;
				float dPos;
				if (thumbScrollRange != 0) {
					dPos = thumbScrollOffset / thumbScrollRange;
				} else {
					// If the thumb scroll range is empty, just use the
					// movement direction to snap on or off.
					dPos = thumbScrollOffset > 0 ? 1 : -1;
				}

				final float newPos = constrain(mThumbPosition + dPos, 0, 1);
				if (newPos != mThumbPosition) {
					mTouchX = x;
					setThumbPosition(newPos);
				}
				return true;
			}
			}
			break;
		}

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL: {
			if (mTouchMode == TOUCH_MODE_DRAGGING) {
				stopDrag(ev);
				// Allow super class to handle pressed state, etc.
				super.onTouchEvent(ev);
				return true;
			} else if (mTouchMode == TOUCH_MODE_DOWN
					|| mTouchMode == TOUCH_MODE_CLICK) {
				performClick();
				mTouchMode = TOUCH_MODE_IDLE;
				mVelocityTracker.clear();
				return true;
			}
			mTouchMode = TOUCH_MODE_IDLE;
			mVelocityTracker.clear();
			break;
		}
		}

		return super.onTouchEvent(ev);
	}

	public static float constrain(float amount, float low, float high) {
		return amount < low ? low : (amount > high ? high : amount);
	}

	private void cancelSuperTouch(MotionEvent ev) {
		MotionEvent cancel = MotionEvent.obtain(ev);
		cancel.setAction(MotionEvent.ACTION_CANCEL);
		super.onTouchEvent(cancel);
		cancel.recycle();
	}

	/**
	 * Called from onTouchEvent to end a drag operation.
	 *
	 * @param ev
	 *            Event that triggered the end of drag mode - ACTION_UP or
	 *            ACTION_CANCEL
	 */
	private void stopDrag(MotionEvent ev) {
		mTouchMode = TOUCH_MODE_IDLE;

		// Commit the change if the event is up and not canceled and the switch
		// has not been disabled during the drag.
		final boolean commitChange = ev.getAction() == MotionEvent.ACTION_UP
				&& isEnabled();
		final boolean oldState = isChecked();
		final boolean newState;
		if (commitChange) {
			mVelocityTracker.computeCurrentVelocity(1000);
			final float xvel = mVelocityTracker.getXVelocity();
			if (Math.abs(xvel) > mMinFlingVelocity) {
				newState = xvel > 0;
			} else {
				newState = getTargetCheckedState();
			}
		} else {
			newState = oldState;
		}

		super.setChecked(newState);
		animateThumbToCheckedState(newState);

		cancelSuperTouch(ev);
	}

	private void animateThumbToCheckedState(boolean newCheckedState) {
		final float targetPosition = newCheckedState ? 1 : 0;
		mPositionAnimator = ObjectAnimator.ofFloat(this, THUMB_POS,
				targetPosition);
		mPositionAnimator.setDuration(THUMB_ANIMATION_DURATION);
		mPositionAnimator.start();
	}

	private void cancelPositionAnimator() {
		if (mPositionAnimator != null) {
			mPositionAnimator.cancel();
		}
	}

	private boolean getTargetCheckedState() {
		return mThumbPosition > 0.5f;
	}

	/**
	 * Sets the thumb position as a decimal value between 0 (off) and 1 (on).
	 *
	 * @param position
	 *            new position between [0,1]
	 */
	private void setThumbPosition(float position) {
		mThumbPosition = position;
		invalidate();
	}

	@Override
	public void toggle() {
		super.setChecked(!isChecked());
		animateThumbToCheckedState(isChecked());
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mSwitchLeft = 0;
		mSwitchTop = 0;
		mSwitchBottom = getHeight();
		mSwitchRight = mSwitchWidth;
	}

	@Override
	public void draw(Canvas c) {
		final int switchLeft = mSwitchLeft;
		final int switchTop = mSwitchTop;
		final int switchRight = mSwitchRight;
		final int switchBottom = mSwitchBottom;

		int thumbInitialLeft = switchLeft + getThumbOffset();

		// Layout the track.
		if (mTrackDrawable != null) {
			// If necessary, offset by the optical insets of the thumb asset.
			int trackLeft = switchLeft;
			int trackTop = switchTop;
			int trackRight = switchRight;
			int trackBottom = switchBottom;
			mTrackDrawable.setBounds(trackLeft, trackTop, trackRight,
					trackBottom);
		}

		// Layout the thumb.
		if (mThumbDrawable != null) {
			final int thumbLeft = thumbInitialLeft;
			final int thumbRight = thumbInitialLeft
					+ mThumbDrawable.getIntrinsicWidth();
			mThumbDrawable.setBounds(thumbLeft, switchTop, thumbRight,
					switchBottom);
		}

		// Draw the background.
		super.draw(c);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		final int saveCount = canvas.save();
		if (mTrackDrawable != null) {
			mTrackDrawable.draw(canvas);
		}

		if (mThumbDrawable != null) {
			mThumbDrawable.draw(canvas);
		}

		final Layout switchText = isChecked() ? mOnLayout : mOffLayout;
		if (switchText != null) {
			final int drawableState[] = getDrawableState();
			if (mTextColors != null) {
				mTextPaint.setColor(mTextColors.getColorForState(drawableState,
						0));
			}
			mTextPaint.drawableState = drawableState;

			final Rect bounds = mThumbDrawable.getBounds();

			int left = 0;

			if (!isChecked()) {
				left = bounds.right + mThumbTextPadding;
			} else {
				left = bounds.left - mThumbTextPadding - switchText.getWidth();
			}

			final int top = getHeight() / 2 - switchText.getHeight() / 2;
			canvas.translate(left, top);
			switchText.draw(canvas);
		}

		canvas.restoreToCount(saveCount);
	}

	@Override
	public int getCompoundPaddingLeft() {
		return super.getCompoundPaddingLeft();
	}

	@Override
	public int getCompoundPaddingRight() {
		return super.getCompoundPaddingRight() + mSwitchWidth;
	}

	private int getThumbOffset() {
		return (int) (mThumbPosition * getThumbScrollRange() + 0.5f);
	}

	private int getThumbScrollRange() {
		if (mTrackDrawable != null) {
			return mSwitchWidth - mThumbWidth;
		} else {
			return 0;
		}
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isChecked()) {
			mergeDrawableStates(drawableState, CHECKED_STATE_SET);
		}
		return drawableState;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();

		final int[] myDrawableState = getDrawableState();

		if (mThumbDrawable != null) {
			mThumbDrawable.setState(myDrawableState);
		}

		if (mTrackDrawable != null) {
			mTrackDrawable.setState(myDrawableState);
		}

		invalidate();
	}

	@Override
	protected boolean verifyDrawable(Drawable who) {
		return super.verifyDrawable(who) || who == mThumbDrawable
				|| who == mTrackDrawable;
	}

	@Override
	public void jumpDrawablesToCurrentState() {
		super.jumpDrawablesToCurrentState();

		if (mThumbDrawable != null) {
			mThumbDrawable.jumpToCurrentState();
		}

		if (mTrackDrawable != null) {
			mTrackDrawable.jumpToCurrentState();
		}

		if (mPositionAnimator != null && mPositionAnimator.isRunning()) {
			mPositionAnimator.end();
			mPositionAnimator = null;
		}
	}

	private static final FloatProperty<Switch> THUMB_POS = new FloatProperty<Switch>(
			"thumbPos") {
		@Override
		public Float get(Switch object) {
			return object.mThumbPosition;
		}

		@Override
		public void setValue(Switch object, float value) {
			object.setThumbPosition(value);
		}
	};
}
