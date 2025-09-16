package iammert.com.expandablelib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SectionTest {

    @Test
    public void defaultConstructor_createsEmptyCollapsedSection() {
        Section<String, String> section = new Section<>();

        assertFalse(section.expanded);
        assertTrue(section.children.isEmpty());
    }

    @Test
    public void parentConstructor_setsParent() {
        Section<String, String> section = new Section<>("Fruits");

        assertEquals("Fruits", section.parent);
        assertTrue(section.children.isEmpty());
    }

    @Test
    public void childrenConstructor_copiesChildren() {
        List<String> children = new ArrayList<>(Arrays.asList("Apple", "Orange"));
        Section<String, String> section = new Section<>("Fruits", children, true);

        children.add("Banana");

        assertTrue(section.expanded);
        assertEquals(Arrays.asList("Apple", "Orange"), section.children);
    }
}
