package iammert.com.expandablelayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import iammert.com.expandablelib.ExpandableLayout;
import iammert.com.expandablelib.Section;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ExpandableLayout sectionLinearLayout = findViewById(R.id.el);

        sectionLinearLayout.setParentRenderer(new ExpandableLayout.Renderer<FruitCategory>() {
            @Override
            public void render(View view, FruitCategory model, boolean isExpanded) {
                ((TextView) view.findViewById(R.id.tvParent)).setText(model.name);
                view.findViewById(R.id.arrow).setBackgroundResource(isExpanded ? R.drawable.arrow_up : R.drawable.arrow_down);
            }
        });

        sectionLinearLayout.setChildRenderer(new ExpandableLayout.Renderer<Fruit>() {
            @Override
            public void render(View view, Fruit model, boolean isExpanded) {
                ((TextView) view.findViewById(R.id.tvChild)).setText(model.name);
            }
        });

        sectionLinearLayout.addSection(getSection());
        sectionLinearLayout.addSection(getSection());

        sectionLinearLayout.setExpandListener((parentIndex, parent, view) -> {
            view.findViewById(R.id.arrow).setBackgroundResource(R.drawable.arrow_up);
        });

        sectionLinearLayout.setCollapseListener((parentIndex, parent, view) -> {
            view.findViewById(R.id.arrow).setBackgroundResource(R.drawable.arrow_down);
        });

    }

    public Section<FruitCategory, Fruit> getSection() {
        Section<FruitCategory, Fruit> section = new Section<>();
        FruitCategory fruitCategory = new FruitCategory("Fruits");
        Fruit fruit1 = new Fruit("Apple");
        Fruit fruit2 = new Fruit("Orange");
        Fruit fruit3 = new Fruit("Banana");
        Fruit fruit4 = new Fruit("Grape");
        Fruit fruit5 = new Fruit("Strawberry");
        Fruit fruit6 = new Fruit("Cherry");

        section.parent = fruitCategory;
        section.children.add(fruit1);
        section.children.add(fruit2);
        section.children.add(fruit3);
        section.children.add(fruit4);
        section.children.add(fruit5);
        return section;
    }
}
