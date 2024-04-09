package Toolbox.tools.renderpipeline.scenes;

import Toolbox.interfaces.Renderable;
import Toolbox.tools.shaders.GlobalShader;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import org.lwjgl.opengl.GL40;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Scene {

    // Scenes are basically a collection of objects. They are rendered in RenderInstruction classes.
    // TODO add a default shader and render instruction to render the scene.

    private String sceneName;
    private FrameBuffer frameBuffer; // What to render it to. If null then it's rendered directly to the screen
    private HashMap<String, List<Renderable>> sceneContents;
    private HashMap<String, Object> sceneObjects;


    public Scene(String sceneName, FrameBuffer frameBuffer) {
        this.sceneName = sceneName;
        this.frameBuffer = frameBuffer;

        sceneContents = new HashMap<>();
        sceneObjects = new HashMap<>();
    }

    // This adds a renderable under a specific key.
    public void addRenderables(String renderKey, Renderable... renderables) {
        for(Renderable renderable : renderables) {
            if(sceneContents.containsKey(renderKey)) {
                List<Renderable> renderablesKeyList = sceneContents.get(renderKey);
                renderablesKeyList.add(renderable);
                sceneContents.put(renderKey, renderablesKeyList); // Maybe not necessary but probably a good idea.
            }
            else {
                List<Renderable> renderablesKeyList = new ArrayList<>();
                renderablesKeyList.add(renderable);
                sceneContents.put(renderKey, renderablesKeyList);
            }
        }
    }

    public List<Renderable> getRenderablesByKey(String key) {
        if(sceneContents.containsKey(key)) {
            return sceneContents.get(key);
        }
        return new ArrayList<>();
    }

    public void deleteRenderableByKey(String key) {
        sceneContents.remove(key);
    }

    public void addObject(String key, Object object) {
        sceneObjects.put(key, object);
    }

    public Object getObjectByKey(String key) {
        if(!sceneObjects.containsKey(key)) {
            return null;
        }
        return sceneObjects.get(key);
    }

    public void deleteObjectByKey(String key) {
        sceneObjects.remove(key);
    }

    // --------------------
    // Getter and setter hell
    // --------------------
    public String getSceneName() {
        return sceneName;
    }
    // --------------------



    // --------------------
    // Asset management
    // --------------------
    public Texture getTexture() {
        System.out.println(frameBuffer.getColorBufferTexture().getTextureObjectHandle() + " " + sceneName);
        return frameBuffer.getColorBufferTexture();
    }
    // --------------------



    // --------------------
    // Rendering
    // --------------------
    public void render(int primitiveType, GlobalShader shader, List<Renderable> meshes) {
        // Binds, renders, ends
        for(Renderable instance : meshes) {
            instance.render(shader, primitiveType);
        }
    }

    public void beginFrameBuffer() {
        frameBuffer.begin();
    }

    public void endFrameBuffer() {
        frameBuffer.end();
    }

    public FrameBuffer getFrameBuffer() {
        return frameBuffer;
    }

    public void clearScreen() {
        Gdx.gl.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);
    }
    // --------------------



    // --------------------
    // Disposal
    // --------------------
    public void dispose() {

    }


}
