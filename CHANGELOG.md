# Changelog

## 2.0.0

### Breaking changes
- minSdk is now 21.
- Adding children no longer calls the expand listener.
- Parent rows are rendered again on every expand/collapse.
- Sections without any child matching `filterChildren()` stay hidden even when the parent filter matches, and sections hidden by `filterParent()` are no longer shown by `filterChildren()`.

### New
- Public `expand()`, `collapse()`, `toggle()`, `isExpanded()`, `expandAll()` and `collapseAll()`.
- `removeSection()`, `removeChild()`, `removeAllSections()` and `addSections()`.
- `notifyChildChanged()` and `notifyDataSetChanged()`.
- `clearFilter()`; filters now apply to items added later.
- `OnChildClickListener`.
- `app:singleExpansion`, `app:animateExpansion` and `app:toggleOnParentClick`.
- `setParentLayout()` / `setChildLayout()` for layouts created from code.
- Expanded states are saved and restored.
- Accessibility: state description and expand/collapse actions on parent rows.
- `Section` convenience constructors.

### Fixes
- Sections added collapsed showed nothing when expanded.
- Children added to a collapsed section were visible.
- Duplicate children created extra views and broke expand/filter.
- Wrong child positions in `addChildren()`.
- Sections added before `setRenderer()` were never shown.
- Filtering and toggling picked the wrong views for equal models.
- Collapsed sections opened while filtering.
- Layout params of the root of item layouts were ignored.
- Crashes in `notifyParentChanged()` and with null parents.
- Missing layout attributes now fail with a descriptive message.

### Build
- Android Gradle Plugin 9.4.0, Gradle 9.6.1, compile/target SDK 36.
- Published with `maven-publish`; JitPack builds with JDK 17.
- Robolectric unit tests and GitHub Actions CI.

## 1.4.1
- Last release before 2.0.0.
