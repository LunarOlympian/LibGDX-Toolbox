package com.github.LunarOlympian.Toolbox.tools.renderpipeline;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.github.LunarOlympian.Toolbox.interfaces.ToolBoxDisposable;
import com.github.LunarOlympian.Toolbox.tools.renderpipeline.data.RenderPipelineData;
import com.github.LunarOlympian.Toolbox.tools.renderpipeline.scenes.RenderInstructions;
import com.github.LunarOlympian.Toolbox.tools.renderpipeline.scenes.Scene;
import com.github.LunarOlympian.Toolbox.tools.shaders.GlobalShader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RenderPipeline implements ToolBoxDisposable {

    // This is intended to be a big list of all shaders for RenderInstructions to access.
    public static HashMap<String, GlobalShader> globalShaders = new HashMap<>();

    private RenderPipelineData data;

    // --------------------
    // Constructors
    // --------------------
    // To not force people to set screen render options here.
    public RenderPipeline(){
        data = new RenderPipelineData();
    }

    // To allow for easy rendering to screen
    public RenderPipeline(OrthographicCamera screenCamera, SpriteBatch batch) {
        data = new RenderPipelineData();
        data.setFbCamera(screenCamera);
        data.setBatch(batch);
    }
    // --------------------



    // --------------------
    // Rendering
    // --------------------
    // Renders a specific scene, but only stuff from a specific ID
    public void renderScene(String name, RenderInstructions instructions, boolean renderDirectlyToScreen) {
        if(data.getScenes().get(name) == null) {
            throw new RuntimeException("Invalid scene name: " + name);
        }
        instructions.render(data.getScenes().get(name));
        data.setLastRender(getSceneTexture(name));
        if(renderDirectlyToScreen) {
            displayLastScene();
        }
    }

    public void displayLastScene() {
        if(data.getBatch() == null)
            throw new NullPointerException("SpriteBatch and camera haven't been set for this render buffer.");
        else {
            SpriteBatch batch = data.getBatch();
            batch.setProjectionMatrix(data.getFbCamera().combined);

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
    // --------------------]
    public void disposeScene(String ID) {
        data.getScenes().get(ID).dispose();
        data.getScenes().remove(ID);
    }
    // --------------------



    // --------------------
    // Other disposable management
    // --------------------

    // Adding just allows you to have a nice list of all disposable objects
    public void addDisposable(ToolBoxDisposable disposable) {
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



    // --------------------
    // Disposal
    // --------------------
    @Override
    public void dispose() {
        for(Scene scene : data.getScenesAL()) {
            scene.dispose();
        }

        // Disposes of other things.
        for(ToolBoxDisposable disposable : data.getToolBoxDisposables()) {
            disposable.dispose();
        }
        for(Disposable disposable : data.getLibGDXDisposables()) {
            disposable.dispose();
        }
    }

    // This should only be disposed of as the last thing.
    @Override
    public boolean rebuild() {
        return false;
    }
    // --------------------



    // --------------------
    // Getter and setter hell
    // --------------------
    public void addScene(Scene scene) {
        data.getScenes().put(scene.getSceneName(), scene);
        data.getScenesAL().add(scene);
    }
}
