package de.simonschuler.ba.ui;

import android.os.Bundle;
import android.view.Menu;

import de.simonschuler.ba.R;

public class SettingsActivity extends AbstractToolbarController {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.settings);
        setupActionBar();
    }

    /**
     * this is there to make sure every activity sets which menu items are activated
     *
     * @param menu the menu to configure
     */
    @Override
    protected void setupMenuItems(Menu menu) {
        //TODO
    }

    /**
     * this is there to make sure every activity sets its own title
     *
     * @return
     */
    @Override
    protected int getCurrentViewTitleId() {
        return R.string.settings;
    }
}
