package Lighting;

import MeshesPlus.MeshPlus;
import Shaders.GlobalShader;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL40;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class LightSource {
    // Basically a camera. For simplicity, you can add your own cameras.
    private Camera light;
    private Vector2 resolution;

    private GlobalShader shader;

    private FrameBuffer lightBuffer;
    private ArrayList<MeshPlus> objects;

    public LightSource(Camera light) {
        this.light = light;
        resolution = new Vector2(light.viewportWidth, light.viewportHeight);
        lightBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        shader = new GlobalShader(classPathFile("/Assets/Shaders/ShadowMapShader/SMSVertex.glsl"), classPathFile("/Assets/Shaders/ShadowMapShader/SMSFragment.glsl"));
    }

    public LightSource(Camera light, Vector2 resolution) {
        this.light = light;
        this.resolution = resolution;
    }

    // Adds an object to be rendered
    public void addObject(MeshPlus mesh) {
        shader.setRenderTarget(mesh);
    }

    public void begin() {
        lightBuffer.begin();
        Gdx.gl.glClearColor(0.5f, 0.5f, 1, 1);
        Gdx.gl.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(1, 1, 1, 1);
    }

    public void end() {
        lightBuffer.end();
    }

    public Texture getShadowMap() {
        return lightBuffer.getColorBufferTexture();
    }

    public void dispose() {
        lightBuffer.dispose();
        shader.dispose();
    }

    private String classPathFile(String path) {
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
