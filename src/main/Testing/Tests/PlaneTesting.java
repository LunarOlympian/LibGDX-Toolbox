import MeshesPlus.MeshPlus;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL40;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PlaneTesting extends ApplicationAdapter {

    private Camera camera;

    private Mesh mesh;

    private ShaderProgram testShader;

    @Override
    public void create() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glEnable(GL40.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL40.GL_DEPTH_WRITEMASK);

        MeshPlus meshPlus = new MeshPlus(classPathFile("/DoNotPush/Meshes/Plane_Mesh"));
        this.mesh = meshPlus.toMesh();

        this.testShader = new ShaderProgram(classPathFile("/DoNotPush/Shaders/TestVertex.glsl"),
                classPathFile("/DoNotPush/Shaders/TestFragment.glsl"));

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Vector3 cameraPos = new Vector3(0f, 3f, 0f);
        this.camera.position.set(new Vector3(cameraPos));
        this.camera.lookAt(0, 0, 0); // Set at this weird point as that allows it to keep everything in frame
        // Sets camera render info
        this.camera.near = 0.5f;
        this.camera.far = 300f;
        // Sets camera movement/input info
        CameraInputController camController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(camController);
        // Updates camera
        this.camera.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Gdx.gl.glEnable(GL20.GL_CULL_FACE);


    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_COLOR_BUFFER_BIT);

        testShader.bind();
        testShader.setUniformMatrix("proj", camera.combined);

        mesh.render(testShader, GL40.GL_TRIANGLES);
    }

    @Override
    public void dispose() {
        mesh.dispose();
        testShader.dispose();
    }

    public String classPathFile(String path) {
        try {
            // Comment below is just telling IntelliJ to stop warning me that it might be null.
            //noinspection DataFlowIssue
            return IOUtils.toString(this.getClass().getResourceAsStream(path), StandardCharsets.UTF_8);
        }
        catch(IOException e) {
            // Same thing as above. Basically telling IntelliJ to shut it.
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return "";
        }
    }

}
