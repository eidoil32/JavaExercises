package controller.screen.main;

import magit.Magit;

public class CommitsDetailsController implements IController{
    private Magit model;
    private MainController mainController;

    public CommitsDetailsController(Magit model, MainController mainController) {
        this.model = model;
        this.mainController = mainController;
    }
}
