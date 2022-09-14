package pl.szmaus.utility;

import org.springframework.stereotype.Component;
import pl.szmaus.configuration.ScheduleConfiguration;
import java.time.LocalDate;
import static java.time.LocalDate.now;

@Component
public class DateUtility {
    private final ScheduleConfiguration scheduleConfiguration;

    public DateUtility(ScheduleConfiguration scheduleConfiguration) {
        this.scheduleConfiguration = scheduleConfiguration;
    }

    public LocalDate dateReminder1Documents(){
        return LocalDate.of(now().getYear(),now().getMonth(),scheduleConfiguration.getReminder1Documents());
    }

    public LocalDate dateReminder2Documents() {
        return LocalDate.of(now().getYear(), now().getMonth(), scheduleConfiguration.getReminder2Documents());
    }

    public LocalDate dateReminder3Documents() {
        return LocalDate.of(now().getYear(), now().getMonth(), scheduleConfiguration.getReminder3Documents());
    }

    public String extractPreviousMonthAndYear() {
        return now().minusMonths(1).toString().substring(0, 7);
    }
}
