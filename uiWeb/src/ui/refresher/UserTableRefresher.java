package ui.refresher;

import dto.server.UserDTO;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import system.controller.EngineController;

import java.util.List;
import java.util.TimerTask;

public class UserTableRefresher extends TimerTask {
    private final EngineController httpEngineController;
    private final ListProperty<UserDTO> userInTheUI;
    private List<UserDTO> users;

    public UserTableRefresher(EngineController httpEngineController,
                              ListProperty<UserDTO> userInTheUI) {
        this.httpEngineController = httpEngineController;
        this.userInTheUI = userInTheUI;
    }

    @Override
    public void run() {
        try {
            users = httpEngineController.getAllUsersDTO();
            if (!userInTheUI.get().equals(users)) {
                Platform.runLater(() -> userInTheUI.setAll(users));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
