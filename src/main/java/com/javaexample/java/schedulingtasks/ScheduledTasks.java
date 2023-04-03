package com.javaexample.java.schedulingtasks;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    //Example patterns:
    //
    //"0 0 * * * *" = the top of every hour of every day.
    //"*/10 * * * * *" = every ten seconds.
    //"0 0 8-10 * * *" = 8, 9 and 10 o'clock of every day.
    //"0 0 6,19 * * *" = 6:00 AM and 7:00 PM every day.
    //"0 0/30 8-10 * * *" = 8:00, 8:30, 9:00, 9:30, 10:00 and 10:30 every day.
    //"0 0 9-17 * * MON-FRI" = on the hour nine-to-five weekdays
    //"0 0 0 25 12 ?" = every Christmas Day at midnight
    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
        log.info("The time is now {}", dateFormat.format(new Date()));
    }
}