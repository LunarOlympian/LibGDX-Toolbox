package com.github.LunarOlympian.Toolbox.tools.renderpipeline.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.github.LunarOlympian.Toolbox.interfaces.Renderable;
import com.github.LunarOlympian.Toolbox.tools.shaders.GlobalShader;
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


    public Scene(String sceneName, FrameBuffer frameBuffer) {
        this.sceneName = sceneName;
        this.frameBuffer = frameBuffer;

        sceneContents = new HashMap<>();
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

    public List<Renderable> getRenderablesFromKey(String key) {
        if(sceneContents.containsKey(key)) {
            return sceneContents.get(key);
        }
        return new ArrayList<>();
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
        this.frameBuffer.begin();
    }

    public void endFrameBuffer() {
        this.frameBuffer.end();
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
