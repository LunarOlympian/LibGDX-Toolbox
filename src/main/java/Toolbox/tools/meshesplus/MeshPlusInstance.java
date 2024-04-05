package Toolbox.tools.meshesplus;


import Toolbox.interfaces.Renderable;
import Toolbox.tools.shaders.GlobalShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.util.HashMap;

public class MeshPlusInstance implements Renderable {
    private MeshPlus mesh;
    private HashMap<String, Object> overriddenComponents;
    private String ID;
    
    public MeshPlusInstance(MeshPlus mesh, String ID) {
        this.mesh = mesh;
        overriddenComponents = new HashMap<>();
    }

    public void overrideComponent(String component, Object value) {
        overriddenComponents.put(component, value);
    }

    public Object getComponent(String component) {
        if(overriddenComponents.containsKey(component)) {
            return overriddenComponents.get(component);
        }
        else if(mesh.getAllComponents().containsKey(component)) {
            return mesh.getComponent(component);
        }
        throw new IllegalArgumentException("Invalid component " + component + ".");
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public void render(GlobalShader shader, int type) {
        // This binds all the components to the shader.

        // First checks overridenComponents and sets those.
        // Then it sets the defaults.
        // TODO add this
        this.mesh.render(shader, type);
    }

    @Override
    public void render(ShaderProgram shader, int type) {

    }


    // --------------------
    // Rendering
    // --------------------

}
