package iammert.com.expandablelib;

/**
 * Created by mertsimsek on 27/11/2017.
 */

class FilterManager {

    private boolean enableLowerCaseFiltering;

    public FilterManager(boolean enableLowerCaseFiltering) {
        this.enableLowerCaseFiltering = enableLowerCaseFiltering;
    }

    public boolean contains(String string, String substring) {
        if (enableLowerCaseFiltering) {
            return string.toLowerCase().contains(substring.toLowerCase());
        } else {
            return string.contains(substring);
        }
    }

    public void setEnableLowerCaseFiltering(boolean enableLowerCaseFiltering) {
        this.enableLowerCaseFiltering = enableLowerCaseFiltering;
    }
}
