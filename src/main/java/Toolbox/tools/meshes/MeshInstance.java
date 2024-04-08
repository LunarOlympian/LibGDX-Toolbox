package Toolbox.tools.meshes;


import Toolbox.interfaces.Renderable;
import Toolbox.tools.shaders.GlobalShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector4;

import java.util.ArrayList;
import java.util.HashMap;

public class MeshInstance implements Renderable {
    private MeshTemplate mesh;
    private ArrayList<String> overriddenNames;
    private HashMap<String, Object> overriddenComponents;
    private String ID;
    
    public MeshInstance(MeshTemplate mesh, String ID) {
        this.mesh = mesh;
        overriddenComponents = new HashMap<>();
        overriddenNames = new ArrayList<>();
        this.ID = ID;
    }

    public void overrideComponent(String component, Object value) {
        overriddenComponents.put(component, value);

        if(!overriddenNames.contains(component)) {
            overriddenNames.add(component);
        }
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

    public void resetComponent(String key) {
        overriddenNames.remove(key);
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public void render(GlobalShader shader, int type) {
        // This binds all the components to the shader.

        // First checks the default component values and sets those.
        //setComponents(mesh.getComponentNames(), mesh.getAllComponents(), shader);
        // Then it sets the overriden ones
        setComponents(overriddenNames, overriddenComponents, shader);

        this.mesh.render(shader, type);
    }

    @Override
    public void render(ShaderProgram shader, int type) {

    }

    private void setComponents(ArrayList<String> components, HashMap<String, Object> map, GlobalShader shader) {
        for(String component : components) {
            // Checks if the shader contains a uniform that's being set by the mesh. If not moves on
            if(shader.hasUniform(component)) {
                // Now it checks the type of the component
                Object val = map.get(component);
                if(val instanceof Float) {
                    shader.setFloat(component, (Float) val);
                }
                if(val instanceof Vector2) {
                    shader.setFloat(component, ((Vector2) val).x, ((Vector2) val).y);
                }
                if(val instanceof Vector3) {
                    shader.setFloat(component, ((Vector3) val).x, ((Vector3) val).y,
                            ((Vector3) val).z);
                }
                if(val instanceof Vector4) {
                    shader.setFloat(component, ((Vector4) val).x, ((Vector4) val).y,
                            ((Vector4) val).z, ((Vector4) val).w);
                }
            }

        }
    }


    // --------------------
    // Rendering
    // --------------------

}
