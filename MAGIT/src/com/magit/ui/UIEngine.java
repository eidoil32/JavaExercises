package com.magit.ui;

import com.magit.engine.Magit;
import com.magit.engine.eTypeOfString;
import com.magit.exceptions.RepositoryException;
import com.magit.settings.LangEN;
import com.magit.settings.Settings;

import java.io.IOException;
import java.util.Scanner;

public class UIEngine {
    private final Settings mySettings = new Settings();
    private Magit system = new Magit();

    public void start() {
        try{
            do {
                MainMenu.showMenu();
            } while(executeCommand(MainMenu.getChoice())) ;
        } catch (IOException e)
        {
            System.out.println(LangEN.COMMAND_FAILED + e.getMessage());
            start();
        }

        System.out.println(LangEN.END_PROGRAM_MESSAGE);
    }

    private boolean executeCommand(eMenuItem menuItem) throws IOException {
        try {
            String results = menuItem.executeCommand();
            if(results != null)
                System.out.println(results);
            else
                return false;
        } catch (RepositoryException e) {
            System.out.println(String.format("%s%s",LangEN.ERROR_REPOSITORY,e.getMessage()));
            System.out.println(LangEN.TRY_AGAIN_OR_CHOOSE_OTHER_COMMAND);
            String userChoice = new Scanner(System.in).nextLine();
            if(userChoice.equals("Y") || userChoice.toLowerCase().equals("yes"))
                executeCommand(menuItem);
        }

        return true;
    }

    public Magit getSystem() {
        return system;
    }

    public String askUserForString(String message, eTypeOfString type)
    {
        System.out.println(message);
        String userAnswer = new Scanner(System.in).nextLine();
        return type.valid(userAnswer) ? userAnswer : null;
    }
}