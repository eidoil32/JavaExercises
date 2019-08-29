package controller.screen.main;

import magit.Magit;

public class OpenedChangesController implements IController {
    private Magit model;
    private MainController mainController;

    public OpenedChangesController(Magit model, MainController mainController) {
        this.model = model;
        this.mainController = mainController;
    }
}
