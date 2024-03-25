import Lighting.LightSource;
import MeshesPlus.MeshPlus;
import Shaders.GlobalShader;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.system.Callback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ShaderTesting extends ApplicationAdapter {

    private Mesh mesh;
    private MeshPlus meshPlus;
    private GlobalShader shader;

    private Camera camera;

    private SpriteBatch batch;

    private LightSource lightSource;

    private OrthographicCamera fbCamera;

    private FrameBuffer testFB;

    private ShaderProgram shaderProgram;

    private TestFrameBuffer testPropFB;

    private Texture testTex;



    @Override
    public void create() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glEnable(GL40.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL40.GL_DEPTH_WRITEMASK);


        meshPlus = new MeshPlus(classPathFile("/DoNotPush/Meshes/Tile_Mesh"));
        this.mesh = meshPlus.toMesh();

        shader = new GlobalShader(classPathFile("/DoNotPush/Shaders/TestVertex.glsl"), classPathFile("/DoNotPush/Shaders/TestFragment.glsl"));

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Vector3 cameraPos = new Vector3(0f, 8f, 0f);
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

        // For rendering 2d images to
        batch = new SpriteBatch();

        fbCamera = new OrthographicCamera();
        fbCamera.update();
        fbCamera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        testFB = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        shaderProgram = new ShaderProgram(classPathFile("/DoNotPush/Shaders/TestVertex.glsl"), classPathFile("/DoNotPush/Shaders/TestFragment.glsl"));

        // texture = new Texture(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);
       // testPropFB = new TestFrameBuffer(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Pixmap pm = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);
        pm.setColor(Color.GREEN);
        pm.fillRectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.testTex = new Texture(new FileHandle("C:\\Users\\sebas\\Downloads\\SachiDesignInsp.jpg"));
        pm.dispose();
    }



    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0.7f, 0.9f, 1);
        Gdx.gl.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);
        camera.update();
        // renderPropShader();
        // testCustomFB();
        // renderGS();
        //renderSP();
        renderImprovedMesh();
    }


    public void renderPropShader() {
        fbCamera.update();
        testFB.begin();
        Gdx.gl.glClearColor(0, 0.7f, 0.9f, 1);
        Gdx.gl.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);
        // renderSP();
        renderGS();
        testFB.end();

        Gdx.gl.glClear(GL40.GL_COLOR_BUFFER_BIT);

        batch.flush();
        batch.setProjectionMatrix(fbCamera.combined);

        batch.begin();
        batch.draw(testFB.getColorBufferTexture(), 0, 0);
        batch.end();

    }

    public void testCustomFB() {
        /*testPropFB.begin();
        //Gdx.gl.glClearColor(0, 0.7f, 0.9f, 1);
        //Gdx.gl.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);
        //Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        testPropFB.end();*/
        fbCamera.update();

        batch.flush();
        batch.setProjectionMatrix(fbCamera.combined);

        batch.begin();
        batch.draw(testFB.getColorBufferTexture(), 300, 300);
        batch.end();
    }

    public void renderGS() {
        shader.setUniformMatrix("proj", camera.combined);
        shader.render(GL40.GL_TRIANGLES);
    }

    public void renderSP() {
        shaderProgram.bind();
        shaderProgram.setUniformMatrix("proj", camera.combined);
        mesh.render(shaderProgram, GL40.GL_TRIANGLES);
    }

    public void renderImprovedMesh() {
        shader.setUniformMatrix("proj", camera.combined);
        shader.renderMesh(GL40.GL_TRIANGLES, meshPlus);
    }

    @Override
    public void dispose() {
        mesh.dispose();
        shader.dispose();
        //testPropFB.dispose();
        testTex.dispose();
        meshPlus.dispose();
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
