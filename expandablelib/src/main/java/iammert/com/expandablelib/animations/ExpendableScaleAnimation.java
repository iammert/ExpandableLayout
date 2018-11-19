package iammert.com.expandablelib.animations;

import android.view.View;
import android.view.animation.Animation;

import iammert.com.expandablelib.ExpandableAnimation;
import iammert.com.expandablelib.ExpandableState;

/**
 * Created by T0173711 on 13/12/2017.
 */

public class ExpendableScaleAnimation extends ExpandableAnimation {
    public ExpendableScaleAnimation(int duration) {
        super(duration);
    }

    @Override
    public Animation generateAnimation(View parentView, final View childView, ExpandableState expandableState) {
        float startScale;
        float endScale;
        final int visibility;
        switch (expandableState) {
            case COLLAPSE:
                startScale = 1.F;
                endScale = 0.F;
                visibility = View.GONE;
                break;
            case EXPAND:
                startScale = 0.F;
                endScale = 1.F;
                visibility = View.VISIBLE;
                // prevent from animation to be "GONE"
                childView.setVisibility(visibility);
                break;
            default:
                return null;
        }
        Animation mAnimation =new android.view.animation.ScaleAnimation(
                1f, 1f, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                Animation.ABSOLUTE, 1f);
        mAnimation.setFillAfter(true); // Needed to keep the result of the animation
        mAnimation.setDuration(1000);
        mAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                childView.setVisibility(visibility);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return mAnimation;
    }
}
