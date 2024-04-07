package com.github.LunarOlympian.Toolbox.interfaces;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.github.LunarOlympian.Toolbox.tools.shaders.GlobalShader;

public interface Renderable {
    String getID();
    void render(GlobalShader shader, int type); // Renders with a shader.
    void render(ShaderProgram shader, int type); // Renders with a shader.
}
