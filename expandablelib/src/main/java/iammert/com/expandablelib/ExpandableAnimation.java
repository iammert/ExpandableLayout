package iammert.com.expandablelib;

import android.view.View;
import android.view.animation.Animation;

/**
 * Created by T0173711 on 13/12/2017.
 */

public abstract class ExpandableAnimation {

    private int mExpandableAnimationDuration;

    public int getExpandableAnimationDuration() {
        return mExpandableAnimationDuration;
    }

    public ExpandableAnimation(int expandableAnimationDuration){
        this.mExpandableAnimationDuration=expandableAnimationDuration;
    }

    public abstract Animation generateAnimation(View parentView, View childView, ExpandableState expandableState);
}