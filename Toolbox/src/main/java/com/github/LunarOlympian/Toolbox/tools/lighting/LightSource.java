package com.github.LunarOlympian.Toolbox.tools.lighting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.github.LunarOlympian.Toolbox.interfaces.ToolBoxDisposable;
import com.github.LunarOlympian.Toolbox.tools.meshesplus.MeshPlus;
import com.github.LunarOlympian.Toolbox.tools.shaders.GlobalShader;
import org.lwjgl.opengl.GL40;

public class LightSource implements ToolBoxDisposable {
    // Basically a camera. For simplicity, you can add your own cameras.
    private Camera light;
    private Vector2 resolution;

    private GlobalShader shader;

    private FrameBuffer lightBuffer;
    private boolean begun;

    private float brightness;

    public LightSource(Camera light, boolean ortho) {
        this.light = light;
        resolution = new Vector2(light.viewportWidth, light.viewportHeight);
        lightBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        shader = null;
        if(!ortho) {
            shader.setFloat("lightPosition", light.position.x, light.position.y, light.position.z);
        }

        this.brightness = 1f;
    }

    public LightSource(Camera light, Vector2 resolution) {
        this.light = light;
        this.resolution = resolution;
    }



    // --------------------------------------------------
    // Getters and setters!
    // --------------------------------------------------
    // Used to calculate the brightness of the light at x distance.
    // This is the brightness 1 unit away
    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }


    public Texture getShadowMap() {
        // return lightBuffer.getColorBufferTexture();
        return lightBuffer.getColorBufferTexture();
    }

    public Vector2 getResolution() {
        return resolution;
    }

    public Camera getLight() {
        return light;
    }
    // --------------------------------------------------



    // --------------------------------------------------
    // Binding
    // --------------------------------------------------
    public void bindShadowMap() {
        bindShadowMap(0);
    }
    public void bindShadowMap(int location) {
        GL40.glBindTexture(location, lightBuffer.getColorBufferTexture().getTextureObjectHandle());
    }

    // --------------------------------------------------



    // --------------------------------------------------
    // Rendering
    // --------------------------------------------------
    public void begin() {
        lightBuffer.begin();
        Gdx.gl.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);
        begun = true;
    }

    public void render(MeshPlus meshPlus) {
        if(begun) {
            // This renders the light source to a frame buffer.
            shader.setUniformMatrix("proj", light.combined);
            shader.render(GL40.GL_TRIANGLES, meshPlus);
        }
        else {
            throw new RuntimeException("Run LightSource.begin() before rendering with the light source.");
        }
    }

    public void end() {
        lightBuffer.end();
        begun = false;
    }



    // --------------------------------------------------
    // Disposal and rebuilding
    // --------------------------------------------------
    public void dispose() {
        lightBuffer.dispose();
        shader.dispose();
    }

    @Override
    public boolean rebuild() {
        return false; // Light sources are kinda pointless to rebuild.
    }
    // --------------------------------------------------

    // For ortho lights.
    private class Plane {

    }

}
