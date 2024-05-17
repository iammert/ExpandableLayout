package iammert.com.expandablelib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
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
            layoutInflater = LayoutInflater.from(context);
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
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
        removeAllViews();
        for (int i = 0; i < sections.size(); i++) {
            renderSection(i);
        }
    }

    public void addSection(@NonNull Section section) {
        sections.add(section);
        renderSection(sections.size() - 1);
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

    public void filterParent(@NonNull Operation op) {
        for (Section section : getSections()) {
            final Object parent = section.parent;
            final boolean contains = op.apply(parent);
            getChildAt(sections.indexOf(section)).setVisibility(contains ? View.VISIBLE : View.GONE);
        }
    }


    public void filterChildren(@NonNull Operation op) {
        currentFilter = op;
        for (Section section : getSections()) {
            final List children = section.children;
            boolean keepParentVisible = false;
            final ViewGroup childrenViews = (ViewGroup) getChildAt(sections.indexOf(section));
            for (int i = 0; i < children.size(); i++) {
                final Object child = children.get(i);
                final boolean contains = op.apply(child);
                childrenViews.getChildAt(children.indexOf(child) + 1).setVisibility(contains ? View.VISIBLE : View.GONE);
                if (!keepParentVisible && contains) {
                    keepParentVisible = true;
                }

            }
            childrenViews.setVisibility(keepParentVisible ? View.VISIBLE : View.GONE);
        }
    }

    private <C> void notifyItemAdded(int parentIndex, C child) {
        if (renderer == null) {
            return;
        }
        ViewGroup parentView = (ViewGroup) getChildAt(parentIndex);
        View childView = layoutInflater.inflate(childLayout, parentView, false);
        renderer.renderChild(childView, child, parentIndex, sections.get(parentIndex).children.size() - 1);
        childView.setVisibility(sections.get(parentIndex).expanded ? View.VISIBLE : View.GONE);
        parentView.addView(childView);
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
            childView.setVisibility(sections.get(parentIndex).expanded ? View.VISIBLE : View.GONE);
            parentView.addView(childView);
        }
    }

    private void renderSection(int sectionIndex) {
        if (renderer == null)
            return;

        final Section section = sections.get(sectionIndex);

        LinearLayout sectionLayout = new LinearLayout(getContext());
        sectionLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        sectionLayout.setOrientation(LinearLayout.VERTICAL);

        View parentView = layoutInflater.inflate(parentLayout, sectionLayout, false);
        parentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (section.expanded) {
                    collapse(section.parent);
                } else {
                    expand(section.parent);
                }
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
            childView.setVisibility(section.expanded ? View.VISIBLE : View.GONE);
            sectionLayout.addView(childView);
        }
        addView(sectionLayout);
    }

    public void notifyParentChanged(int position) {
        if (position > getChildCount() - 1) {
            return;
        }
        ViewGroup viewGroup = (ViewGroup) getChildAt(position);
        if (viewGroup != null && viewGroup.getChildCount() > 0) {
            View parentView = viewGroup.getChildAt(0);
            renderer.renderParent(parentView, sections.get(position).parent, sections.get(position).expanded, position);
        }
    }

    private <P> void expand(@NonNull P parent) {
        for (int i = 0; i < sections.size(); i++) {
            if (parent.equals(sections.get(i).parent)) {
                ViewGroup sectionView = ((ViewGroup) getChildAt(i));
                for (int j = 1; j < sectionView.getChildCount(); j++) {
                    final View childView = sectionView.getChildAt(j);
                    final Object childType = sections.get(i).children.get(j - 1);
                    childView.setVisibility(currentFilter.apply(childType) ? View.VISIBLE : View.GONE);
                }
                sections.get(i).expanded = true;
                renderer.renderParent(sectionView.getChildAt(0), sections.get(i).parent, true, i);
                if (expandListener != null)
                    expandListener.onExpanded(i, sections.get(i).parent, sectionView.getChildAt(0));
                break;
            }
        }
    }

    private <P> void collapse(@NonNull P parent) {
        for (int i = 0; i < sections.size(); i++) {
            if (parent.equals(sections.get(i).parent)) {
                ViewGroup sectionView = ((ViewGroup) getChildAt(i));
                sections.get(i).expanded = false;

                for (int j = 1; j < sectionView.getChildCount(); j++) {
                    sectionView.getChildAt(j).setVisibility(View.GONE);
                }
                renderer.renderParent(sectionView.getChildAt(0), sections.get(i).parent, false, i);

                if (collapseListener != null)
                    collapseListener.onCollapsed(i, sections.get(i).parent, sectionView.getChildAt(0));
                break;
            }
        }
    }
}
