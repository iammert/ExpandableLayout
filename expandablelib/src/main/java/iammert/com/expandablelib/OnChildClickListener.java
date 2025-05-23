package iammert.com.expandablelib;

import android.view.View;

/**
 * Notified when a child row of an {@link ExpandableLayout} is clicked.
 */
public interface OnChildClickListener<P, C> {

    void onChildClicked(int parentIndex, P parent, int childIndex, C child, View view);
}
