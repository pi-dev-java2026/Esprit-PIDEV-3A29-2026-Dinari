package org.example.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import org.example.entities.Activite;
import org.example.services.ServiceActivite;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CalendarController {

    @FXML private StackPane calendarContainer;

    private final ServiceActivite serviceActivite = new ServiceActivite();

    @FXML
    public void initialize() {

        CalendarView view = new CalendarView();
        view.showMonthPage();


        Calendar activitesCal = new Calendar("Activités");
        CalendarSource source = new CalendarSource("Budget");
        source.getCalendars().add(activitesCal);
        view.getCalendarSources().add(source);


        loadActivitiesIntoCalendar(activitesCal);

        calendarContainer.getChildren().add(view);
    }

    private void loadActivitiesIntoCalendar(Calendar activitesCal) {
        try {
            List<Activite> list = serviceActivite.afficher();

            for (Activite a : list) {
                if (a.getDateActivite() == null) continue;

                LocalDate d = a.getDateActivite().toLocalDate();

                String title = a.getDescription() + " (" + a.getMontant() + ")";
                Entry<String> entry = new Entry<>(title);

                // événement sur la journée (tu peux mettre une heure fixe)
                entry.changeStartDate(d);
                entry.changeEndDate(d);
                entry.changeStartTime(LocalTime.of(10, 0));
                entry.changeEndTime(LocalTime.of(10, 30));

                activitesCal.addEntry(entry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}