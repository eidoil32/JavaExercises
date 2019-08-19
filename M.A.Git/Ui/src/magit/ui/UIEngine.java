package magit.ui;

import exceptions.MyFileException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import magit.Magit;
import settings.Settings;

import java.io.IOException;
import java.util.Scanner;

public class UIEngine {
    private Magit system;
    public static StringBuilder MAIN_MENU = new StringBuilder();

    public UIEngine() {
        system = new Magit();
        for (int i = 1; i <= Settings.MENU_SIZE; i++) {
            String itemName = eMenuItem.getItem(i).get().getName();
            MAIN_MENU.append(String.format("%d - %s", i, itemName)).append(System.lineSeparator());
        }
    }

    public void start() {
        String currentUser;
        try {
            do {
                currentUser = system.getCurrentUser();
                MainMenu.showMenu(currentUser);
            } while (executeCommand(MainMenu.getChoice(), currentUser));
        } catch (IOException e) {
            System.out.println(Settings.language.getString("COMMAND_FAILED") + e.getMessage());
            start();
        }

        System.out.println(Settings.language.getString("END_PROGRAM_MESSAGE"));
    }

    private boolean executeCommand(eMenuItem menuItem, String currentUser) throws IOException {
        try {
            if (system.getCurrentRepository() != null || menuItem.isAllow()) {
                String results = menuItem.executeCommand(currentUser, system);
                if (results != null)
                    System.out.println(results);
                else
                    return false;
            } else {
                System.out.println(Settings.language.getString("NO_ACTIVE_REPOSITORY"));
            }
        } catch (RepositoryException e) {
            if (e.getCode() == eErrorCodes.DO_CHECKOUT) {
                return executeCommand(eMenuItem.getItem(Settings.MENU_ITEM_CHECK_OUT).get(), currentUser);
            } else {
                return tryAgain(menuItem, currentUser,e.getCode().getMessage());
            }
        } catch (MyFileException e) {
            return tryAgain(menuItem, currentUser,e.getCode().getMessage());
        }

        return true;
    }

    private boolean tryAgain(eMenuItem menuItem, String currentUser, String message) throws IOException {
        System.out.println(String.format("%s%s", Settings.language.getString("ERROR_REPOSITORY"), message));
        System.out.println(Settings.language.getString("ERROR_REPOSITORY"));
        String userChoice = new Scanner(System.in).nextLine();
        if (userChoice.equals("Y") || userChoice.toLowerCase().equals("yes"))
            return executeCommand(menuItem, currentUser);
        return false;
    }
}