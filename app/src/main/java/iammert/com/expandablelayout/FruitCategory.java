package iammert.com.expandablelayout;

import android.support.annotation.NonNull;

import iammert.com.expandablelib.Filterable;

/**
 * Created by mertsimsek on 28/07/2017.
 */

public class FruitCategory implements Filterable{
    public String name;

    public FruitCategory(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String getKey() {
        return name;
    }
}
