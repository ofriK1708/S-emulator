package ui.dashboard.users;

import dto.server.UserDTO;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.jetbrains.annotations.NotNull;

public class UsersPanelController {

    @FXML
    private TableView<UserDTO> usersTableView;
    @FXML
    private TableColumn<UserDTO, String> usernameColumn;
    @FXML
    private TableColumn<UserDTO, Number> mainProgramsUploadedColumn;
    @FXML
    private TableColumn<UserDTO, Number> subFunctionsUploadedColumn;
    @FXML
    private TableColumn<UserDTO, Number> currentCreditsColumn;
    @FXML
    private TableColumn<UserDTO, Number> creditsSpentColumn;
    @FXML
    private TableColumn<UserDTO, Number> totalRunsColumn;
    @FXML
    private Button clearChoice;

    private @NotNull Runnable onClearSelectionCallback = () -> {
    };
    private StringProperty selectedUser;

    @FXML
    public void initialize() {
        setupTableColumns();
        clearChoice.disableProperty().bind(usersTableView.getSelectionModel().selectedItemProperty().isNull());
        System.out.println("UsersPanelController initialized (disabled)");
    }

    private void setupTableColumns() {
        usernameColumn.setCellValueFactory(callData ->
                new SimpleStringProperty(callData.getValue().name()));

        mainProgramsUploadedColumn.setCellValueFactory(callData ->
                new ReadOnlyIntegerWrapper(callData.getValue().mainProgramsUploaded()));

        subFunctionsUploadedColumn.setCellValueFactory(callData ->
                new ReadOnlyIntegerWrapper(callData.getValue().subFunctionsContributed()));

        currentCreditsColumn.setCellValueFactory(callData ->
                new ReadOnlyIntegerWrapper(callData.getValue().currentCredits()));

        creditsSpentColumn.setCellValueFactory(callData ->
                new ReadOnlyIntegerWrapper(callData.getValue().creditSpend()));

        totalRunsColumn.setCellValueFactory(callData ->
                new ReadOnlyIntegerWrapper(callData.getValue().totalRuns()));

    }

    @FXML
    private void handleSelectUser() {
        UserDTO selected = usersTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedUser.set(selected.name());
        }
    }

    public void initComponent(@NotNull ListProperty<UserDTO> usersList,
                              @NotNull Runnable onClearSelectionCallback,
                              @NotNull StringProperty selectedUser) {

        usersTableView.itemsProperty().bind(usersList);
        this.selectedUser = selectedUser;
        this.onClearSelectionCallback = onClearSelectionCallback;

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
        onClearSelectionCallback.run();
    }
}