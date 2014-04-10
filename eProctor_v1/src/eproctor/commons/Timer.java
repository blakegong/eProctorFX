package eproctor.commons;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 *
 * @author CLY
 */
public class Timer {

    /**
     *
     * @param count
     * @param heading
     * @param lbl
     * @param eh
     * @return
     */
    public static Timeline produceATimer(Integer count, String heading, Label lbl, EventHandler<ActionEvent> eh) {
        return null;
        
//        Timeline timer =  new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                //count = count - 1;
//                System.out.println("count: " + count);
//                lbl.setText("time to exam: " + intSecToReadableSecond(count, 4));
//            }
//        }));
//        timer.setCycleCount(count);
//        timer.setOnFinished(eh);
//        
//        return timer;
    }

    /**
     *
     * @param t
     * @param level
     * @return
     */
    public static String intSecToReadableSecond(int t, int level) {
        int sec = t % 60;
        int min = (t / 60) % 60;
        int hour = (t / 3600) % 24;
        int day = (t / (3600 * 24)) % 7;
//        int day = (t / (3600 * 24)) % 7;
//        int week = t / (3600 * 24 * 7);

        String sol = "";
        if (level >= 4) {
            sol = sec + " seconds" + sol;
        }

        if (level >= 3 && min != 0) {
            sol = min + " minute " + sol;
        }

        if (level >= 2 && hour != 0) {
            sol = hour + " hour " + sol;
        }

        if (level >= 1 && day != 0) {
            sol = day + " day " + sol;
        }

//        if (level >= 0 && week != 0)
//            sol = week + " week " + sol;
        return sol;
    }
}
