package ui.web.jfx.dashboard.users;

import dto.server.UserDTO;
import javafx.beans.property.ListProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.jetbrains.annotations.NotNull;

public class UsersPanelController {

    @FXML
    private TableView<UserDTO> usersTableView;
    @FXML
    private TableColumn<UserDTO, String> usernameColumn;
    @FXML
    private TableColumn<UserDTO, Number> mainProgramsColumn;
    @FXML
    private TableColumn<UserDTO, Number> subFunctionsColumn;
    @FXML
    private TableColumn<UserDTO, Number> currentCreditsColumn;
    @FXML
    private TableColumn<UserDTO, Number> creditsSpentColumn;
    @FXML
    private TableColumn<UserDTO, Number> totalRunsColumn;
    @FXML
    private Button selectUserButton;

    private StringProperty selectedUser;
    private @NotNull String originalUser = "";

    @FXML
    public void initialize() {
        setupTableColumns();
        // Mock data disabled - panel not yet implemented
        // loadMockData();

        System.out.println("UsersPanelController initialized (disabled)");
    }

    private void setupTableColumns() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        mainProgramsColumn.setCellValueFactory(new PropertyValueFactory<>("mainProgramsUploaded"));
        subFunctionsColumn.setCellValueFactory(new PropertyValueFactory<>("subFunctionsContributed"));
        currentCreditsColumn.setCellValueFactory(new PropertyValueFactory<>("currentCredits"));
        creditsSpentColumn.setCellValueFactory(new PropertyValueFactory<>("creditsSpent"));
        totalRunsColumn.setCellValueFactory(new PropertyValueFactory<>("totalRuns"));

    }

    @FXML
    private void handleSelectUser() {
        UserDTO selected = usersTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedUser.set(selected.name());
        }
    }

    public void initComponent(@NotNull ListProperty<UserDTO> usersList,
                              @NotNull StringProperty selectedUser,
                              @NotNull String originalUser) {

        usersTableView.itemsProperty().bind(usersList);
        this.selectedUser = selectedUser;
        this.originalUser = originalUser;

        usersTableView.setRowFactory(tv -> {
            TableRow<UserDTO> row = new TableRow<>() {
                @Override
                protected void updateItem(UserDTO item, boolean empty) {
                    super.updateItem(item, empty);
                }
            };

            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    System.out.println("Row clicked: " + row.getItem());
                    UserDTO user = row.getItem();
                    selectedUser.set(user.name());
                }
            });
            return row;
        });
    }

    public void clearSelection() {
        usersTableView.getSelectionModel().clearSelection();
        selectedUser.set(originalUser);
    }
}