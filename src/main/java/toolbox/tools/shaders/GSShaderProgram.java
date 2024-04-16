package toolbox.tools.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class GSShaderProgram extends ShaderProgram {
    public GSShaderProgram() {
        super("", "");

    }

    public void compileShaders(String one, String two) {
        System.out.println(":)");
    }

    @Override
    public String getLog() {
        return "Works!!!!!!!";
    }
}
