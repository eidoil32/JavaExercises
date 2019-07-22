package com.magit.ui;

import com.magit.settings.LangEN;
import com.magit.settings.Settings;

import java.util.Scanner;

public class MainMenu {
    private static final int menu_size = Settings.MENU_SIZE;

    public static void ShowMenu() {
        for (int i = 0; i < menu_size; i++) {
            System.out.println(String.format("%d - %s", i + 1, Settings.menu_options.get(i)));
        }
    }

    public static eMenuItem GetChoice() {
        int integer;
        Scanner scanner = new Scanner(System.in);
        try {
            integer = Integer.parseInt(scanner.nextLine());
            while (integer < 1 || integer > menu_size) {
                System.out.println(LangEN.MENU_CHOICE_WRONG);
                integer = Integer.parseInt(scanner.nextLine());
            }
        } catch (NumberFormatException e) {
            System.out.println(LangEN.MENU_CHOICE_NOT_NUMBER);
            return GetChoice();
        }

        return Settings.e_menu_options.get(integer - 1);
    }
}
