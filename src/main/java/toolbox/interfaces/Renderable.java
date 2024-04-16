package toolbox.interfaces;


import toolbox.tools.shaders.GlobalShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public interface Renderable {
    String getID();
    void render(GlobalShader shader, int type); // Renders with a shader.
    void render(ShaderProgram shader, int type); // Renders with a shader.]
}
