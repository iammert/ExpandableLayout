package iammert.com.expandablelib.animations;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import iammert.com.expandablelib.ExpandableAnimation;
import iammert.com.expandablelib.ExpandableState;

import static iammert.com.expandablelib.ExpandableState.COLLAPSE;
import static iammert.com.expandablelib.ExpandableState.EXPAND;

/**
 * Created by T0173711 on 13/12/2017.
 */

public class ExpendableScaleAnimationAdvanced extends ExpandableAnimation {

    private int mExpendHeight;
    private LinearLayout.LayoutParams mLayoutParams;

    public ExpendableScaleAnimationAdvanced(int duration) {
        super(duration);
    }


    @Override
    public Animation generateAnimation(View parentView, final View childView, final ExpandableState expandableState) {
        // FIXME little trick to save height don't work if childViews don't have same height
        if (expandableState == COLLAPSE) {
            mExpendHeight = childView.getHeight();
        }
        return new CustomAnimation(childView, expandableState);
    }

    class CustomAnimation extends Animation {
        private View mView;
        private ExpandableState mState;
        private int mEndHeight;

        CustomAnimation(final View childView, final ExpandableState expandableState) {
            setDuration(getExpandableAnimationDuration());
            mView = childView;
            mView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            mEndHeight = mView.getMeasuredHeight();
            mState = expandableState;
            mLayoutParams = ((LinearLayout.LayoutParams) childView.getLayoutParams());
            if (mState == EXPAND) {
                mLayoutParams.height = 0;
            } else {
                mLayoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            }
            mView.setVisibility(View.VISIBLE);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {

            super.applyTransformation(interpolatedTime, t);
            if (interpolatedTime < 1.0f) {
                if (mState == EXPAND) {
                    mLayoutParams.height = (int) (mEndHeight * (interpolatedTime));
                } else {
                    mLayoutParams.height = (int) (mEndHeight * (1 - interpolatedTime));
                }
                mView.requestLayout();
            } else {
                if (mState == EXPAND) {
                    mLayoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    mView.requestLayout();
                } else {
                    mView.setVisibility(View.GONE);
                }
            }
        }
    }
}

