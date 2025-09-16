package iammert.com.expandablelib;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A parent and its children shown by {@link ExpandableLayout}.
 *
 * Created by mertsimsek on 28/07/2017.
 */

public class Section<P, C> {
    public boolean expanded;
    public P parent;
    public List<C> children;

    public Section() {
        children = new ArrayList<>();
        expanded = false;
    }

    public Section(P parent) {
        this();
        this.parent = parent;
    }

    /**
     * The children are copied, later changes to the given list are not
     * reflected in the section.
     */
    public Section(P parent, @NonNull List<C> children) {
        this(parent, children, false);
    }

    public Section(P parent, @NonNull List<C> children, boolean expanded) {
        this.parent = parent;
        this.children = new ArrayList<>(children);
        this.expanded = expanded;
    }
}
