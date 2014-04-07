package eproctor_v1;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author CLY
 */
public class Timer {

    
    
    
    
    
    
    
    
    public static class smallTimer extends Service<Void> {
        long ms = -1;
        int s = -1;
        String sRead = "";
        
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Thread.sleep(1000);
                    return null;
                }
            };
        }
    }
}
