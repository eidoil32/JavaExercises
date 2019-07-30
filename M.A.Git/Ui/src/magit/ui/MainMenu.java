package magit.ui;

import magit.settings.LangEN;
import magit.settings.Settings;

import java.util.Scanner;

public class MainMenu {
    private static final int menu_size = Settings.MENU_SIZE;

    public static void showMenu(String currentUser) {
        System.out.println(String.format("%S %s", LangEN.MAGIT_TOP_MASSAGE, currentUser));
        System.out.println(Settings.MAIN_MENU.toString());
    }

    public static eMenuItem getChoice() {
        int integer;
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.print(LangEN.ENTER_YOUR_COMMAND_NUMBER);
            integer = Integer.parseInt(scanner.nextLine());
            while (integer < 1 || integer > menu_size) {
                System.out.println(LangEN.MENU_CHOICE_WRONG);
                System.out.print(LangEN.ENTER_YOUR_COMMAND_NUMBER);
                integer = Integer.parseInt(scanner.nextLine());
            }
        } catch (NumberFormatException e) {
            System.out.println(LangEN.MENU_CHOICE_NOT_NUMBER);
            return getChoice();
        }

        return Settings.e_menu_options.get(integer - 1);
    }
}
