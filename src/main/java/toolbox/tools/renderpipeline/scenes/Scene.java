package toolbox.tools.renderpipeline.scenes;

import toolbox.interfaces.Renderable;
import toolbox.interfaces.ToolBoxDisposable;
import toolbox.tools.shaders.GlobalShader;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import org.lwjgl.opengl.GL40;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scene {

    // Scenes are basically a collection of objects. They are rendered in RenderInstruction classes.
    // TODO add a default shader and render instruction to render the scene.

    private String sceneName;
    private FrameBuffer sceneBuffer; // What to render it to. If null then it's rendered directly to the screen
    private HashMap<String, List<Renderable>> sceneContents;
    private HashMap<String, Object> sceneObjects;


    public Scene(String sceneName, FrameBuffer frameBuffer) {
        this.sceneName = sceneName;
        sceneBuffer = frameBuffer;

        sceneContents = new HashMap<>();
        sceneObjects = new HashMap<>();
    }

    // This adds a renderable under a specific key.
    public void addRenderables(String renderKey, Renderable... renderables) {
        for(Renderable renderable : renderables) {
            if(renderable == null) {
                throw new NullPointerException("A renderable added to " + sceneName + " is null.");
            }
            else if(sceneContents.containsKey(renderKey)) {
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

    public void updateBuffer(FrameBuffer buffer) {
        sceneBuffer = buffer;
    }
    // --------------------



    // --------------------
    // Asset management
    // --------------------
    public Texture getTexture() {
        return sceneBuffer.getColorBufferTexture();
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
        sceneBuffer.begin();
    }

    public void endFrameBuffer() {
        sceneBuffer.end();
    }

    public FrameBuffer getSceneBuffer() {
        return sceneBuffer;
    }

    public void clearScreen() {
        Gdx.gl.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);
    }
    // --------------------



    // --------------------
    // Disposal
    // --------------------
    public void dispose(boolean disposeOfContents) {
        sceneBuffer.dispose();


        if(disposeOfContents) {
            // Loops through contents and disposes of them
            for(Map.Entry<String, List<Renderable>> contentObjects : sceneContents.entrySet()) {

                for(Renderable contentObject : contentObjects.getValue()) {
                    if (contentObject instanceof ToolBoxDisposable) {
                        if(!((ToolBoxDisposable) contentObject).disposedOf()) {
                            ((ToolBoxDisposable) contentObject).dispose();
                        }
                    }
                }

            }
        }


    }


}
