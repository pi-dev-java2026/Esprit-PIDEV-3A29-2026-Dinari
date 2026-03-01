package controllers;

import entities.LogEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedReader;
import java.io.FileReader;

public class HistoriqueController {

    @FXML
    private TableView<LogEntry> tableLog;

    @FXML
    private TableColumn<LogEntry, String> dateCol;

    @FXML
    private TableColumn<LogEntry, String> actionCol;

    @FXML
    private TableColumn<LogEntry, String> entityCol;

    @FXML
    private TableColumn<LogEntry, String> detailsCol;

    private final String LOG_FILE = "logs/app-log.txt";

    @FXML
    public void initialize() {

        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
        entityCol.setCellValueFactory(new PropertyValueFactory<>("entity"));
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));

        loadLogs();
    }


    private void loadLogs() {

        ObservableList<LogEntry> list = FXCollections.observableArrayList();

        try (BufferedReader br = new BufferedReader(new FileReader(LOG_FILE))) {

            String line;

            while ((line = br.readLine()) != null) {

                String[] parts = line.split("\\|");

                if (parts.length < 4) continue;

                LogEntry log = new LogEntry();

                log.setDate(parts[0].trim());

                log.setAction(parts[1].replace("ACTION=", "").trim());

                log.setEntity(parts[2].replace("ENTITY=", "").trim());

                // ⭐ Reconstruction correcte des DETAILS
                StringBuilder details = new StringBuilder();

                for (int i = 3; i < parts.length; i++) {
                    details.append(parts[i]).append(" | ");
                }

                log.setDetails(details.toString().trim());

                list.add(log);
            }

            tableLog.setItems(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
