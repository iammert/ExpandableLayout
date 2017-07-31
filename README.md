# ExpandableLayout
Expandable LinearLayout
<img src="https://raw.githubusercontent.com/iammert/ExpandableLayout/master/art/ell.png"/>

# Setup

## Set renderers
```java
expandableLayout.setParentRenderer(new ExpandableLayout.Renderer<FruitCategory>() {
    @Override
    public void render(View view, FruitCategory model, boolean isExpanded) {
        ((TextView) view.findViewById(R.id.name)).setText(model.name);
    }
});

expandableLayout.setChildRenderer(new ExpandableLayout.Renderer<Fruit>() {
    @Override
    public void render(View view, Fruit model, boolean isExpanded) {
        ((TextView) view.findViewById(R.id.name)).setText(model.name);
    }
});
```
## Set listeners
```java
expandableLayout.setExpandListener(new ExpandCollapseListener.ExpandListener<FruitCategory>() {
    @Override
    public void onExpanded(int parentIndex, FruitCategory parent, View view) {
        //Layout expanded 
    }
});

expandableLayout.setCollapseListener(new ExpandCollapseListener.CollapseListener<FruitCategory>() {
    @Override
    public void onCollapsed(int parentIndex, FruitCategory parent, View view) {
        //Layout collapsed
    }
});
```
## Add section or children
```java
Section<FruitCategory, Fruit> section = new Section<>();

FruitCategory fruitCategory = new FruitCategory("Fruits");
Fruit fruit1 = new Fruit("Apple");
Fruit fruit2 = new Fruit("Orange");

section.parent = fruitCategory;
section.children.add(fruit1);
section.children.add(fruit2);

expandableLayout.addSection(section);
expandableLayout.addChild(fruitCategory, new Fruit("Grape"));
```
