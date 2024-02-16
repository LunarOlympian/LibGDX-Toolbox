package Shaders;

import Shaders.GeometryShaders.ShaderProgramG;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class GlobalShader {
    // Goal is to implement both geometry and LibGDX shaders in one class.
    private ShaderProgram GDXShader;
    private ShaderProgramG geometryShader;

    public GlobalShader(String[] shaders, boolean geometry) {
        // Checks if it's valid
        if((shaders.length == 2 && !geometry) || (shaders.length == 3 && geometry)) {

        }
        else {
            // Invalid. Throws an exception.
            throw new IllegalArgumentException("Invalid shader input amount.");
        }
    }

    public void setUniforms() {

    }
}
