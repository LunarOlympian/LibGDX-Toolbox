import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class TestingMain {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        // Sets the title.
        config.setTitle("Light testing");
        // Sets resolution in windowed mode
        config.setWindowedMode(1920, 1080);
        // Sets target FPS.
        config.setForegroundFPS(60);
        // Enables VSync which helps keep the monitor in sync with GPU and prevents screen-tearing
        // Least that's what TomsGuide tells me.
        config.useVsync(true);

        // Creates the game/application. This is window and all that jazz.
        // Setting to an object is unnecessary.
        //new Lwjgl3Application(new RenderPipelineTest(), config);
    }
}
