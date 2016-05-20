package com.demo.customview.flowlayout;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class FlowLayout extends ViewGroup {

	private static final String TAG = "FlowLayout";
	/**
	 * 存储本组件内所有的标签视图（TextView），
	 * 最外一层List存储行记录，里面的List存储本行内全部的标签的View,
	 * rowList< textOfRowList<textView > > 
	 */
	private List<List<View>> mAllRowViewList = new ArrayList<List<View>>();
	
	/**
	 * 记录每一行的最大高度
	 */
	private List<Integer> mAllRowHeightList = new ArrayList<Integer>();


	public FlowLayout(Context context) {
		super(context);
		//this(context, null);
	}
	
	public FlowLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		//this(context, attrs, 0);
	}

	public FlowLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/*
	 * Android API中如下介绍： LayoutParams are used by views to tell their parents
	 * how they want to be laid out. 意思大概是说：
	 * View通过LayoutParams类告诉其父视图它想要地大小(即，长度和宽度)。 (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#generateLayoutParams(android.view.ViewGroup.
	 * LayoutParams)
	 */
	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new MarginLayoutParams(p);
	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new MarginLayoutParams(getContext(), attrs);
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}

	/**
	 * 负责设置子控件的测量模式和大小 根据所有子控件设置自己的宽和高
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// 获得它的父容器为它设置的测量模式和大小
		int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
		int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
		int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
		int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

		Log.e(TAG, sizeWidth + "," + sizeHeight);

		// 如果是warp_content情况下，记录宽和高
		int width = 0;
		int height = 0;
		/**
		 * 记录每一行的宽度，width不断取最大宽度
		 */
		int rowWidth = 0;
		/**
		 * 每一行的高度，累加至height
		 */
		int rowHeight = 0;

		int cCount = getChildCount();

		// 遍历每个子元素
		for (int i = 0; i < cCount; i++) {
			View child = getChildAt(i);
			// 测量每一个child的宽和高
			measureChild(child, widthMeasureSpec, heightMeasureSpec);
			// 得到child的lp
			MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
			// 当前子空间实际占据的宽度
			int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
			// 当前子空间实际占据的高度
			int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
			/**
			 * 如果加入当前child，则超出最大宽度，则的到目前最大宽度给width，类加height 然后开启新行
			 */
			if (rowWidth + childWidth > sizeWidth) {
				width = Math.max(rowWidth, childWidth);// 取最大的
				rowWidth = childWidth; // 重新开启新行，开始记录
				// 叠加当前高度，
				height += rowHeight;
				// 开启记录下一行的高度
				rowHeight = childHeight;
			} else
			// 否则累加值lineWidth,lineHeight取最大高度
			{
				rowWidth += childWidth;
				rowHeight = Math.max(rowHeight, childHeight);
			}
			// 如果是最后一个，则将当前记录的最大宽度和当前lineWidth做比较
			if (i == cCount - 1) {
				width = Math.max(width, rowWidth);
				height += rowHeight;
			}

		}
		//根据计算结果，重新设置组件的高度和宽度
		setMeasuredDimension((modeWidth == MeasureSpec.EXACTLY) ? sizeWidth : width,
				(modeHeight == MeasureSpec.EXACTLY) ? sizeHeight : height);

	}


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		mAllRowViewList.clear();
		mAllRowHeightList.clear();

		int width = getWidth();

		int rowWidth = 0;
		int rowHeight = 0;
		// 存储每一行所有的childView
		List<View> oneRowViewList = new ArrayList<View>();
		int cCount = getChildCount();
		// 遍历所有的孩子
		for (int i = 0; i < cCount; i++) {
			View child = getChildAt(i);
			MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
			int childWidth = child.getMeasuredWidth();
			int childHeight = child.getMeasuredHeight();

			// 如果已经需要换行
			if (childWidth + lp.leftMargin + lp.rightMargin + rowWidth > width) {
				// 记录这一行所有的View以及最大高度
				mAllRowHeightList.add(rowHeight);
				// 将当前行的childView保存，然后开启新的ArrayList保存下一行的childView
				mAllRowViewList.add(oneRowViewList);
				rowWidth = 0;// 重置行宽
				oneRowViewList = new ArrayList<View>();
			}
			/**
			 * 如果不需要换行，则累加
			 */
			rowWidth += childWidth + lp.leftMargin + lp.rightMargin;
			rowHeight = Math.max(rowHeight, childHeight + lp.topMargin + lp.bottomMargin);
			oneRowViewList.add(child);
		}
		// 记录最后一行
		mAllRowHeightList.add(rowHeight);
		mAllRowViewList.add(oneRowViewList);

		int left = 0;
		int top = 0;
		// 得到总行数
		int rowNums = mAllRowViewList.size();
		for (int i = 0; i < rowNums; i++) {
			// 每一行的所有的views
			oneRowViewList = mAllRowViewList.get(i);
			// 当前行的最大高度
			rowHeight = mAllRowHeightList.get(i);

			Log.e(TAG, "第" + i + "行 ：" + oneRowViewList.size() + " , " + oneRowViewList);
			Log.e(TAG, "第" + i + "行， ：" + rowHeight);

			// 遍历当前行所有的View
			for (int j = 0; j < oneRowViewList.size(); j++) {
				View child = oneRowViewList.get(j);
				if (child.getVisibility() == View.GONE) {
					continue;
				}
				MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

				// 计算childView的left,top,right,bottom
				int lc = left + lp.leftMargin;
				int tc = top + lp.topMargin;
				int rc = lc + child.getMeasuredWidth();
				int bc = tc + child.getMeasuredHeight();

				Log.e(TAG, child + " , l = " + lc + " , t = " + t + " , r =" + rc + " , b = " + bc);

				child.layout(lc, tc, rc, bc);

				left += child.getMeasuredWidth() + lp.rightMargin + lp.leftMargin;
			}
			left = 0;
			top += rowHeight;
		}

	}
}
