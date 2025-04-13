package iammert.com.expandablelib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class ExpandableLayoutTest {

    private ExpandableLayout layout;
    private final List<String> events = new ArrayList<>();

    @Before
    public void setUp() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        AttributeSet attrs = Robolectric.buildAttributeSet()
                .addAttribute(R.attr.parentLayout, "@layout/expandable_test_parent")
                .addAttribute(R.attr.childLayout, "@layout/expandable_test_child")
                .build();
        layout = new ExpandableLayout(activity, attrs);
        layout.setRenderer(renderer);
        layout.setExpandListener((ExpandCollapseListener.ExpandListener<String>)
                (parentIndex, parent, view) -> events.add("expand " + parent));
        layout.setCollapseListener((ExpandCollapseListener.CollapseListener<String>)
                (parentIndex, parent, view) -> events.add("collapse " + parent));
    }

    private final ExpandableLayout.Renderer<String, String> renderer = new ExpandableLayout.Renderer<String, String>() {
        @Override
        public void renderParent(View view, String model, boolean isExpanded, int parentPosition) {
            ((TextView) view).setText(model + (isExpanded ? " -" : " +"));
        }

        @Override
        public void renderChild(View view, String model, int parentPosition, int childPosition) {
            ((TextView) view).setText(model + "@" + parentPosition + ":" + childPosition);
        }
    };

    private static Section<String, String> section(String parent, boolean expanded, String... children) {
        Section<String, String> section = new Section<>();
        section.parent = parent;
        section.expanded = expanded;
        section.children.addAll(Arrays.asList(children));
        return section;
    }

    private ViewGroup sectionView(int index) {
        return (ViewGroup) layout.getChildAt(index);
    }

    private View parentView(int index) {
        return sectionView(index).getChildAt(0);
    }

    /** Texts of the visible child rows of a section. */
    private List<String> visibleChildren(int index) {
        List<String> texts = new ArrayList<>();
        ViewGroup section = sectionView(index);
        for (int i = 1; i < section.getChildCount(); i++) {
            View child = section.getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                texts.add(((TextView) child).getText().toString());
            }
        }
        return texts;
    }

    @Test
    public void addSection_rendersParentAndExpandedChildren() {
        layout.addSection(section("Fruits", true, "Apple", "Orange"));

        assertEquals(1, layout.getChildCount());
        assertEquals("Fruits -", ((TextView) parentView(0)).getText().toString());
        assertEquals(Arrays.asList("Apple@0:0", "Orange@0:1"), visibleChildren(0));
    }

    @Test
    public void clickingParent_collapsesExpandedSection() {
        layout.addSection(section("Fruits", true, "Apple", "Orange"));

        parentView(0).performClick();

        assertEquals(Arrays.asList(), visibleChildren(0));
        assertEquals(Arrays.asList("collapse Fruits"), events);
    }

    @Test
    public void clickingParentTwice_expandsSectionAgain() {
        layout.addSection(section("Fruits", true, "Apple", "Orange"));

        parentView(0).performClick();
        parentView(0).performClick();

        assertEquals(Arrays.asList("Apple@0:0", "Orange@0:1"), visibleChildren(0));
        assertEquals(Arrays.asList("collapse Fruits", "expand Fruits"), events);
    }

    @Test
    public void filterChildren_hidesNonMatchingChildren() {
        layout.addSection(section("Fruits", true, "Apple", "Orange"));

        layout.filterChildren(obj -> ((String) obj).startsWith("A"));

        assertEquals(Arrays.asList("Apple@0:0"), visibleChildren(0));
    }

    @Test
    public void filterParent_hidesNonMatchingSections() {
        layout.addSection(section("Fruits", true, "Apple"));
        layout.addSection(section("Vegetables", true, "Carrot"));

        layout.filterParent(obj -> obj.equals("Vegetables"));

        assertEquals(View.GONE, sectionView(0).getVisibility());
        assertEquals(View.VISIBLE, sectionView(1).getVisibility());
    }

    @Test
    public void collapsedSection_showsChildrenWhenExpanded() {
        layout.addSection(section("Fruits", false, "Apple", "Orange"));
        assertEquals(Arrays.asList(), visibleChildren(0));

        parentView(0).performClick();

        assertEquals(Arrays.asList("Apple@0:0", "Orange@0:1"), visibleChildren(0));
        assertEquals(Arrays.asList("expand Fruits"), events);
    }

    @Test
    public void addChild_ignoresDuplicates() {
        layout.addSection(section("Fruits", true, "Apple"));

        layout.addChild("Fruits", "Apple");

        assertEquals(1, layout.getSections().get(0).children.size());
        assertEquals(2, sectionView(0).getChildCount());
    }

    @Test
    public void addChildren_onlyAddsMissingChildren() {
        layout.addSection(section("Fruits", true, "Apple"));

        layout.addChildren("Fruits", Arrays.asList("Apple", "Orange", "Orange"));

        assertEquals(Arrays.asList("Apple", "Orange"), layout.getSections().get(0).children);
        assertEquals(3, sectionView(0).getChildCount());

        // Views and data are still in sync, so toggling works.
        parentView(0).performClick();
        parentView(0).performClick();
        assertEquals(2, visibleChildren(0).size());
    }

    @Test
    public void addChildren_passesPositionsAfterExistingChildren() {
        layout.addSection(section("Fruits", true, "Apple"));

        layout.addChildren("Fruits", Arrays.asList("Orange", "Banana"));

        assertEquals(Arrays.asList("Apple@0:0", "Orange@0:1", "Banana@0:2"), visibleChildren(0));
    }

    @Test
    public void addChild_toCollapsedSection_staysHidden() {
        layout.addSection(section("Fruits", false, "Apple"));

        layout.addChild("Fruits", "Orange");
        layout.addChildren("Fruits", Arrays.asList("Banana"));

        assertEquals(Arrays.asList(), visibleChildren(0));
        parentView(0).performClick();
        assertEquals(Arrays.asList("Apple@0:0", "Orange@0:1", "Banana@0:2"), visibleChildren(0));
    }

    @Test
    public void addChild_doesNotNotifyExpandListener() {
        layout.addSection(section("Fruits", true, "Apple"));

        layout.addChild("Fruits", "Orange");
        layout.addChildren("Fruits", Arrays.asList("Banana"));

        assertEquals(Arrays.asList(), events);
        assertEquals(3, visibleChildren(0).size());
    }

    @Test
    public void sectionsAddedBeforeRenderer_areRenderedWhenRendererIsSet() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        layout = new ExpandableLayout(activity, Robolectric.buildAttributeSet()
                .addAttribute(R.attr.parentLayout, "@layout/expandable_test_parent")
                .addAttribute(R.attr.childLayout, "@layout/expandable_test_child")
                .build());

        layout.addSection(section("Fruits", true, "Apple"));
        layout.addSection(section("Vegetables", false, "Carrot"));
        layout.addChild("Fruits", "Orange");
        assertEquals(0, layout.getChildCount());

        layout.setRenderer(renderer);

        assertEquals(2, layout.getChildCount());
        assertEquals("Vegetables +", ((TextView) parentView(1)).getText().toString());
        assertEquals(Arrays.asList("Apple@0:0", "Orange@0:1"), visibleChildren(0));
    }

    @Test
    public void toggling_rerendersParentWithNewState() {
        layout.addSection(section("Fruits", true, "Apple"));

        parentView(0).performClick();
        assertEquals("Fruits +", ((TextView) parentView(0)).getText().toString());

        parentView(0).performClick();
        assertEquals("Fruits -", ((TextView) parentView(0)).getText().toString());
    }

    @Test
    public void filterChildren_keepsCollapsedSectionsCollapsed() {
        layout.addSection(section("Fruits", false, "Apple", "Avocado", "Orange"));

        layout.filterChildren(obj -> ((String) obj).startsWith("A"));
        assertEquals(View.VISIBLE, sectionView(0).getVisibility());
        assertEquals(Arrays.asList(), visibleChildren(0));

        parentView(0).performClick();
        assertEquals(Arrays.asList("Apple@0:0", "Avocado@0:1"), visibleChildren(0));
    }

    @Test
    public void filterChildren_handlesEqualChildren() {
        layout.addSection(section("Fruits", true, "Apple", "Apple", "Orange"));

        layout.filterChildren(obj -> obj.equals("Orange"));

        assertEquals(Arrays.asList("Orange@0:2"), visibleChildren(0));
    }

    @Test
    public void clickingParent_togglesTheClickedSectionWhenParentsAreEqual() {
        layout.addSection(section("Fruits", true, "Apple"));
        layout.addSection(section("Fruits", true, "Orange"));

        parentView(1).performClick();

        assertEquals(Arrays.asList("Apple@0:0"), visibleChildren(0));
        assertEquals(Arrays.asList(), visibleChildren(1));
    }

    @Test
    public void addedChildren_respectCurrentFilter() {
        layout.addSection(section("Fruits", true, "Apple"));
        layout.filterChildren(obj -> ((String) obj).startsWith("A"));

        layout.addChild("Fruits", "Orange");
        layout.addChildren("Fruits", Arrays.asList("Avocado", "Banana"));
        layout.addSection(section("More", true, "Apricot", "Cherry"));

        assertEquals(Arrays.asList("Apple@0:0", "Avocado@0:2"), visibleChildren(0));
        assertEquals(Arrays.asList("Apricot@1:0"), visibleChildren(1));
    }

    @Test
    public void clearFilter_showsEverythingAgain() {
        layout.addSection(section("Fruits", true, "Apple", "Orange"));
        layout.addSection(section("Vegetables", true, "Carrot"));
        layout.filterParent(obj -> obj.equals("Fruits"));
        layout.filterChildren(obj -> obj.equals("Apple"));

        layout.clearFilter();

        assertEquals(View.VISIBLE, sectionView(0).getVisibility());
        assertEquals(View.VISIBLE, sectionView(1).getVisibility());
        assertEquals(Arrays.asList("Apple@0:0", "Orange@0:1"), visibleChildren(0));
    }

    @Test
    public void expandAndCollapse_canBeCalledFromCode() {
        layout.addSection(section("Fruits", false, "Apple"));
        layout.addSection(section("Vegetables", true, "Carrot"));

        layout.expand(0);
        layout.collapse("Vegetables");

        assertEquals(true, layout.isExpanded(0));
        assertEquals(false, layout.isExpanded(1));
        assertEquals(Arrays.asList("Apple@0:0"), visibleChildren(0));
        assertEquals(Arrays.asList(), visibleChildren(1));
        assertEquals(Arrays.asList("expand Fruits", "collapse Vegetables"), events);
    }

    @Test
    public void expand_isNoOpWhenAlreadyExpanded() {
        layout.addSection(section("Fruits", true, "Apple"));

        layout.expand(0);
        layout.collapse(0);
        layout.collapse(0);

        assertEquals(Arrays.asList("collapse Fruits"), events);
    }

    @Test
    public void toggle_switchesState() {
        layout.addSection(section("Fruits", false, "Apple"));

        layout.toggle(0);

        assertEquals(true, layout.isExpanded(0));
    }

    @Test
    public void expandAllAndCollapseAll_changeEverySection() {
        layout.addSection(section("Fruits", false, "Apple"));
        layout.addSection(section("Vegetables", true, "Carrot"));

        layout.expandAll();
        assertEquals(Arrays.asList("Apple@0:0"), visibleChildren(0));
        assertEquals(Arrays.asList("Carrot@1:0"), visibleChildren(1));

        layout.collapseAll();
        assertEquals(Arrays.asList(), visibleChildren(0));
        assertEquals(Arrays.asList(), visibleChildren(1));
        assertEquals(Arrays.asList("expand Fruits", "collapse Fruits", "collapse Vegetables"), events);
    }

    @Test
    public void layoutsCanBeSetFromCode() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        layout = new ExpandableLayout(activity);
        layout.setParentLayout(R.layout.expandable_test_parent);
        layout.setChildLayout(R.layout.expandable_test_child);
        layout.setRenderer(renderer);

        layout.addSection(section("Fruits", true, "Apple"));

        assertEquals(Arrays.asList("Apple@0:0"), visibleChildren(0));
    }

    @Test
    public void missingLayout_failsWithDescriptiveError() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        layout = new ExpandableLayout(activity);
        layout.setRenderer(renderer);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> layout.addSection(section("Fruits", true, "Apple")));
        assertTrue(error.getMessage().contains("app:parentLayout"));
    }

    @Test
    public void notifyParentChanged_rendersParentAgain() {
        Section<String, String> section = section("Fruits", true, "Apple");
        layout.addSection(section);

        section.parent = "Berries";
        layout.notifyParentChanged(0);

        assertEquals("Berries -", ((TextView) parentView(0)).getText().toString());
    }

    @Test
    public void notifyParentChanged_ignoresInvalidPositions() {
        layout.addSection(section("Fruits", true, "Apple"));

        layout.notifyParentChanged(-1);
        layout.notifyParentChanged(1);

        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        ExpandableLayout withoutRenderer = new ExpandableLayout(activity);
        withoutRenderer.addSection(section("Fruits", true, "Apple"));
        withoutRenderer.notifyParentChanged(0);
    }

    @Test
    public void filterChildren_doesNotShowSectionsHiddenByParentFilter() {
        layout.addSection(section("Fruits", true, "Apple"));
        layout.addSection(section("Vegetables", true, "Asparagus"));
        layout.filterParent(obj -> obj.equals("Fruits"));

        layout.filterChildren(obj -> ((String) obj).startsWith("A"));

        assertEquals(View.VISIBLE, sectionView(0).getVisibility());
        assertEquals(View.GONE, sectionView(1).getVisibility());
    }

    @Test
    public void parentFilter_appliesToSectionsAddedLater() {
        layout.filterParent(obj -> obj.equals("Fruits"));

        layout.addSection(section("Fruits", true, "Apple"));
        layout.addSection(section("Vegetables", true, "Carrot"));

        assertEquals(View.VISIBLE, sectionView(0).getVisibility());
        assertEquals(View.GONE, sectionView(1).getVisibility());
    }

    @Test
    public void addingMatchingChild_showsSectionHiddenByChildFilter() {
        layout.addSection(section("Fruits", true, "Orange"));
        layout.filterChildren(obj -> ((String) obj).startsWith("A"));
        assertEquals(View.GONE, sectionView(0).getVisibility());

        layout.addChild("Fruits", "Apple");

        assertEquals(View.VISIBLE, sectionView(0).getVisibility());
        assertEquals(Arrays.asList("Apple@0:1"), visibleChildren(0));
    }

    @Test
    public void removeSection_rendersFollowingSectionsWithNewPositions() {
        layout.addSection(section("Fruits", true, "Apple"));
        layout.addSection(section("Vegetables", true, "Carrot"));
        layout.addSection(section("Nuts", false, "Walnut"));

        layout.removeSection(0);

        assertEquals(2, layout.getSections().size());
        assertEquals(2, layout.getChildCount());
        assertEquals(Arrays.asList("Carrot@0:0"), visibleChildren(0));
        assertEquals("Nuts +", ((TextView) parentView(1)).getText().toString());

        parentView(1).performClick();
        assertEquals(Arrays.asList("Walnut@1:0"), visibleChildren(1));
    }

    @Test
    public void removeChild_removesItsView() {
        layout.addSection(section("Fruits", true, "Apple", "Orange", "Banana"));

        layout.removeChild("Fruits", "Orange");

        assertEquals(Arrays.asList("Apple", "Banana"), layout.getSections().get(0).children);
        assertEquals(Arrays.asList("Apple@0:0", "Banana@0:1"), visibleChildren(0));
    }

    @Test
    public void removeAllSections_clearsLayout() {
        layout.addSection(section("Fruits", true, "Apple"));
        layout.addSection(section("Vegetables", true, "Carrot"));

        layout.removeAllSections();

        assertEquals(0, layout.getSections().size());
        assertEquals(0, layout.getChildCount());
    }

    @Test
    public void notifyChildChanged_rendersChildAgain() {
        Section<String, String> section = section("Fruits", true, "Apple", "Orange");
        layout.addSection(section);

        section.children.set(1, "Mandarin");
        layout.notifyChildChanged(0, 1);
        layout.notifyChildChanged(0, 5);

        assertEquals(Arrays.asList("Apple@0:0", "Mandarin@0:1"), visibleChildren(0));
    }

    @Test
    public void singleExpansion_collapsesOtherSections() {
        layout.setSingleExpansion(true);
        layout.addSection(section("Fruits", true, "Apple"));
        layout.addSection(section("Vegetables", false, "Carrot"));

        parentView(1).performClick();

        assertEquals(false, layout.isExpanded(0));
        assertEquals(true, layout.isExpanded(1));
        assertEquals(Arrays.asList("collapse Fruits", "expand Vegetables"), events);
    }

    @Test
    public void singleExpansion_canBeSetFromXml() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        ExpandableLayout fromXml = new ExpandableLayout(activity, Robolectric.buildAttributeSet()
                .addAttribute(R.attr.singleExpansion, "true")
                .build());

        assertTrue(fromXml.isSingleExpansion());
    }

    private static Parcelable parcel(Parcelable state) {
        Parcel parcel = Parcel.obtain();
        try {
            parcel.writeParcelable(state, 0);
            parcel.setDataPosition(0);
            return parcel.readParcelable(ExpandableLayout.class.getClassLoader());
        } finally {
            parcel.recycle();
        }
    }

    private ExpandableLayout newLayout() {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        ExpandableLayout restored = new ExpandableLayout(activity);
        restored.setParentLayout(R.layout.expandable_test_parent);
        restored.setChildLayout(R.layout.expandable_test_child);
        restored.setRenderer(renderer);
        return restored;
    }

    @Test
    public void expandedStates_areRestoredForExistingSections() {
        layout.addSection(section("Fruits", true, "Apple"));
        layout.addSection(section("Vegetables", false, "Carrot"));
        layout.collapse(0);
        layout.expand(1);
        Parcelable state = parcel(layout.onSaveInstanceState());

        layout = newLayout();
        layout.addSection(section("Fruits", true, "Apple"));
        layout.addSection(section("Vegetables", false, "Carrot"));
        events.clear();
        layout.onRestoreInstanceState(state);

        assertEquals(false, layout.isExpanded(0));
        assertEquals(true, layout.isExpanded(1));
        assertEquals(Arrays.asList(), visibleChildren(0));
        assertEquals(Arrays.asList("Carrot@1:0"), visibleChildren(1));
        assertEquals(Arrays.asList(), events);
    }

    @Test
    public void expandedStates_areAppliedToSectionsAddedAfterRestore() {
        layout.addSection(section("Fruits", true, "Apple"));
        layout.addSection(section("Vegetables", false, "Carrot"));
        layout.collapse(0);
        layout.expand(1);
        Parcelable state = parcel(layout.onSaveInstanceState());

        layout = newLayout();
        layout.onRestoreInstanceState(state);
        layout.addSection(section("Fruits", true, "Apple"));
        layout.addSection(section("Vegetables", false, "Carrot"));
        layout.addSection(section("Nuts", true, "Walnut"));

        assertEquals(false, layout.isExpanded(0));
        assertEquals(true, layout.isExpanded(1));
        assertEquals(true, layout.isExpanded(2));
    }
}
