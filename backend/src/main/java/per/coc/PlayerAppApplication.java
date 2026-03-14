package per.coc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import per.coc.service.ScheduleWarDataSaver;

@SpringBootApplication
public class PlayerAppApplication {

    public static void main(String[] args) {
        // SpringApplication.run(PlayerAppApplication.class, args);
        ScheduleWarDataSaver.run(args);
    }

}
