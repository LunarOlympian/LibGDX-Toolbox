package Toolbox.tools.meshesplus;

import Toolbox.interfaces.Renderable;
import Toolbox.interfaces.ToolBoxDisposable;
import Toolbox.tools.groups.Pair;
import Toolbox.tools.meshesplus.meshdata.MeshData;
import Toolbox.tools.meshesplus.meshparts.MeshCore;
import Toolbox.tools.meshesplus.meshparts.MeshIndex;
import Toolbox.tools.meshesplus.meshparts.MeshVertex;
import Toolbox.tools.meshesplus.tools.MeshPlusBuilder;
import Toolbox.tools.meshesplus.tools.MeshPlusTools;
import Toolbox.tools.shaders.GlobalShader;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import org.lwjgl.opengl.GL40;

import java.util.ArrayList;
import java.util.HashMap;

public class MeshPlus implements Renderable, ToolBoxDisposable {

    private MeshPlusTools meshPlusTools = new MeshPlusTools();
    // --------------------------------------------------
    // Variable declaration
    // --------------------------------------------------
    private String template;
    private HashMap<String, GlobalShader> shaders = new HashMap<>();

    // --------------------------------------------------
    // Mesh info
    // --------------------------------------------------
    private MeshData data;
    private HashMap<String, Object> components; // Completely optional and determined by the user.

    // --------------------------------------------------
    // Constructors
    // --------------------------------------------------
    public MeshPlus(String template, String ID) {
        this.template = template;
        Pair<MeshData, HashMap<String, Object>> parserOutput = MeshPlusBuilder.buildFromTemplate(template, ID);
        this.data = parserOutput.getFirst();
        this.components = parserOutput.getSecond();
    }

    public Object getComponent(String component) {
        if(components.containsKey(component)) {
            throw new IllegalArgumentException("Invalid component " + component + ".");
        }
        return components.get(component);
    }
    public HashMap<String, Object> getAllComponents() {
        return components;
    }

    public void updateComponent(String componentName, Object value) {
        // This changes a few things, but actual movement is done in the shader.
        components.put(componentName, value);
    }

    // --------------------------------------------------
    // Mesh creation
    // --------------------------------------------------
    // Deprecated temporarily, will update later
    @Deprecated
    public Mesh toMesh() {
        return null;
        /*
        // Sets the usage
        int usage = switch (VAU.toLowerCase()) {
            case "position" -> VertexAttributes.Usage.Position;
            case "colorunpacked" -> VertexAttributes.Usage.ColorUnpacked;
            default -> 0;
        };

        // Sets the values of the vertices and indices lists.
        // Despite my better judgement it's 4 connections passed to the vertex, each in vec4 format. (4 * 4) + 4 (If component num is 3 then w is just set to 0). So 20
        float[] vertArray = new float[vertices.size() * 4];
        Arrays.fill(vertArray, 0f);
        // Where the loop currently is.
        int vertPos = 0;

        // The offset between the non-clone vertex and the way it's referred to in the template.

        for (MeshVertex vertex : vertices) {
            vertArray[vertPos] = vertex.x;
            vertPos++;
            vertArray[vertPos] = vertex.y;
            vertPos++;
            vertArray[vertPos] = vertex.z;
            vertPos++;

            // vertArray[vertPos] = vertex.w;
            // vertPos++;
        }


        // ------------------------------------------------------------
        // Indices
        short[] indArray = new short[indices.size() * 3];
        int indPos = 0;
        // There are only 3 components. Ever.
        for (MeshIndex index : indices) {
            short[] indexOrder = index.getOrder();
            indArray[indPos] = indexOrder[0];
            indPos++;
            indArray[indPos] = indexOrder[1];
            indPos++;
            indArray[indPos] = indexOrder[2];
            indPos++;
        }
        // ------------------------------------------------------------

        // ------------------------------------------------------------
        // Return

        Mesh returnMe = new Mesh(staticMesh,
                (int) Math.floor(((double) vertices.size() * 3) * flexiblePointCount),
                indices.size() * 3,
                new VertexAttribute(usage,
                        3, alias)
                );
        returnMe
                .setVertices(vertArray)
                .setIndices(indArray);

        return returnMe;
        */
    }


    // ------------------------------
    // Getters and setters
    // ------------------------------

    public MeshCore getMeshCore(double[] optimizedLine, MeshCore defaultCore) {
        // Index the specified core is at. If it's -1 it gets the default core.
        int coreIndex;

        // First checks if it should get the default core.
        if(optimizedLine.length == 3) {
            // It's length 3, meaning it assumes 0 and default core.
            // If default core isn't set it defaults to direction 1.
            coreIndex = -1;
        }
        // Ok this means it is either overridden with a direction or the core is manually set
        else {
            // If it's 5 it gets the specified core. If it's any other length it marks it just returns a null core.
            // -2 marks it to return a null core.
            coreIndex = (optimizedLine.length == 5 ? (int) optimizedLine[4] : -2);
        }

        // Overridden direction. Returns a null core.
        if(coreIndex == -2) {
            return null;
        }

        MeshCore indexCore = null;
        // If it's set to get the default core and the default core is defined it returns the default core.
        if(coreIndex == -1 && defaultCore != null) {
            indexCore = defaultCore;
        }
        // If default core is null this will return a null core.
        return indexCore;
    }


    public ArrayList<MeshVertex> getVertices() {
        return data.vertices();
    }

    public ArrayList<MeshIndex> getIndices() {
        return data.indices();
    }

    public short[] getIndicesArray() {
        short[] indicesArray = new short[data.indices().size() * 3];
        int spot = 0;
        for(MeshIndex index : data.indices()) {
            // TODO: Implement attribute order syncing
            short[] indexVals = index.getOrder();
            indicesArray[spot] = indexVals[0];
            spot++;
            indicesArray[spot] = indexVals[1];
            spot++;
            indicesArray[spot] = indexVals[2];
            spot++;
        }

        return indicesArray;
    }

    public int getVBO() {
        return data.vboID();
    }

    public int getVAO() {
        return data.vaoID();
    }

    public int getEBO() {
        return data.eboID();
    }

    public String getAlias() {
        return data.alias();
    }

    public MeshData getData() {
        return data;
    }


    // --------------------------------------------------
    // Disposal and rebuilding
    // --------------------------------------------------
    public void dispose() {
        GL40.glDeleteBuffers(data.vboID());
        GL40.glDeleteVertexArrays(data.vaoID());
        GL40.glDeleteBuffers(data.eboID());
    }

    @Override
    public boolean rebuild() {
        // TODO implement this.
        return true;
    }


    @Override
    public String getID() {
        return data.ID();
    }
    @Override
    public void render(GlobalShader shader, int type) {
        shader.render(type, this);
    }

    @Override
    public void render(ShaderProgram shader, int type) {

    }

    // --------------------------------------------------
    // Rendering
    // --------------------------------------------------
    // Render ID is used to render certain meshes at certain times.
    // So if you want to apply a static filter to some meshes you'd set the render ID to "static" or something.

    // --------------------------------------------------


}
