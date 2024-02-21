import Events.Event;
import org.junit.Test;

public class TestsMain {

    @Test
    public void EventsTest() throws InterruptedException {
        TestEvent event = new TestEvent("123");
        event.trigger();

        Thread.sleep(5000);

        event.trigger();
    }


    public class TestEvent extends Event {
        private int runCount = 0;

        public TestEvent(String test) {
            System.out.println("Test");
        }

        private void testingABitMore() {
            System.out.println("Works!!!!!!");
        }

        @Override
        public void trigger() {
            if(runCount == 0) {
                testingABitMore();
                runCount++;
            }
            else {
                System.out.println("h123");
                endEvent();
            }
        }
    }
}
