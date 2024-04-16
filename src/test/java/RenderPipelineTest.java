import toolbox.tools.lighting.LightSource;
import toolbox.tools.meshes.MeshInstance;
import toolbox.tools.meshes.MeshTemplate;
import toolbox.tools.renderpipeline.RenderPipeline;
import toolbox.tools.renderpipeline.scenes.Scene;
import toolbox.tools.shaders.GlobalShader;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector4;
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
    private MeshTemplate meshTemplate;


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
        File file = new File("C:\\Users\\sebas\\IdeaProjects\\toolbox repos\\GDX-toolbox\\src\\main\\resources\\TestTile.obj");

        try {
            meshTemplate = new MeshTemplate(Files.readString(file.toPath()), "Test Mesh");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File file2 = new File("C:\\Users\\sebas\\IdeaProjects\\toolbox repos\\GDX-toolbox\\src\\main\\resources\\DefaultShader\\DefaultVertex.glsl");
        File file3 = new File("C:\\Users\\sebas\\IdeaProjects\\toolbox repos\\GDX-toolbox\\src\\main\\resources\\DefaultShader\\DefaultFragment.glsl");
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

        renderPipeline = new RenderPipeline(fbCamera, batch,
                new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true));
        // renderPipeline.addDisposable(meshTemplate);
        RenderPipeline.globalShaders.put("TestShader", shader);
        // ----------
        Scene scene = new Scene("Main", testFB);
        scene.addObject("camera", camera);

        MeshInstance instance = new MeshInstance(meshTemplate, "Test instance");
        instance.overrideComponent("coord_offset", new Vector4(0f, 0f, 0f, 0f));
        instance.overrideComponent("scale", 0.1f);
        instance.render(shader, GL40.GL_TRIANGLES);
        scene.addRenderables("Test", instance);
        scene.addRenderables("Test mesh", meshTemplate);

        renderPipeline.addScene(scene);
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);

        shader.buildShaderProgram();
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);
        camera.update();

        RenderPipeline.globalShaders.get("TestShader").setUniformMatrix("u_projTrans", camera.combined);

        renderPipeline.renderScene("Main", scene -> {
            Gdx.gl.glClearColor(0f, 1f, 1f, 1f);
            GlobalShader shader = RenderPipeline.globalShaders.get("TestShader");
            Camera camera = (Camera) scene.getObjectByKey("camera");
            camera.update();
            shader.setUniformMatrix("u_projTrans", camera.combined);
            scene.beginFrameBuffer();
            scene.clearScreen();
            scene.render(GL40.GL_TRIANGLES, RenderPipeline.globalShaders.get("TestShader"),
                    scene.getRenderablesByKey("Test"));
            scene.endFrameBuffer();
            Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        }, true);


        renderPipeline.displayLastScene();
        shader.render(GL40.GL_TRIANGLES, meshTemplate);
    }

    @Override
    public void resize(int width, int height) {
        testFB.dispose();
        testFB = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight(), true);

        // renderPipeline.getScene("Main").updateBuffer(testFB);
    }

    @Override
    public void dispose() {
        renderPipeline.dispose();
        System.out.println("Done :)");
    }
}
