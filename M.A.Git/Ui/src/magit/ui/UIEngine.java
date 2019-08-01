package magit.ui;

import exceptions.MyFileException;
import exceptions.RepositoryException;
import magit.Magit;
import magit.settings.LangEN;
import magit.settings.Settings;

import java.io.IOException;
import java.util.Scanner;

public class UIEngine {
    private final Settings mySettings = new Settings();
    private Magit system = new Magit();

    public void start() {
        String currentUser;
        try {
            do {
                currentUser = system.getCurrentUser();
                MainMenu.showMenu(currentUser);
            } while (executeCommand(MainMenu.getChoice(), currentUser));
        } catch (IOException e) {
            System.out.println(LangEN.COMMAND_FAILED + e.getMessage());
            start();
        }

        System.out.println(LangEN.END_PROGRAM_MESSAGE);
    }

    private boolean executeCommand(eMenuItem menuItem, String currentUser) throws IOException {
        try {
            if (system.getCurrentRepository() != null || menuItem.isAllow()) {
                String results = menuItem.executeCommand(currentUser,system);
                if (results != null)
                    System.out.println(results);
                else
                    return false;
            } else {
                System.out.println(LangEN.NO_ACTIVE_REPOSITORY);
            }
        } catch (RepositoryException | MyFileException e) {
            System.out.println(String.format("%s%s", LangEN.ERROR_REPOSITORY, e.getMessage()));
            System.out.println(LangEN.TRY_AGAIN_OR_CHOOSE_OTHER_COMMAND);
            String userChoice = new Scanner(System.in).nextLine();
            if (userChoice.equals("Y") || userChoice.toLowerCase().equals("yes"))
                executeCommand(menuItem, currentUser);
        }

        return true;
    }
}