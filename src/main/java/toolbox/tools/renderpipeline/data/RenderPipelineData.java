package toolbox.tools.renderpipeline.data;

import toolbox.interfaces.ToolBoxDisposable;
import toolbox.tools.renderpipeline.scenes.Scene;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RenderPipelineData {
    private HashMap<String, Scene> scenes = new HashMap<>(); private ArrayList<Scene> scenesAL = new ArrayList<>(); // Scenes

    private List<ToolBoxDisposable> toolBoxDisposables = new ArrayList<>();
    private List<Disposable> LibGDXDisposables = new ArrayList<>(); // Memory management

    private SpriteBatch batch = null; private OrthographicCamera fbCamera = null; // Where to display the output
    private Texture lastRender; // Info on previous renders.

    public ArrayList<Scene> getScenesAL() {
        return scenesAL;
    }

    public HashMap<String, Scene> getScenes() {
        return scenes;
    }

    public List<ToolBoxDisposable> getToolBoxDisposables() {
        return toolBoxDisposables;
    }

    public void setToolBoxDisposables(List<ToolBoxDisposable> toolBoxDisposables) {
        this.toolBoxDisposables = toolBoxDisposables;
    }

    public List<Disposable> getLibGDXDisposables() {
        return LibGDXDisposables;
    }

    public void setLibGDXDisposables(List<Disposable> libGDXDisposables) {
        LibGDXDisposables = libGDXDisposables;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public void setBatch(SpriteBatch batch) {
        this.batch = batch;
    }

    public OrthographicCamera getFbCamera() {
        return fbCamera;
    }

    public void setFbCamera(OrthographicCamera fbCamera) {
        this.fbCamera = fbCamera;
    }

    public Texture getLastRender() {
        return lastRender;
    }

    public void setLastRender(Texture lastRender) {
        this.lastRender = lastRender;
    }
}
