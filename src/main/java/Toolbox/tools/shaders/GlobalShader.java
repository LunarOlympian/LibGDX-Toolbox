package Toolbox.tools.shaders;

import Toolbox.interfaces.ToolBoxDisposable;
import Toolbox.tools.meshes.MeshTemplate;
import Toolbox.tools.shaders.ShaderTools.ShaderFunction;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ObjectIntMap;
import org.lwjgl.opengl.GL40;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlobalShader implements ToolBoxDisposable {
    // Goal is to enable conversion between LibGDX and LWJGL shaders. However, I'm practically only gonna
    // use this for geometry shaders :)
    private final int[] shaders = new int[3];
    private HashMap<String, Integer> uniforms;

    private int programID;
    private IntBuffer params;

    // Uniforms
    private String[] uniformNames;
    private IntBuffer type;
    private ObjectIntMap<String> uniformTypes;
    private ObjectIntMap<String> uniformSizes;

    private HashMap<String, Integer> attributeMap;


    private short[] indices;
    private ShortBuffer indexBuffer;

    private int vaoID = -1;

    private int[] shaderTypes = new int[] {
            GL40.GL_VERTEX_SHADER,
            GL40.GL_GEOMETRY_SHADER,
            GL40.GL_FRAGMENT_SHADER
    };

    // Just the string values of the shaders. Useful for editing.
    private String vertexString;
    private String geometryString;
    private String fragmentString;

    private ArrayList<Integer> attributeLocations;

    private String verticesName; // Defaults to a_position



    // -------------------------------
    // Creation of the shader.
    // -------------------------------
    public GlobalShader(String vertex, String fragment, ShaderFunction... functions) {
        makeShader(vertex, null, fragment, functions);
    }
    public GlobalShader(String vertex, String geometry, String fragment, int[] types, ShaderFunction... functions) {
        shaderTypes = types;
        makeShader(vertex, geometry, fragment, functions);
    }

    public GlobalShader(String fragment, ShaderFunction... functions) {
        makeShader(null, null, fragment, functions);
    }
    public GlobalShader(String vertex, String geometry, String fragment, ShaderFunction... functions) {
        makeShader(vertex, geometry, fragment, functions);
    }

    private void makeShader(String vertex, String geometry, String fragment, ShaderFunction... functions) {
        // First creates a program.
        int programID = GL40.glCreateProgram();

        this.vertexString = vertex;
        this.geometryString = geometry;
        this.fragmentString = fragment;

        // Deals with functions
        attachFunctions(functions);

        if(vertexString != null) {
            addShader(shaderTypes[0], vertexString, programID, 0);
        }
        if(geometryString != null) {
            addShader(shaderTypes[1], geometryString, programID, 1);
        }
        addShader(shaderTypes[2], fragmentString, programID, 2);

        this.programID = programID;
        GL40.glLinkProgram(programID);


        this.params = BufferUtils.newIntBuffer(1);
        this.type = BufferUtils.newIntBuffer(1);
        this.uniforms = new HashMap<>();
        this.uniformSizes = new ObjectIntMap<>();
        this.uniformTypes = new ObjectIntMap<>();
        fetchUniforms();

        attributeLocations = new ArrayList<>(); // I don't think this actually does anything?
        setVertexAttributes();
    }
    /*
    Types are determined by GL(version).GL_... which is what is passed in.
    If anyone reads this I recommend GL40+ as that's what I built this using.
    */
    private void addShader(int type, String shader, int programID, int shaderNum) {
        try {
            int ID = GL40.glCreateShader(type);
            GL40.glShaderSource(ID, shader);
            shaders[shaderNum] = ID;

            GL40.glCompileShader(ID);

            if (GL40.glGetShaderi(ID, GL40.GL_COMPILE_STATUS) == 0) {
                String shaderType = "Invalid shader";
                switch(type) {
                    case GL40.GL_VERTEX_SHADER -> shaderType = "Vertex shader";
                    case GL40.GL_GEOMETRY_SHADER -> shaderType = "Geometry shader";
                    case GL40.GL_FRAGMENT_SHADER -> shaderType = "Fragment shader";
                }
                throw new Exception("Error compiling " + shaderType + ":\n" + GL40.glGetShaderInfoLog(ID));
            }

            GL40.glAttachShader(programID, ID);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    // -------------------------------
    // Rendering, disposing, and similar
    // -------------------------------

    // Defaults to triangles
    public void render(MeshTemplate mesh) {
        render(GL40.GL_TRIANGLES, mesh);
    }

    public void render(int renderType, MeshTemplate mesh) {
        // TODO implement ShaderProgram as a render option.
        GL40.glUseProgram(programID);
        // Bind the vertex array and buffer
        GL40.glBindVertexArray(mesh.getVAO());
        GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, mesh.getVBO());
        GL40.glBindBuffer(GL40.GL_ELEMENT_ARRAY_BUFFER, mesh.getEBO());

        // Sets attributes
        String meshAlias = mesh.getAlias().trim();
        GL40.glEnableVertexAttribArray(attributeMap.get(meshAlias));


        GL40.glVertexAttribPointer(attributeMap.get(meshAlias), 3, GL40.GL_FLOAT, false, 0, 0);
        // Here is where it would set the remainder. Alias is special as it's required.
        // Other vertex attributes are NOT declared in the template as that would be a headache.

        // Draws the elements.
        GL40.glDrawElements(renderType, mesh.getIndicesArray().length, GL40.GL_UNSIGNED_SHORT, 0);

        GL40.glDisableVertexAttribArray(0);
        GL40.glBindVertexArray(0);
        GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER,0);
        GL40.glBindBuffer(GL40.GL_ELEMENT_ARRAY_BUFFER, 0);

        GL40.glUseProgram(0);
    }

    public void dispose() {
        GL40.glUseProgram(0);
        GL40.glDeleteProgram(programID);


        for(int x = 0; x < 3; x++) {
            GL40.glDeleteShader(shaders[x]);
        }

        GL40.glDeleteVertexArrays(vaoID); // Theoretically this is never used. Just to be safe though.

        for(Integer attrib : attributeLocations) {
            GL40.glDeleteBuffers(attrib);
        }
    }

    @Override
    public boolean rebuild() {
        return false;
    }

    // -------------------------------
    // Getters and setters
    // -------------------------------

    public int getShaderID() {
        return programID;
    }

    public String getLog() {
        return Gdx.gl20.glGetProgramInfoLog(programID);
    }

    // -------------------------------
    // Uniforms
    // -------------------------------
    public void setUniformMatrix(String name, Matrix4 matrix) {
        // Transpose false for now.
        GL40.glUseProgram(programID);
        GL40.glUniformMatrix4fv(fetchUniform(name), false, matrix.val);
        GL40.glUseProgram(0);
    }

    public void setFloat(String name, float... values) {
        GL40.glUseProgram(programID);
        switch(values.length) {
            case 1:
                GL40.glUniform1f(fetchUniform(name), values[0]);
                break;
            case 2:
                GL40.glUniform2f(fetchUniform(name), values[0], values[1]);
                break;
            case 3:
                GL40.glUniform3f(fetchUniform(name), values[0], values[1], values[2]);
                break;
            case 4:
                GL40.glUniform4f(fetchUniform(name), values[0], values[1], values[2], values[3]);
                break;
            default:
                throw new RuntimeException("Invalid value set to uniform " + name + ". Please make sure the length of " +
                        "floats is between 1 and 4.");
        }
        GL40.glUseProgram(0);
    }

    public void setInt(String name, int value) {
        GL40.glUseProgram(programID);
        GL40.glUniform1i(fetchUniform(name), value);
        GL40.glUseProgram(0);
    }

    public boolean hasUniform(String uniform) {
        if(Arrays.stream(uniformNames).toList().contains(uniform)) {
            return true;
        }
        return false;
    }

    public HashMap<String, Integer> getUniforms() {
        return uniforms;
    }

    // -------------------------------
    // Functions
    // -------------------------------
    private void attachFunctions(ShaderFunction[] functions) {

        // This is kinda clunky but it works
        int functionID = 0;
        for(ShaderFunction function : functions) {
            int attachTo = function.getAttatchTo();
            int spot = 0;
            for(int shaderType : shaderTypes) {
                if(attachTo == shaderType) {
                    if(spot == 0) {
                        vertexString = vertexString.replace("//!Function" + functionID + "!", function.getFunction());
                    }
                    else if(spot == 1) {
                        geometryString = geometryString.replace("//!Function" + functionID + "!", function.getFunction());
                    }
                    else {
                        fragmentString = fragmentString.replace("//!Function" + functionID + "!", function.getFunction());
                    }
                    break; // Exits the loop as it's already been added as a function.
                }
                // Defaults to fragment shader
                else if(attachTo == -1) {
                    if(shaderType == shaderTypes[2]) {
                        fragmentString = fragmentString.replace("//!Function" + functionID + "!", function.getFunction());
                    }
                    break;
                }


                spot++;
            }

            functionID++;
        }


        // This is also pretty clunky, but it's better
        // Now just needs to handle comments with the IDs of the function to add.
        // Generates a regex pattern
        Pattern pattern = Pattern.compile("//\\?\\d+\\?\\s");
        Matcher matcher;

        List<MatchResult> matchResults;

        if(vertexString != null) {
            matcher = pattern.matcher(vertexString);
            matchResults = matcher.results().toList();
            if (!matchResults.isEmpty()) {
                List<String> matches = new ArrayList<>();
                matchResults.forEach(matchResult -> matches.add(matchResult.group()));
                vertexString = ShaderFunction.functionCommentInsertion(vertexString, matches);
            }
        }

        if(geometryString != null) {
            matcher = pattern.matcher(geometryString);
            matchResults = matcher.results().toList();
            if (!matchResults.isEmpty()) {
                List<String> matches = new ArrayList<>();
                matchResults.forEach(matchResult -> matches.add(matchResult.group()));
                geometryString = ShaderFunction.functionCommentInsertion(geometryString, matches);
            }
        }


        matcher = pattern.matcher(fragmentString);
        matchResults = matcher.results().toList();
        if (!matchResults.isEmpty()) {
            List<String> matches = new ArrayList<>();
            matchResults.forEach(matchResult -> matches.add(matchResult.group()));
            fragmentString = ShaderFunction.functionCommentInsertion(fragmentString, matches);
        }
    }

    // --------------------------
    // Tools
    // --------------------------
    private void fetchUniforms() {
        this.params.clear();
        GL40.glGetProgramiv(programID, GL40.GL_ACTIVE_UNIFORMS, this.params);
        int numUniforms = params.get(0);
        this.uniformNames = new String[numUniforms];

        for(int x = 0; x < numUniforms; x++) {
            this.params.clear();
            this.params.put(0, 1);
            this.type.clear();
            String name = GL40.glGetActiveUniform(this.programID, x, this.params, this.type);
            int location = GL40.glGetUniformLocation(this.programID, name);
            this.uniforms.put(name, location);
            this.uniformTypes.put(name, this.type.get(0));
            this.uniformSizes.put(name, this.params.get(0));
            this.uniformNames[x] = name;
        }

    }

    private int fetchUniform(String name) {
        if(!this.uniforms.containsKey(name)) {
            return -1;
        }
        return this.uniforms.get(name);
    }

    private void setVertexAttributes() {
        attributeMap = new HashMap<>();
        GL40.glGetProgramiv(programID, GL40.GL_ACTIVE_ATTRIBUTES, this.params);
        int numAttributes = params.get(0);
        for(int x = 0; x < numAttributes; x++) {
            attributeMap.put(GL40.glGetActiveAttrib(this.programID, x, this.params, this.type),
                    GL40.glGetAttribLocation(programID, GL40.glGetActiveAttrib(this.programID, x, this.params, this.type)));
        }
    }


    @Deprecated
    public void setVertexAttribute(String attributeName, float[] attributeValues, int type, int size) {
        GL40.glLinkProgram(this.programID);
        this.params.clear();
        GL40.glGetProgramiv(programID, GL40.GL_ACTIVE_ATTRIBUTES, this.params);

        int attribLocation = GL40.glGetAttribLocation(programID, attributeName);
        if(vaoID == -1) {
            throw new RuntimeException("Error: Please set the render target before setting attributes.");
        }
        if(attribLocation != -1) {
            // Means it's valid in the program
            GL40.glBindVertexArray(vaoID);

            // Generates a new buffer and binds it
            int vboID = GL40.glGenBuffers();
            GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, vboID);
            FloatBuffer buffer = BufferUtils.newFloatBuffer(attributeValues.length);
            buffer.put(attributeValues);
            buffer.flip();


            attributeLocations.add(vboID);
            GL40.glBufferData(GL40.GL_ARRAY_BUFFER, buffer, GL40.GL_STATIC_DRAW);

            GL40.glEnableVertexAttribArray(attribLocation);
            GL40.glVertexAttribPointer(attribLocation, size, type, false, 0, 0);

            GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, 0);
        }
        else {
            throw new RuntimeException("Error: Attribute " + attributeName + " does not exist in shader.");
        }
    }

}
