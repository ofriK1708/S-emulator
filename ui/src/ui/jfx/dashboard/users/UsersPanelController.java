package ui.jfx.dashboard.users;

import dto.ui.UserDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Controller for the Users Panel.
 * NOTE: This panel is not yet fully implemented as per requirements.
 * The table structure is defined but functionality is disabled.
 */
public class UsersPanelController {

    @FXML private TableView<UserDTO> usersTableView;
    @FXML private TableColumn<UserDTO, String> usernameColumn;
    @FXML private TableColumn<UserDTO, Number> mainProgramsColumn;
    @FXML private TableColumn<UserDTO, Number> subFunctionsColumn;
    @FXML private TableColumn<UserDTO, Number> currentCreditsColumn;
    @FXML private TableColumn<UserDTO, Number> creditsSpentColumn;
    @FXML private TableColumn<UserDTO, Number> totalRunsColumn;
    @FXML private Button selectUserButton;

    private final ObservableList<UserDTO> usersList = FXCollections.observableArrayList();
    private Consumer<String> selectUserCallback;

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

        usersTableView.setItems(usersList);
    }

    // Mock data method for future implementation
    @SuppressWarnings("unused")
    private void loadMockData() {
        usersList.addAll(
                new UserDTO("Alice", 3, 8, 150, 420, 89),
                new UserDTO("Bob", 2, 5, 200, 310, 67),
                new UserDTO("Charlie", 1, 12, 75, 580, 134)
        );
    }

    @FXML
    private void handleSelectUser() {
        UserDTO selected = usersTableView.getSelectionModel().getSelectedItem();
        if (selected != null && selectUserCallback != null) {
            selectUserCallback.accept(selected.getUsername());
            System.out.println("User selected: " + selected.getUsername());
        }
    }

    public void initComponent(Consumer<String> selectUserCallback) {
        this.selectUserCallback = selectUserCallback;
    }

    public void setUsers(@NotNull ObservableList<UserDTO> users) {
        usersList.setAll(users);
    }

    public void clearUsers() {
        usersList.clear();
    }
}