package com.getbase.floatingactionbutton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.widget.ImageButton;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class FloatingActionButton extends ImageButton {

  public static final int SIZE_NORMAL = 0;
  public static final int SIZE_MINI = 1;

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({ SIZE_NORMAL, SIZE_MINI })
  public @interface FAB_SIZE {
  }

  private static final int HALF_TRANSPARENT_WHITE = Color.argb(128, 255, 255, 255);
  private static final int HALF_TRANSPARENT_BLACK = Color.argb(128, 0, 0, 0);

  int mColorNormal;
  int mColorPressed;
  @DrawableRes
  private int mIcon;
  private int mSize;

  private float mCircleSize;
  private float mShadowRadius;
  private float mShadowOffset;
  private int mDrawableSize;

  public FloatingActionButton(Context context) {
    this(context, null);
  }

  public FloatingActionButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public FloatingActionButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  void init(Context context, AttributeSet attributeSet) {
    TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionButton, 0, 0);
    mColorNormal = attr.getColor(R.styleable.FloatingActionButton_fab_colorNormal, getColor(android.R.color.holo_blue_dark));
    mColorPressed = attr.getColor(R.styleable.FloatingActionButton_fab_colorPressed, getColor(android.R.color.holo_blue_light));
    mSize = attr.getInt(R.styleable.FloatingActionButton_fab_size, SIZE_NORMAL);
    mIcon = attr.getResourceId(R.styleable.FloatingActionButton_fab_icon, 0);
    attr.recycle();

    updateCircleSize();
    mShadowRadius = getDimension(R.dimen.fab_shadow_radius);
    mShadowOffset = getDimension(R.dimen.fab_shadow_offset);
    updateDrawableSize();

    updateBackground();
  }

  private void updateDrawableSize() {
    mDrawableSize = (int) (mCircleSize + 2 * mShadowRadius);
  }

  private void updateCircleSize() {
    mCircleSize = getDimension(mSize == SIZE_NORMAL ? R.dimen.fab_size_normal : R.dimen.fab_size_mini);
  }

  public void setSize(@FAB_SIZE int size) {
    if (size != SIZE_MINI && size != SIZE_NORMAL) {
      throw new IllegalArgumentException("Use @FAB_SIZE constants only!");
    }

    if (mSize != size) {
      mSize = size;
      updateCircleSize();
      updateDrawableSize();
      updateBackground();
    }
  }

  @FAB_SIZE
  public int getSize() {
    return mSize;
  }

  public void setIcon(@DrawableRes int icon) {
    if (mIcon != icon) {
      mIcon = icon;
      updateBackground();
    }
  }

  /**
   * @return the current Color for normal state.
   */
  public int getColorNormal() {
    return mColorNormal;
  }

  public void setColorNormalResId(@ColorRes int colorNormal) {
    setColorNormal(getColor(colorNormal));
  }

  public void setColorNormal(int color) {
    if (mColorNormal != color) {
      mColorNormal = color;
      updateBackground();
    }
  }

  /**
   * @return the current color for pressed state.
   */
  public int getColorPressed() {
    return mColorPressed;
  }

  public void setColorPressedResId(@ColorRes int colorPressed) {
    setColorPressed(getColor(colorPressed));
  }

  public void setColorPressed(int color) {
    if (mColorPressed != color) {
      mColorPressed = color;
      updateBackground();
    }
  }

  int getColor(@ColorRes int id) {
    return getResources().getColor(id);
  }

  float getDimension(@DimenRes int id) {
    return getResources().getDimension(id);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    setMeasuredDimension(mDrawableSize, mDrawableSize);
  }

  void updateBackground() {
    float circleLeft = mShadowRadius;
    float circleTop = mShadowRadius - mShadowOffset;

    final RectF circleRect = new RectF(circleLeft, circleTop, circleLeft + mCircleSize, circleTop + mCircleSize);

    LayerDrawable layerDrawable = new LayerDrawable(
        new Drawable[] {
            getResources().getDrawable(mSize == SIZE_NORMAL ? R.drawable.fab_bg_normal : R.drawable.fab_bg_mini),
            createFillDrawable(circleRect),
            createStrokesDrawable(circleRect),
            getIconDrawable()
        });

    float iconOffset = (mCircleSize - getDimension(R.dimen.fab_icon_size)) / 2f;

    int iconInsetHorizontal = (int) (mShadowRadius + iconOffset);
    int iconInsetTop = (int) (circleTop + iconOffset);
    int iconInsetBottom = (int) (mShadowRadius + mShadowOffset + iconOffset);

    layerDrawable.setLayerInset(3, iconInsetHorizontal, iconInsetTop, iconInsetHorizontal, iconInsetBottom);

    setBackgroundCompat(layerDrawable);
  }

  Drawable getIconDrawable() {
    if (mIcon != 0) {
      return getResources().getDrawable(mIcon);
    } else {
      return new ColorDrawable(Color.TRANSPARENT);
    }
  }

  private StateListDrawable createFillDrawable(RectF circleRect) {
    StateListDrawable drawable = new StateListDrawable();
    drawable.addState(new int[] { android.R.attr.state_pressed }, createCircleDrawable(circleRect, mColorPressed));
    drawable.addState(new int[] { }, createCircleDrawable(circleRect, mColorNormal));
    return drawable;
  }

  private Drawable createCircleDrawable(RectF circleRect, int color) {
    final Bitmap bitmap = Bitmap.createBitmap(mDrawableSize, mDrawableSize, Config.ARGB_8888);
    final Canvas canvas = new Canvas(bitmap);

    final Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setColor(color);

    canvas.drawOval(circleRect, paint);

    return new BitmapDrawable(getResources(), bitmap);
  }

  private int opacityToAlpha(float opacity) {
    return (int) (255f * opacity);
  }

  private Drawable createStrokesDrawable(RectF circleRect) {
    final Bitmap bitmap = Bitmap.createBitmap(mDrawableSize, mDrawableSize, Config.ARGB_8888);
    final Canvas canvas = new Canvas(bitmap);

    final float strokeWidth = getDimension(R.dimen.fab_stroke_width);
    final float halfStrokeWidth = strokeWidth / 2f;

    RectF outerStrokeRect = new RectF(
        circleRect.left - halfStrokeWidth,
        circleRect.top - halfStrokeWidth,
        circleRect.right + halfStrokeWidth,
        circleRect.bottom + halfStrokeWidth
    );

    RectF innerStrokeRect = new RectF(
        circleRect.left + halfStrokeWidth,
        circleRect.top + halfStrokeWidth,
        circleRect.right - halfStrokeWidth,
        circleRect.bottom - halfStrokeWidth
    );

    final Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setStrokeWidth(strokeWidth);
    paint.setStyle(Style.STROKE);

    // outer
    paint.setColor(Color.BLACK);
    paint.setAlpha(opacityToAlpha(0.02f));
    canvas.drawOval(outerStrokeRect, paint);

    // inner bottom
    paint.setShader(new LinearGradient(innerStrokeRect.centerX(), innerStrokeRect.top, innerStrokeRect.centerX(), innerStrokeRect.bottom,
        new int[] { Color.TRANSPARENT, HALF_TRANSPARENT_BLACK, Color.BLACK },
        new float[] { 0f, 0.8f, 1f },
        TileMode.CLAMP
    ));
    paint.setAlpha(opacityToAlpha(0.04f));
    canvas.drawOval(innerStrokeRect, paint);

    // inner top
    paint.setShader(new LinearGradient(innerStrokeRect.centerX(), innerStrokeRect.top, innerStrokeRect.centerX(), innerStrokeRect.bottom,
        new int[] { Color.WHITE, HALF_TRANSPARENT_WHITE, Color.TRANSPARENT },
        new float[] { 0f, 0.2f, 1f },
        TileMode.CLAMP
    ));
    paint.setAlpha(opacityToAlpha(0.8f));
    canvas.drawOval(innerStrokeRect, paint);

    return new BitmapDrawable(getResources(), bitmap);
  }

  @SuppressWarnings("deprecation")
  @SuppressLint("NewApi")
  private void setBackgroundCompat(Drawable drawable) {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
      setBackground(drawable);
    } else {
      setBackgroundDrawable(drawable);
    }
  }
}
