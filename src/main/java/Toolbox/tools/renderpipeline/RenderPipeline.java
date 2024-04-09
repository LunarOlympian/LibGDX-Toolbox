package Toolbox.tools.renderpipeline;

import Toolbox.interfaces.ToolBoxDisposable;
import Toolbox.tools.renderpipeline.data.RenderPipelineData;
import Toolbox.tools.renderpipeline.scenes.PipelineRenderInstructions;
import Toolbox.tools.renderpipeline.scenes.SceneRenderInstructions;
import Toolbox.tools.renderpipeline.scenes.Scene;
import Toolbox.tools.shaders.GlobalShader;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Disposable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RenderPipeline implements ToolBoxDisposable {

    // This is intended to be a big list of all shaders for RenderInstructions to access.
    public static HashMap<String, GlobalShader> globalShaders = new HashMap<>();

    private RenderPipelineData data;

    private FrameBuffer buffer;
    private boolean disposed = false;

    // --------------------
    // Constructors
    // --------------------
    // To not force people to set screen render options here.
    public RenderPipeline(){
        data = new RenderPipelineData();
    }

    // To allow for easy rendering to screen
    public RenderPipeline(OrthographicCamera screenCamera, SpriteBatch batch, FrameBuffer pipelineBuffer) {
        data = new RenderPipelineData();
        data.setFbCamera(screenCamera);
        data.setBatch(batch);
        this.buffer = pipelineBuffer;
    }
    // --------------------



    // --------------------
    // Rendering
    // --------------------
    // Renders a specific scene, but only stuff from a specific ID
    public void renderScene(String name, SceneRenderInstructions instructions, boolean renderDirectlyToScreen) {
        if(data.getScenes().get(name) == null) {
            throw new RuntimeException("Invalid scene name: " + name);
        }
        instructions.render(data.getScenes().get(name));
        data.setLastRender(getSceneTexture(name));
        if(renderDirectlyToScreen) {
            displayLastScene();
        }
    }

    public void renderTextures(boolean directlyToScreen,
                               PipelineRenderInstructions instructions, Texture... textures) {
        instructions.render(buffer, data.getBatch(), textures);

        if(directlyToScreen) {
            data.getBatch().flush();
            data.getBatch().begin();
            data.getBatch().draw(buffer.getColorBufferTexture(), 0, 0);
            data.getBatch().end();
        }
    }

    public void displayLastScene() {
        if(data.getBatch() == null)
            throw new NullPointerException("SpriteBatch and camera haven't been set for this render buffer.");
        else {
            SpriteBatch batch = data.getBatch();
            batch.setProjectionMatrix(data.getFbCamera().combined);
            data.getLastRender().bind();
            batch.flush();

            batch.begin();
            batch.draw(data.getLastRender(), 0, 0);
            batch.end();
        }
    }

    public Texture getSceneTexture(String name) {
        return data.getScenes().get(name).getTexture();
    }
    // --------------------



    // --------------------
    // Scene management
    // --------------------
    public void disposeScene(String ID, boolean disposeOfContents) {
        data.getScenes().get(ID).dispose(disposeOfContents);
        data.getScenes().remove(ID);
    }
    // --------------------



    // --------------------
    // Other disposable management
    // --------------------

    // Adding just allows you to have a nice list of all disposable objects
    public void addDisposable(ToolBoxDisposable disposable) {
        if(disposable == null) {
            throw new IllegalArgumentException("Disposable inputs cannot be null.");
        }
        data.getToolBoxDisposables().add(disposable);
    }
    public void addDisposable(Disposable disposable) {
        data.getLibGDXDisposables().add(disposable);
    }

    // This allows you to dispose of objects without risk of having them be disposed of twice.
    public void disposeObjects(ToolBoxDisposable... disposables) {
        List<ToolBoxDisposable> disposalList = Arrays.stream(disposables).toList();
        for(ToolBoxDisposable toDispose : disposalList) {
            data.getToolBoxDisposables().remove(toDispose);
        }
    }
    public void disposeObjects(Disposable... disposables) {
        List<Disposable> disposalList = Arrays.stream(disposables).toList();
        for(Disposable toDispose : disposalList) {
            data.getLibGDXDisposables().remove(toDispose);
        }
    }
    // --------------------

    public Scene getScene(String name) {
        if(data.getScenes().isEmpty()) {
            return null;
        }
        return data.getScenes().get(name);
    }



    // --------------------
    // Disposal
    // --------------------
    @Override
    public void dispose() {
        for (Scene scene : data.getScenesAL()) {
            scene.dispose(true);
        }

        for(GlobalShader shader : globalShaders.values().stream().toList()) {
            if(!shader.disposedOf()) {
                shader.dispose();
            }
        }

        // Disposes of other things.
        for(ToolBoxDisposable disposable : data.getToolBoxDisposables()) {
            System.out.println(disposable);
            if(!disposable.disposedOf()) {
                disposable.dispose();
            }
        }
        for(Disposable disposable : data.getLibGDXDisposables()) {
            // Sadly disposedOf doesn't work with LibGDX :,(
            disposable.dispose();
        }
        this.buffer.dispose();
        this.disposed = true;
    }

    // This should only be disposed of as the last thing.
    @Override
    public boolean rebuild() {
        return false;
    }

    @Override
    public boolean disposedOf() {
        return disposed;
    }
    // --------------------



    // --------------------
    // Getter and setter hell
    // --------------------
    public void addScene(Scene scene) {
        if(!data.getScenesAL().contains(scene)) {
            data.getScenesAL().add(scene);
        }
        data.getScenes().put(scene.getSceneName(), scene);
    }

    public void removeScene(Scene scene) {
        if(data.getScenesAL().contains(scene)) {
            data.getScenesAL().remove(scene);
            data.getScenes().remove(scene.getSceneName(), scene);
        }
    }

    public void updateSceneBuffer(String name, FrameBuffer buffer) {
        if(data.getScenes().containsKey(name)) {
            this.getScene(name).updateBuffer(buffer);
        }
        else {
            throw new NullPointerException("No scene in render pipeline with name " + name);
        }
    }

    public void setPipelineBuffer(FrameBuffer buffer) {
        this.buffer = buffer;
    }


}
