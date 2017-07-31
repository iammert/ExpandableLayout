# ExpandableLayout
Expandable LinearLayout
<img src="https://raw.githubusercontent.com/iammert/ExpandableLayout/master/art/ell.png"/>

# Setup
## Dependency
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    compile 'com.github.iammert:ExpandableLayout:1.1'
}
```
## Layout
```xml
<iammert.com.expandablelib.ExpandableLayout
    android:id="@+id/el"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:parentLayout="@layout/layout_parent"
    app:childLayout="@layout/layout_child"/>
```
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
License
--------


    Copyright 2017 Mert Şimşek.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.






