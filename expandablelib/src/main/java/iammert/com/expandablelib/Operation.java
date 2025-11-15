package iammert.com.expandablelib;

/**
 * Predicate used by {@link ExpandableLayout#filterParent(Operation)} and
 * {@link ExpandableLayout#filterChildren(Operation)}.
 */
@FunctionalInterface
public interface Operation {

    /**
     * @param obj a parent or child model
     * @return true to keep the item visible
     */
    boolean apply(Object obj);
}
