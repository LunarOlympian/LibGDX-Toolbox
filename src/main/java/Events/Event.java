package Events;

public class Event {
    // Intended to make repetitive events (Such as rotating something) able to be done easily.
    boolean active = true; // Upon declaration the event is active;
    public void trigger() {
        // Just so the event automatically ends.
        endEvent();
    }

    public void trigger(Object... arguments) {
        endEvent();
    }

    public void endEvent() {
        active = false;
    }

    public boolean getActive() {
        return active;
    }



}