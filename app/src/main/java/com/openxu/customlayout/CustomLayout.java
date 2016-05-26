package com.openxu.customlayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 自定义布局管理器的示例。 这是一个相当全面的布局管理器，处理了所有布局情况。 可以简化为更具体的案例。
 */
public class CustomLayout extends ViewGroup {

	private static final String TAG = "CustomLayout";

	public CustomLayout(Context context) {
		super(context);
	}

	public CustomLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CustomLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 要求所有的孩子测量自己的大小，然后根据这些孩子的大小完成自己的尺寸测量
	 */
	@SuppressLint("NewApi")
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		/** 
         * 获得此ViewGroup上级容器为其推荐的宽和高，以及计算模式 
         */  
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);  
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);  
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);  
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);  
        int layoutWidth = 0;
        int layoutHeight = 0;
		int cWidth = 0;
		int cHeight = 0;
		int count = getChildCount();  
		
		// 计算出所有的childView的宽和高
		for(int i = 0; i < count; i++){
			View child = getChildAt(i);  
			measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
		}
		CustomLayoutParams params = null;
		if(widthMode == MeasureSpec.EXACTLY){
			//如果布局容器的宽度模式时确定的（具体的size或者match_parent）
			layoutWidth = sizeWidth;
		}else{
			//如果是未指定或者wrap_content，我们都按照包裹内容做，宽度方向上只需要拿到所有子控件中宽度做大的作为布局宽度
			for (int i = 0; i < count; i++)  {  
				 View child = getChildAt(i);  
		         cWidth = child.getMeasuredWidth();  
		         params = (CustomLayoutParams) child.getLayoutParams();  
		         //获取子控件宽度和左右边距之和，作为这个控件需要占据的宽度
		         int marginWidth = cWidth+params.leftMargin+params.rightMargin;
		         layoutWidth = marginWidth > layoutWidth ? marginWidth : layoutWidth;
			}
		}
		//高度很宽度处理思想一样
		if(heightMode == MeasureSpec.EXACTLY){
			layoutHeight = sizeHeight;
		}else{
			for (int i = 0; i < count; i++)  {  
				 View child = getChildAt(i);  
				 cHeight = child.getMeasuredHeight();
				 params = (CustomLayoutParams) child.getLayoutParams();  
				 int marginHeight = cHeight+params.topMargin+params.bottomMargin;
				 layoutHeight = marginHeight > layoutHeight ? marginHeight : layoutHeight;
			}
		}
		
		// 测量并保存layout的宽高
		setMeasuredDimension(layoutWidth, layoutHeight);
	}

	/**
	 * 为所有的子控件摆放位置.
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		final int count = getChildCount();
		int childMeasureWidth = 0;
		int childMeasureHeight = 0;
		CustomLayoutParams params = null;
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			// 注意此处不能使用getWidth和getHeight，这两个方法必须在onLayout执行完，才能正确获取宽高
			childMeasureWidth = child.getMeasuredWidth();
			childMeasureHeight = child.getMeasuredHeight();
			params = (CustomLayoutParams) child.getLayoutParams();  
            switch (params.position) {
			case CustomLayoutParams.POSITION_MIDDLE:    // 中间
				left = (getWidth()-childMeasureWidth)/2 - params.rightMargin + params.leftMargin;
				top = (getHeight()-childMeasureHeight)/2 + params.topMargin - params.bottomMargin;
				break;
			case CustomLayoutParams.POSITION_LEFT:      // 左上方
				left = 0 + params.leftMargin;
				top = 0 + params.topMargin;
				break;
			case CustomLayoutParams.POSITION_RIGHT:     // 右上方
				left = getWidth()-childMeasureWidth - params.rightMargin;
				top = 0 + params.topMargin;
				break;
			case CustomLayoutParams.POSITION_BOTTOM:    // 左下角
				left = 0 + params.leftMargin;
				top = getHeight()-childMeasureHeight-params.bottomMargin;
				break;
			case CustomLayoutParams.POSITION_RIGHTANDBOTTOM:// 右下角
				left = getWidth()-childMeasureWidth - params.rightMargin;
				top = getHeight()-childMeasureHeight-params.bottomMargin;
				break;
			default:
				break;
			}
            
			// 确定子控件的位置，四个参数分别代表（左上右下）点的坐标值
			child.layout(left, top, left+childMeasureWidth, top+childMeasureHeight);
		}
		
	}

	/**
	 * 如果自定义布局参数，需要重写这个方法，返回我们自己的
	 */
	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new CustomLayoutParams(getContext(), attrs);
	}
    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new CustomLayoutParams(p);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new CustomLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof CustomLayoutParams;
    }
	public static class CustomLayoutParams extends MarginLayoutParams {
		public static final int POSITION_MIDDLE = 0; // 中间
		public static final int POSITION_LEFT = 1; // 左上方
		public static final int POSITION_RIGHT = 2; // 右上方
		public static final int POSITION_BOTTOM = 3; // 左下角
		public static final int POSITION_RIGHTANDBOTTOM = 4; // 右下角

		public int position = POSITION_LEFT;

		public CustomLayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
			TypedArray a = c.obtainStyledAttributes(attrs,R.styleable.CustomLayout);
			//获取设置在子控件上的位置属性
			position = a.getInt(R.styleable.CustomLayout_layout_position,position);

			a.recycle();
		}

		public CustomLayoutParams(int width, int height) {
			super(width, height);
		}

		public CustomLayoutParams(LayoutParams source) {
			super(source);
		}

	}

}
