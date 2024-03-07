package iammert.com.expandablelib;

import static org.junit.Assert.assertEquals;

import android.app.Activity;
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
        layout.setRenderer(new ExpandableLayout.Renderer<String, String>() {
            @Override
            public void renderParent(View view, String model, boolean isExpanded, int parentPosition) {
                ((TextView) view).setText(model + (isExpanded ? " -" : " +"));
            }

            @Override
            public void renderChild(View view, String model, int parentPosition, int childPosition) {
                ((TextView) view).setText(model + "@" + parentPosition + ":" + childPosition);
            }
        });
        layout.setExpandListener((ExpandCollapseListener.ExpandListener<String>)
                (parentIndex, parent, view) -> events.add("expand " + parent));
        layout.setCollapseListener((ExpandCollapseListener.CollapseListener<String>)
                (parentIndex, parent, view) -> events.add("collapse " + parent));
    }

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
}
