package controller.screen.main;

import magit.Magit;

public class TreeController implements IController {
    private Magit model;
    private MainController mainController;

    public TreeController(Magit model, MainController mainController) {
        this.model = model;
        this.mainController = mainController;
    }
}
