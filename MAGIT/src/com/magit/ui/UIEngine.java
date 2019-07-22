package com.magit.ui;

import com.magit.engine.Magit;
import com.magit.settings.LangEN;
import com.magit.settings.Settings;

public class UIEngine {
    private final Settings mySettings = new Settings();
    private Magit system = new Magit();

    public void Start()
    {
        boolean validChoice;

        do {
            System.out.println(LangEN.MENU_PLEASE_CHOOSE);
            MainMenu.ShowMenu();
        } while(ExecuteCommand(MainMenu.GetChoice())) ;

        System.out.println(LangEN.END_PROGRAM_MESSAGE);
    }

    private boolean ExecuteCommand(eMenuItem menuItem) {
        return menuItem.executeCommand();
    }

    public Magit getSystem() {
        return system;
    }
}