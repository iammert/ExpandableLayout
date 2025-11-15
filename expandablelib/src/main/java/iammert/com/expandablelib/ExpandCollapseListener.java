package iammert.com.expandablelib;

import android.view.View;

/**
 * Listeners notified when a section of {@link ExpandableLayout} changes state.
 *
 * Created by mertsimsek on 28/07/2017.
 */

public interface ExpandCollapseListener<P> {

    /**
     * Called after a section was expanded by the user or from code.
     * {@code view} is the parent row of the section.
     */
    @FunctionalInterface
    interface ExpandListener<P> {
        void onExpanded(int parentIndex, P parent, View view);
    }

    /**
     * Called after a section was collapsed by the user or from code.
     * {@code view} is the parent row of the section.
     */
    @FunctionalInterface
    interface CollapseListener<P> {
        void onCollapsed(int parentIndex, P parent, View view);
    }
}
