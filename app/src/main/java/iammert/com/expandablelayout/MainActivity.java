package iammert.com.expandablelayout;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import iammert.com.expandablelib.ExpandableLayout;
import iammert.com.expandablelib.OnChildClickListener;
import iammert.com.expandablelib.Section;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applyWindowInsets();

        ExpandableLayout expandableLayout = findViewById(R.id.el);
        expandableLayout.setRenderer(new ExpandableLayout.Renderer<FruitCategory, Fruit>() {
            @Override
            public void renderParent(View view, FruitCategory model, boolean isExpanded, int parentPosition) {
                ((TextView) view.findViewById(R.id.tvParent)).setText(model.name);
                ((ImageView) view.findViewById(R.id.arrow))
                        .setImageResource(isExpanded ? R.drawable.arrow_up : R.drawable.arrow_down);
            }

            @Override
            public void renderChild(View view, Fruit model, int parentPosition, int childPosition) {
                ((TextView) view.findViewById(R.id.tvChild)).setText(model.name);
            }
        });
        expandableLayout.setOnChildClickListener((OnChildClickListener<FruitCategory, Fruit>)
                (parentIndex, parent, childIndex, child, view) -> Toast.makeText(this,
                        getString(R.string.child_clicked, child.name, parent.name), Toast.LENGTH_SHORT).show());

        expandableLayout.addSections(createSections());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_expand_all) {
                expandableLayout.expandAll();
                return true;
            } else if (item.getItemId() == R.id.action_collapse_all) {
                expandableLayout.collapseAll();
                return true;
            }
            return false;
        });

        EditText editText = findViewById(R.id.edittext);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim().toLowerCase(Locale.ROOT);
                if (query.isEmpty()) {
                    expandableLayout.clearFilter();
                } else {
                    expandableLayout.filterChildren(obj ->
                            ((Fruit) obj).name.toLowerCase(Locale.ROOT).contains(query));
                }
            }
        });
    }

    private void applyWindowInsets() {
        View root = findViewById(R.id.root);
        View scroll = findViewById(R.id.scroll);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(insets.left, insets.top, insets.right, 0);
            scroll.setPadding(0, 0, 0, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private static List<Section<FruitCategory, Fruit>> createSections() {
        List<Section<FruitCategory, Fruit>> sections = new ArrayList<>();
        sections.add(section("Citrus", true, "Orange", "Lemon", "Lime", "Grapefruit", "Mandarin"));
        sections.add(section("Berries", false, "Strawberry", "Blueberry", "Raspberry", "Blackberry"));
        sections.add(section("Stone fruits", false, "Cherry", "Peach", "Plum", "Apricot"));
        sections.add(section("Tropical", false, "Banana", "Mango", "Pineapple", "Papaya", "Coconut"));
        sections.add(section("Pomes", false, "Apple", "Pear", "Quince"));
        sections.add(section("Melons", false, "Watermelon", "Cantaloupe", "Honeydew"));
        return sections;
    }

    private static Section<FruitCategory, Fruit> section(String category, boolean expanded, String... fruits) {
        List<Fruit> children = new ArrayList<>();
        for (String fruit : Arrays.asList(fruits)) {
            children.add(new Fruit(fruit));
        }
        return new Section<>(new FruitCategory(category), children, expanded);
    }
}
