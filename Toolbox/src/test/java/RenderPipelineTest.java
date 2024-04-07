import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.github.LunarOlympian.Toolbox.tools.lighting.LightSource;
import com.github.LunarOlympian.Toolbox.tools.meshesplus.MeshPlus;
import com.github.LunarOlympian.Toolbox.tools.renderpipeline.RenderPipeline;
import com.github.LunarOlympian.Toolbox.tools.renderpipeline.scenes.RenderInstructions;
import com.github.LunarOlympian.Toolbox.tools.renderpipeline.scenes.Scene;
import com.github.LunarOlympian.Toolbox.tools.shaders.GlobalShader;
import org.lwjgl.opengl.GL40;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class RenderPipelineTest extends ApplicationAdapter {

    private RenderPipeline renderPipeline;
    private Camera camera;
    private GlobalShader shader;
    private FrameBuffer testFB;
    private Camera light;
    private LightSource lightSource;
    //private Texture testTex;

    private OrthographicCamera fbCamera;
    private SpriteBatch batch;
    private MeshPlus meshPlus;


    @Override
    public void create() {
        ShaderProgram.pedantic = false;
        Gdx.gl.glEnable(GL40.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL40.GL_DEPTH_WRITEMASK);

        batch = new SpriteBatch();


        fbCamera = new OrthographicCamera();
        fbCamera.update();
        this.fbCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setProjectionMatrix(fbCamera.combined);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        testFB = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        File file = new File("C:\\Users\\sebas\\IdeaProjects\\MurderousIntent\\Toolbox\\src\\main\\resources\\TestTile.obj");

        try {
            meshPlus = new MeshPlus(Files.readString(file.toPath()), "Test Mesh");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File file2 = new File("C:\\Users\\sebas\\IdeaProjects\\MurderousIntent\\Toolbox\\src\\main\\resources\\DefaultShader\\DefaultVertex.glsl");
        File file3 = new File("C:\\Users\\sebas\\IdeaProjects\\MurderousIntent\\Toolbox\\src\\main\\resources\\DefaultShader\\DefaultFragment.glsl");
        try {
            shader = new GlobalShader(Files.readString(file2.toPath()),
                    Files.readString(file3.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Vector3 cameraPos = new Vector3(0f, 5f, 0f);
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

        OrthographicCamera fbCamera = new OrthographicCamera();
        fbCamera.update();
        fbCamera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        fbCamera.update();
        batch.setProjectionMatrix(fbCamera.combined);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        testFB = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);


        light = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        light.position.set(new Vector3(0, 5, 0));
        light.lookAt(0, 0, 0);
        // Sets camera render info
        light.near = 0.5f;
        light.far = 300f;
        light.update();
        // lightSource = new LightSource(light, false);




        renderPipeline = new RenderPipeline(fbCamera, batch);
        renderPipeline.addDisposable(meshPlus);
        RenderPipeline.globalShaders.put("TestShader", shader);

        Scene scene = new Scene("Main", testFB);
        scene.addRenderables("Test", meshPlus);
        renderPipeline.addDisposable(testFB);
        // renderPipeline.addDisposable(lightSource);
        renderPipeline.addScene(scene);
        //renderPipeline.addDisposable(testTex);
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);
        camera.update();
        RenderPipeline.globalShaders.get("TestShader").setUniformMatrix("u_projTrans", camera.combined);
        renderPipeline.renderScene("Main", new testInstructions(), true);
        renderPipeline.displayLastScene();
        shader.render(GL40.GL_TRIANGLES, meshPlus);
    }

    @Override
    public void dispose() {
        testFB.dispose();
        //testTex.dispose();
        renderPipeline.dispose();
    }

    private class testInstructions implements RenderInstructions {
        @Override
        public void render(Scene scene) {

            Gdx.gl.glClearColor(0f, 1f, 1f, 1f);
            GlobalShader shader = RenderPipeline.globalShaders.get("TestShader");
            camera.update();
            shader.setUniformMatrix("u_projTrans", camera.combined);
            scene.beginFrameBuffer();
            scene.clearScreen();
            scene.render(GL40.GL_TRIANGLES, RenderPipeline.globalShaders.get("TestShader"),
                    scene.getRenderablesFromKey("Test"));
            scene.endFrameBuffer();
            Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        }
    }
}
