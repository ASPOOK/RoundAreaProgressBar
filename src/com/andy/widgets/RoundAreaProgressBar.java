package com.andy.widgets;

import com.andy.roundareaprogressbar.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Paint.FontMetrics;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

/**
 * RoundAreaProgressBar
 * 
 * @author Andy 
 * @since 2015-05-19
 */
public class RoundAreaProgressBar extends View {

	/** 圆的填充颜色 */
	private int circleBgColor;
	/** 已完成进度部分的填充色 */
	private int filledColor;
	/** 文本颜色 */
	private int textColor;
	/** 圆半径 */
	private float radius;
	/** 文本大小 */
	private int textSize;
	/** 圆环宽度，最好使用默认值 */
	private float circleWidth;
	/** 是否显示文本 */
	private boolean isShowText;
	/** 当前进度 */
	private int mProgress;
	/** 进度最大值，默认为100 */
	private int maxValue = 100;

	/** 弧度 */
	private double mRadians;
	/** 角度，用于绘制扇形 */
	private float mStartAngle;
	private float mSwipeAngle;

	private Paint mCirclePaint;
	private Paint mTextPaint;
	private Paint mTrianglePaint;
	private RectF mRectF;
	private Path mTrianglePath;

	/** 圆心 */
	private float centerX;
	private float centerY;
	
	private String mSuffix = "%";

	/** 进度对应的弧度（0 <= progress <= 100） */
	private final static double[] PROGRESS_IN_RADIANS = { 0, 0.364413d,
			0.4616d, 0.530831d, 0.586699d, 0.634474d, 0.676734d, 0.714958d,
			0.750081d, 0.782736d, 0.813377d, 0.842337d, 0.869872d, 0.896184d,
			0.921432d, 0.945747d, 0.969237d, 0.991993d, 1.01409d, 1.0356d,
			1.05657d, 1.07706d, 1.0971d, 1.11674d, 1.13601d, 1.15494d,
			1.17356d, 1.19189d, 1.20996d, 1.22779d, 1.24539d, 1.26279d,
			1.27999d, 1.29702d, 1.31389d, 1.33061d, 1.3472d, 1.36366d, 1.38d,
			1.39625d, 1.4124d, 1.42847d, 1.44446d, 1.46039d, 1.47627d,
			1.49209d, 1.50788d, 1.52364d, 1.53937d, 1.55509d, 0.5 * Math.PI,
			1.58651d, 1.60222d, 1.61796d, 1.63371d, 1.6495d, 1.66533d, 1.6812d,
			1.69713d, 1.71313d, 1.72919d, 1.74535d, 1.76159d, 1.77794d,
			1.7944d, 1.81098d, 1.8277d, 1.84457d, 1.8616d, 1.8788d, 1.8962d,
			1.9138d, 1.93163d, 1.9497d, 1.96803d, 1.98665d, 2.00558d, 2.02485d,
			2.04449d, 2.06454d, 2.08502d, 2.10599d, 2.1275d, 2.1496d, 2.17236d,
			2.19585d, 2.22016d, 2.24541d, 2.27172d, 2.29926d, 2.32822d,
			2.35886d, 2.39151d, 2.42663d, 2.46486d, 2.50712d, 2.55489d,
			2.61076d, 2.67999d, 2.77718d, Math.PI };

	public RoundAreaProgressBar(Context context) {
		this(context, null);
	}

	public RoundAreaProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundAreaProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	public void init(Context context, AttributeSet attrs) {
		initPaintRect();

		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.RoundAreaProgressBar);
		circleBgColor = ta.getColor(
				R.styleable.RoundAreaProgressBar_circleBackgroundColor,
				getResources().getColor(R.color.bg_color));
		filledColor = ta.getColor(R.styleable.RoundAreaProgressBar_filledColor,
				getResources().getColor(R.color.filled_color));
		textColor = ta.getColor(R.styleable.RoundAreaProgressBar_textColor,
				getResources().getColor(R.color.text_color));
		radius = ta.getDimension(R.styleable.RoundAreaProgressBar_circleRadius,
				getResources().getDimensionPixelSize(R.dimen.circle_radius));
		textSize = ta.getDimensionPixelSize(
				R.styleable.RoundAreaProgressBar_textSize, getResources()
						.getDimensionPixelSize(R.dimen.text_size));
		circleWidth = ta.getDimension(
				R.styleable.RoundAreaProgressBar_circleWidth, getResources()
						.getDimension(R.dimen.circle_width));
		isShowText = ta.getBoolean(R.styleable.RoundAreaProgressBar_showText,
				true);

		ta.recycle();
	}
	
	public void initPaintRect() {
		mCirclePaint = new Paint();
		mTextPaint = new Paint();
		mTrianglePaint = new Paint();
		mRectF = new RectF();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		centerX = radius + circleWidth;
		centerY = radius + circleWidth;

		float realRadius = radius - circleWidth / 2;
		float filledHeight = getFilledHeightByProgress();
		float width = (float) (realRadius * Math.sin(mRadians));

		/**
		 * 绘制圆环并填充
		 */
		mCirclePaint.setColor(circleBgColor);
		mCirclePaint.setStrokeWidth(circleWidth);
		mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mCirclePaint.setAntiAlias(true);
		canvas.drawCircle(centerX, centerX, radius, mCirclePaint);

		/**
		 * 绘制扇形并填充
		 */
		mCirclePaint.setColor(filledColor);
		mCirclePaint.setStyle(Paint.Style.FILL);
		mCirclePaint.setAntiAlias(true);
		mRectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
		canvas.drawArc(mRectF, mStartAngle, mSwipeAngle, true, mCirclePaint);

		/**
		 * 填充三角形
		 */
		mTrianglePath = new Path();
		mTrianglePath.moveTo(centerX, centerY);
		mTrianglePath.lineTo(centerX - width, centerY + radius - filledHeight);
		mTrianglePath.lineTo(centerX + width, centerY + radius - filledHeight);
		mTrianglePath.lineTo(centerX, centerY);
		mTrianglePath.close();

		if (mProgress > 0 && mProgress < maxValue / 2) {
			mTrianglePaint.setColor(circleBgColor);
			mTrianglePaint.setStrokeWidth(2.0f);
			mTrianglePaint.setStyle(Paint.Style.FILL_AND_STROKE);
			canvas.drawPath(mTrianglePath, mTrianglePaint);
		} else if (mProgress > maxValue / 2 && mProgress < maxValue) {
			mTrianglePaint.setColor(filledColor);
			mTrianglePaint.setStrokeWidth(2.0f);
			mTrianglePaint.setStyle(Paint.Style.FILL_AND_STROKE);
			canvas.drawPath(mTrianglePath, mTrianglePaint);
		}

		/**
		 * 绘制文字
		 */
		if (isShowText) {
			mTextPaint.setColor(textColor);
			mTextPaint.setTextSize(textSize);
			String text = mProgress + mSuffix;
			float textWidth = mTextPaint.measureText(text);
			FontMetrics fontMetrics = mTextPaint.getFontMetrics();
			float ascent = fontMetrics.ascent;
			float descent = fontMetrics.descent;
			float baseY = (ascent + descent) / 2;
			canvas.drawText(text, centerX - textWidth / 2, centerY - baseY, mTextPaint);
		}

	}

	/**
	 * 
	 * @return current progress
	 */
	public int getProgress() {
		return mProgress;
	}

	/**
	 * set current progress
	 * @param progress
	 */
	public void setProgress(int progress) {
		mProgress = progress;
		invalidate();
	}

	public int getCircleBgColor() {
		return circleBgColor;
	}

	public void setCircleBgColor(int circleBgColor) {
		this.circleBgColor = circleBgColor;
	}

	public int getFilledColor() {
		return filledColor;
	}

	public void setFilledColor(int filledColor) {
		this.filledColor = filledColor;
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public float getRadius() {
		return radius;
	}

	/**
	 * set the circle's radius
	 * @param radius
	 */
	public void setRadius(float radius) {
		this.radius = radius;
	}

	public int getTextSize() {
		return textSize;
	}

	public void setTextSize(int textSize) {
		this.textSize = textSize;
	}

	public boolean isShowText() {
		return isShowText;
	}

	public void setShowText(boolean isShowText) {
		this.isShowText = isShowText;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public float getCenterX() {
		return centerX;
	}

	public void setCenterX(float centerX) {
		this.centerX = centerX;
	}

	public float getCenterY() {
		return centerY;
	}

	public void setCenterY(float centerY) {
		this.centerY = centerY;
	}

	/**
	 * 获取颜色填充部分沿竖直直径方向的高度
	 * 
	 * @return
	 */
	public float getFilledHeightByProgress() {
		float mFilledHeight = 0;
		if (mProgress == 0) {
			mRadians = PROGRESS_IN_RADIANS[mProgress];
			mStartAngle = 90;
			mSwipeAngle = 0;
			mFilledHeight = 0;
		} else if (mProgress > 0 && mProgress < maxValue / 2) {
			mFilledHeight = getFilledHeight(true);
		} else if (mProgress == maxValue / 2) {
			mRadians = PROGRESS_IN_RADIANS[mProgress];			
			mStartAngle = 0;
			mSwipeAngle = 180;
			mFilledHeight = radius;
		} else if (mProgress > maxValue / 2 && mProgress < maxValue) {
			mFilledHeight = getFilledHeight(false);
		} else if (mProgress == maxValue) {
			mRadians = PROGRESS_IN_RADIANS[mProgress];			
			mStartAngle = -90;
			mSwipeAngle = 360;
			mFilledHeight = radius * 2;
		}

		return mFilledHeight;
	}

	/**
	 * 
	 * @param isBelowHorizontalDiameter
	 *            进度水平线是否在在水平直径下面
	 */
	public float getFilledHeight(boolean isBelowHorizontalDiameter) {
		float height = 0;
		if (isBelowHorizontalDiameter) {
			mRadians = PROGRESS_IN_RADIANS[mProgress];
			mStartAngle = (float) (90 - mRadians * 180 / Math.PI);
			mSwipeAngle = (float) (mRadians * 180 / Math.PI) * 2;
			height = (float) (radius - radius * Math.cos(mRadians));
		} else {
			mRadians = PROGRESS_IN_RADIANS[mProgress];
			mStartAngle = (float) (90 - mRadians * 180 / Math.PI);
			mSwipeAngle = (float) (mRadians * 180 / Math.PI) * 2;
			height = (float) (radius + radius * Math.cos(Math.PI - mRadians));
		}
		return height;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);

		int borderWidth = (int) (circleWidth * 2);

		if (heightMode != MeasureSpec.EXACTLY) {

			int exceptHeight = (int) (getPaddingTop() + getPaddingBottom()
					+ radius * 2 + borderWidth);
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(exceptHeight,
					MeasureSpec.EXACTLY);
		}
		if (widthMode != MeasureSpec.EXACTLY) {
			int exceptWidth = (int) (getPaddingLeft() + getPaddingRight()
					+ radius * 2 + borderWidth);
			widthMeasureSpec = MeasureSpec.makeMeasureSpec(exceptWidth,
					MeasureSpec.EXACTLY);
		}

		super.onMeasure(heightMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		// TODO Auto-generated method stub
		if (state instanceof Bundle) {
			final Bundle bundle = (Bundle) state;
			circleBgColor = bundle.getInt("CIRCLE_BG_COLOR");
			filledColor = bundle.getInt("FILLED_COLOR");
			textColor = bundle.getInt("TEXT_COLOR");
			radius = bundle.getFloat("CIRCLE_RADIUS");
			textSize = bundle.getInt("TEXT_SIZE");
			circleWidth = bundle.getFloat("CIRCLE_WIDTH");
			isShowText = bundle.getBoolean("IS_SHOWTEXT");
			mProgress = bundle.getInt("PROGRESS");
			maxValue = bundle.getInt("MAX_VALUE");
			setProgress(mProgress);
			initPaintRect();			
			super.onRestoreInstanceState(bundle.getParcelable("INSTANCE_STATE"));
			
			return;
		}
		super.onRestoreInstanceState(state);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		// TODO Auto-generated method stub
		final Bundle bundle = new Bundle();
        bundle.putParcelable("INSTANCE_STATE", super.onSaveInstanceState());
		bundle.putInt("CIRCLE_BG_COLOR", getCircleBgColor());
		bundle.putInt("FILLED_COLOR", getFilledColor());
		bundle.putInt("TEXT_COLOR", getTextColor());
		bundle.putFloat("CIRCLE_RADIUS", getRadius());
		bundle.putInt("TEXT_SIZE", getTextSize());
		bundle.putFloat("CIRCLE_WIDTH", circleWidth);
		bundle.putBoolean("IS_SHOWTEXT", isShowText());
		bundle.putInt("PROGRESS", getProgress());
		bundle.putInt("MAX_VALUE", getMaxValue());
		
		return bundle;
	}

}
