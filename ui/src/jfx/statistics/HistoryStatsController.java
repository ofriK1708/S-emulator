package jfx.statistics;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class HistoryStatsController
{

    @FXML
    private VBox historyListContainer;

    @FXML
    private VBox historyLogContainer;

    @FXML
    private ScrollPane historyScrollPane;

    @FXML
    public TableColumn<StatRow, String> statNameColumn;

    @FXML
    public TableColumn<StatRow, String> statValueColumn;

    @FXML
    private TableView<StatRow> statisticsTable;

    @FXML
    private VBox statsTableContainer;

    public static class StatRow {
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty value;

        public StatRow(String name, String value) {
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.value = new javafx.beans.property.SimpleStringProperty(value);
        }

        public String getName() {
            return name.get();
        }

        public javafx.beans.property.StringProperty nameProperty() {
            return name;
        }

        public String getValue() {
            return value.get();
        }

        public javafx.beans.property.StringProperty valueProperty() {
            return value;
        }
    }
}
