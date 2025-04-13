package iammert.com.expandablelib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mertsimsek on 28/07/2017.
 */

public class ExpandableLayout extends LinearLayout {

    public interface Renderer<P, C> {
        void renderParent(View view, P model, boolean isExpanded, int parentPosition);

        void renderChild(View view, C model, int parentPosition, int childPosition);
    }

    private static final int NO_RES = 0;

    private static final int NO_INDEX = -1;

    private static final Operation DEFAULT_FILTER = new Operation() {
        @Override
        public boolean apply(Object obj) {
            return true;
        }
    };

    private LayoutInflater layoutInflater;

    @LayoutRes
    private int parentLayout;

    @LayoutRes
    private int childLayout;

    private Renderer renderer;

    private List<Section> sections;

    private ExpandCollapseListener.ExpandListener expandListener;

    private ExpandCollapseListener.CollapseListener collapseListener;

    private Operation currentFilter = DEFAULT_FILTER;

    private Operation currentParentFilter = DEFAULT_FILTER;

    private boolean singleExpansion;

    /**
     * Restored expanded states for sections that were not added yet
     * when the instance state was restored.
     */
    @Nullable
    private boolean[] pendingExpandedStates;

    public ExpandableLayout(Context context) {
        super(context);
        init(context, null);
    }

    public ExpandableLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ExpandableLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ExpandableLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        setOrientation(VERTICAL);
        sections = new ArrayList<>();
        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ExpandableLayout);
            parentLayout = typedArray.getResourceId(R.styleable.ExpandableLayout_parentLayout, NO_RES);
            childLayout = typedArray.getResourceId(R.styleable.ExpandableLayout_childLayout, NO_RES);
            singleExpansion = typedArray.getBoolean(R.styleable.ExpandableLayout_singleExpansion, false);
            layoutInflater = LayoutInflater.from(context);
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    /**
     * Layout inflated for every parent row, same as {@code app:parentLayout}.
     */
    public void setParentLayout(@LayoutRes int parentLayout) {
        this.parentLayout = parentLayout;
        rerender();
    }

    /**
     * Layout inflated for every child row, same as {@code app:childLayout}.
     */
    public void setChildLayout(@LayoutRes int childLayout) {
        this.childLayout = childLayout;
        rerender();
    }

    /**
     * When enabled, expanding a section collapses every other expanded
     * section (accordion behaviour). Same as {@code app:singleExpansion}.
     */
    public void setSingleExpansion(boolean singleExpansion) {
        this.singleExpansion = singleExpansion;
    }

    public boolean isSingleExpansion() {
        return singleExpansion;
    }

    public <P> void setExpandListener(ExpandCollapseListener.ExpandListener<P> expandListener) {
        this.expandListener = expandListener;
    }

    public <P> void setCollapseListener(ExpandCollapseListener.CollapseListener<P> collapseListener) {
        this.collapseListener = collapseListener;
    }

    /**
     * Sets the renderer and (re)renders every section that was added so far,
     * so sections may be added before or after the renderer is set.
     */
    public void setRenderer(@NonNull Renderer renderer) {
        this.renderer = renderer;
        rerender();
    }

    private void rerender() {
        if (renderer == null) {
            return;
        }
        removeAllViews();
        for (int i = 0; i < sections.size(); i++) {
            renderSection(i);
        }
    }

    public void addSection(@NonNull Section section) {
        sections.add(section);
        int index = sections.size() - 1;
        if (pendingExpandedStates != null) {
            if (index < pendingExpandedStates.length) {
                section.expanded = pendingExpandedStates[index];
            }
            if (index >= pendingExpandedStates.length - 1) {
                pendingExpandedStates = null;
            }
        }
        renderSection(index);
    }

    /**
     * Removes the section at the given position.
     */
    public void removeSection(int sectionIndex) {
        sections.remove(sectionIndex);
        if (renderer == null) {
            return;
        }
        // Sections after the removed one get new positions, render them
        // again so renderers receive up to date positions.
        removeViews(sectionIndex, getChildCount() - sectionIndex);
        for (int i = sectionIndex; i < sections.size(); i++) {
            renderSection(i);
        }
    }

    /**
     * Removes the first section whose parent equals the given one.
     */
    public <P> void removeSection(@NonNull P parent) {
        int index = indexOfParent(parent);
        if (index != NO_INDEX) {
            removeSection(index);
        }
    }

    public void removeAllSections() {
        sections.clear();
        removeAllViews();
    }

    /**
     * Removes a child from the first section whose parent equals the given one.
     */
    public <P, C> void removeChild(@NonNull P parent, C child) {
        int index = indexOfParent(parent);
        if (index == NO_INDEX || !sections.get(index).children.remove(child)) {
            return;
        }
        if (renderer != null) {
            removeViewAt(index);
            renderSection(index);
        }
    }

    public List<Section> getSections() {
        return sections;
    }

    public <P, C> void addChild(P parent, C child) {
        int parentIndex = indexOfParent(parent);
        if (parentIndex == NO_INDEX) {
            return;
        }
        Section section = sections.get(parentIndex);
        if (section.children.contains(child)) {
            return;
        }
        section.children.add(child);
        notifyItemAdded(parentIndex, child);
    }

    public <P, C> void addChildren(P parent, List<C> children) {
        int parentIndex = indexOfParent(parent);
        if (parentIndex == NO_INDEX) {
            return;
        }
        Section section = sections.get(parentIndex);
        // Only add children that are not in the section yet, so the
        // views stay in sync with the section's children list.
        List<C> newChildren = new ArrayList<>();
        for (C child : children) {
            if (!section.children.contains(child) && !newChildren.contains(child)) {
                newChildren.add(child);
            }
        }
        if (newChildren.isEmpty()) {
            return;
        }
        section.children.addAll(newChildren);
        notifyItemAdded(parentIndex, newChildren);
    }

    private int indexOfParent(Object parent) {
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).parent.equals(parent)) {
                return i;
            }
        }
        return NO_INDEX;
    }

    /**
     * Shows only the sections whose parent matches. The filter stays active
     * for sections added later and is combined with {@link #filterChildren}.
     */
    public void filterParent(@NonNull Operation op) {
        currentParentFilter = op;
        for (int i = 0; i < getChildCount(); i++) {
            updateSectionVisibility(i);
        }
    }

    /**
     * Shows only the children that match. Sections without any matching
     * child are hidden. The filter stays active for children added later.
     */
    public void filterChildren(@NonNull Operation op) {
        currentFilter = op;
        for (int i = 0; i < getChildCount(); i++) {
            final Section section = sections.get(i);
            final ViewGroup sectionView = (ViewGroup) getChildAt(i);
            for (int j = 0; j < section.children.size(); j++) {
                // A collapsed section keeps its children hidden, the filter
                // is applied again by expand().
                sectionView.getChildAt(j + 1).setVisibility(
                        isChildVisible(section, section.children.get(j)) ? View.VISIBLE : View.GONE);
            }
            updateSectionVisibility(i);
        }
    }

    private void updateSectionVisibility(int index) {
        getChildAt(index).setVisibility(isSectionVisible(sections.get(index)) ? View.VISIBLE : View.GONE);
    }

    private boolean isSectionVisible(Section section) {
        if (!currentParentFilter.apply(section.parent)) {
            return false;
        }
        if (currentFilter == DEFAULT_FILTER) {
            return true;
        }
        for (Object child : section.children) {
            if (currentFilter.apply(child)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the parent and children filters and shows every section again.
     */
    public void clearFilter() {
        currentParentFilter = DEFAULT_FILTER;
        filterChildren(DEFAULT_FILTER);
    }

    private boolean isChildVisible(Section section, Object child) {
        return section.expanded && currentFilter.apply(child);
    }

    private void checkLayouts() {
        if (parentLayout == NO_RES) {
            throw new IllegalStateException("ExpandableLayout has no parent layout. "
                    + "Set app:parentLayout in XML or call setParentLayout().");
        }
        if (childLayout == NO_RES) {
            throw new IllegalStateException("ExpandableLayout has no child layout. "
                    + "Set app:childLayout in XML or call setChildLayout().");
        }
    }

    private <C> void notifyItemAdded(int parentIndex, C child) {
        if (renderer == null) {
            return;
        }
        ViewGroup parentView = (ViewGroup) getChildAt(parentIndex);
        View childView = layoutInflater.inflate(childLayout, parentView, false);
        renderer.renderChild(childView, child, parentIndex, sections.get(parentIndex).children.size() - 1);
        childView.setVisibility(isChildVisible(sections.get(parentIndex), child) ? View.VISIBLE : View.GONE);
        parentView.addView(childView);
        updateSectionVisibility(parentIndex);
    }

    private <C> void notifyItemAdded(int parentIndex, List<C> children) {
        if (renderer == null) {
            return;
        }
        ViewGroup parentView = (ViewGroup) getChildAt(parentIndex);
        // The children were already appended to the section.
        int firstPosition = sections.get(parentIndex).children.size() - children.size();
        for (int i = 0; i < children.size(); i++) {
            View childView = layoutInflater.inflate(childLayout, parentView, false);
            renderer.renderChild(childView, children.get(i), parentIndex, firstPosition + i);
            childView.setVisibility(isChildVisible(sections.get(parentIndex), children.get(i)) ? View.VISIBLE : View.GONE);
            parentView.addView(childView);
        }
        updateSectionVisibility(parentIndex);
    }

    private void renderSection(int sectionIndex) {
        if (renderer == null)
            return;
        checkLayouts();

        final Section section = sections.get(sectionIndex);

        LinearLayout sectionLayout = new LinearLayout(getContext());
        sectionLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        sectionLayout.setOrientation(LinearLayout.VERTICAL);

        View parentView = layoutInflater.inflate(parentLayout, sectionLayout, false);
        parentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // Look the section up by position, parents are not required
                // to be unique (or to implement equals()).
                toggle(indexOfChild((View) view.getParent()));
            }
        });
        renderer.renderParent(parentView, section.parent, section.expanded, sectionIndex);
        sectionLayout.addView(parentView);

        // Children of collapsed sections are inflated too (but hidden),
        // otherwise expanding the section later has nothing to show.
        for (int i = 0; i < section.children.size(); i++) {
            Object child = section.children.get(i);
            View childView = layoutInflater.inflate(childLayout, sectionLayout, false);
            renderer.renderChild(childView, child, sectionIndex, i);
            childView.setVisibility(isChildVisible(section, child) ? View.VISIBLE : View.GONE);
            sectionLayout.addView(childView);
        }
        sectionLayout.setVisibility(isSectionVisible(section) ? View.VISIBLE : View.GONE);
        addView(sectionLayout, sectionIndex);
    }

    /**
     * Renders the parent row of the section at the given position again,
     * e.g. after its model changed. Invalid positions are ignored.
     */
    public void notifyParentChanged(int position) {
        if (renderer == null || position < 0 || position >= sections.size()
                || position >= getChildCount()) {
            return;
        }
        ViewGroup viewGroup = (ViewGroup) getChildAt(position);
        if (viewGroup != null && viewGroup.getChildCount() > 0) {
            View parentView = viewGroup.getChildAt(0);
            renderer.renderParent(parentView, sections.get(position).parent, sections.get(position).expanded, position);
        }
    }

    /**
     * Renders a child row again, e.g. after its model changed, and
     * re-evaluates the active filter for it. Invalid positions are ignored.
     */
    public void notifyChildChanged(int parentPosition, int childPosition) {
        if (renderer == null || parentPosition < 0 || parentPosition >= sections.size()) {
            return;
        }
        Section section = sections.get(parentPosition);
        if (childPosition < 0 || childPosition >= section.children.size()) {
            return;
        }
        Object child = section.children.get(childPosition);
        View childView = ((ViewGroup) getChildAt(parentPosition)).getChildAt(childPosition + 1);
        renderer.renderChild(childView, child, parentPosition, childPosition);
        childView.setVisibility(isChildVisible(section, child) ? View.VISIBLE : View.GONE);
        updateSectionVisibility(parentPosition);
    }

    /**
     * Expands the first section whose parent equals the given one.
     */
    public <P> void expand(@NonNull P parent) {
        int index = indexOfParent(parent);
        if (index != NO_INDEX) {
            expand(index);
        }
    }

    /**
     * Collapses the first section whose parent equals the given one.
     */
    public <P> void collapse(@NonNull P parent) {
        int index = indexOfParent(parent);
        if (index != NO_INDEX) {
            collapse(index);
        }
    }

    public void expand(int sectionIndex) {
        if (!sections.get(sectionIndex).expanded) {
            expandSection(sectionIndex);
        }
    }

    public void collapse(int sectionIndex) {
        if (sections.get(sectionIndex).expanded) {
            collapseSection(sectionIndex);
        }
    }

    public void toggle(int sectionIndex) {
        if (isExpanded(sectionIndex)) {
            collapseSection(sectionIndex);
        } else {
            expandSection(sectionIndex);
        }
    }

    public void expandAll() {
        for (int i = 0; i < sections.size(); i++) {
            expand(i);
        }
    }

    public void collapseAll() {
        for (int i = 0; i < sections.size(); i++) {
            collapse(i);
        }
    }

    public boolean isExpanded(int sectionIndex) {
        return sections.get(sectionIndex).expanded;
    }

    private void expandSection(int index) {
        if (singleExpansion) {
            for (int i = 0; i < sections.size(); i++) {
                if (i != index && sections.get(i).expanded) {
                    collapseSection(i);
                }
            }
        }
        final Section section = sections.get(index);
        section.expanded = true;
        final ViewGroup sectionView = (ViewGroup) getChildAt(index);
        if (sectionView == null) {
            // Not rendered yet, setRenderer() will use the new state.
            return;
        }
        for (int j = 1; j < sectionView.getChildCount(); j++) {
            final Object child = section.children.get(j - 1);
            sectionView.getChildAt(j).setVisibility(currentFilter.apply(child) ? View.VISIBLE : View.GONE);
        }
        renderer.renderParent(sectionView.getChildAt(0), section.parent, true, index);
        if (expandListener != null)
            expandListener.onExpanded(index, section.parent, sectionView.getChildAt(0));
    }

    private void collapseSection(int index) {
        final Section section = sections.get(index);
        section.expanded = false;
        final ViewGroup sectionView = (ViewGroup) getChildAt(index);
        if (sectionView == null) {
            return;
        }
        for (int j = 1; j < sectionView.getChildCount(); j++) {
            sectionView.getChildAt(j).setVisibility(View.GONE);
        }
        renderer.renderParent(sectionView.getChildAt(0), section.parent, false, index);
        if (collapseListener != null)
            collapseListener.onCollapsed(index, section.parent, sectionView.getChildAt(0));
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.expandedStates = new boolean[sections.size()];
        for (int i = 0; i < sections.size(); i++) {
            state.expandedStates[i] = sections.get(i).expanded;
        }
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        boolean[] expandedStates = savedState.expandedStates;
        // Sections that already exist are updated in place without
        // notifying the listeners, the rest is applied when added.
        for (int i = 0; i < Math.min(expandedStates.length, sections.size()); i++) {
            if (sections.get(i).expanded != expandedStates[i]) {
                sections.get(i).expanded = expandedStates[i];
                if (renderer != null) {
                    removeViewAt(i);
                    renderSection(i);
                }
            }
        }
        pendingExpandedStates = expandedStates.length > sections.size() ? expandedStates : null;
    }

    static class SavedState extends BaseSavedState {

        boolean[] expandedStates;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
            super(source);
            expandedStates = source.createBooleanArray();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeBooleanArray(expandedStates);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
