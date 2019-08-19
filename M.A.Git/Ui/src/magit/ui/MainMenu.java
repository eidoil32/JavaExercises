package magit.ui;

import settings.Settings;

import java.util.Scanner;

public class MainMenu {
    private static final int menu_size = Settings.MENU_SIZE;

    public static void showMenu(String currentUser) {
        System.out.println(String.format("%s %s", Settings.language.getString("MAGIT_TOP_MASSAGE"), currentUser));
        System.out.println(UIEngine.MAIN_MENU.toString());
    }

    public static eMenuItem getChoice() {
        int integer;
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.print(Settings.language.getString("ENTER_YOUR_COMMAND_NUMBER"));
            integer = Integer.parseInt(scanner.nextLine());
            while (integer < 1 || integer > menu_size) {
                System.out.println(Settings.language.getString("MENU_CHOICE_WRONG"));
                System.out.print(Settings.language.getString("ENTER_YOUR_COMMAND_NUMBER"));
                integer = Integer.parseInt(scanner.nextLine());
            }
        } catch (NumberFormatException e) {
            System.out.println(Settings.language.getString("MENU_CHOICE_NOT_NUMBER"));
            return getChoice();
        }

        return eMenuItem.getItem(integer).get();
    }
}
