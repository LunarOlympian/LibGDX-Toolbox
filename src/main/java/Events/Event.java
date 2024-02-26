package Events;

public class Event {
    // Intended to make repetitive events (Such as rotating something) able to be done easily.
    boolean active = true; // Upon declaration the event is active;
    boolean paused = false;
    public void trigger() {
        // Just so the event automatically ends.
        if(!paused) {
            actions();
        }
    }

    // Triggers an event unless paused.
    public void trigger(Object... arguments) {
        if(!paused) {
            actions(arguments);
        }
    }

    public void actions(Object... arguments) {
        end();
    }

    // Temporary pause.
    public void pause(boolean pauseValue) {
        paused = pauseValue;
    }

    public void end() {
        active = false;
    }

    public boolean getActive() {
        return active;
    }



}