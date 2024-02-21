import Events.Event;
import Events.Trigger;
import org.junit.Test;

public class TestsMain {

    @Test
    public void EventsTest() {
        new Trigger(new testEvent("123"));
    }


    public class testEvent implements Event {

        public testEvent(String test) {
            System.out.println("Test");
        }

        private void testingABitMore() {
            System.out.println("Works!!!!!!");
        }

        @Override
        public void trigger() {
            testingABitMore();
        }
    }
}
