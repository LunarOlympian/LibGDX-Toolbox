package com.github.LunarOlympian.Toolbox.tools.events;

public class Event {
    // Intended to make repetitive events (Such as rotating something) able to be done easily.
    boolean active = true; // Upon declaration the event is active;
    boolean paused = false;
    public final void trigger() {
        // Just so the event automatically ends.
        if(!paused) {
            actions();
        }
    }

    // Triggers an event unless paused. Final as allowing overriding may cause confusion.
    public final void trigger(Object... arguments) {
        if(!paused) {
            actions(arguments);
        }
    }

    // Goal is for this to be the only method that you should be allowed to override.
    public void actions(Object... arguments) {
        end();
    }

    // Handle pausing. Both of these are final as I think allowing overriding could cause issues.
    // ----------
    public final void pause(boolean pauseValue) {
        paused = pauseValue;
    }

    public final boolean getPaused() {
        return paused;
    }

    public final void togglePause() {
        paused = !paused;
    }
    // ----------



    public final void end() {
        active = false;
    }

    public final boolean getActive() {
        return active;
    }



}