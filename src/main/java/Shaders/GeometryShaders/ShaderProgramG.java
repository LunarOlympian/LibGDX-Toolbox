package Shaders.GeometryShaders;

import MeshesPlus.MeshConstructor;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ObjectIntMap;
import org.lwjgl.opengl.GL40;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.util.ArrayList;

public class ShaderProgramG {
    // Goal is to enable conversion between LibGDX and LWJGL shaders. However, I'm practically only gonna
    // use this for geometry shaders :)
    private final int[] shaders = new int[3];
    private ObjectIntMap<String> uniforms;

    private int programID;
    private IntBuffer params;

    // Uniforms
    private String[] uniformNames;
    private IntBuffer type;
    private ObjectIntMap<String> uniformTypes;
    private ObjectIntMap<String> uniformSizes;


    private short[] indices;
    private ShortBuffer indexBuffer;

    private int vaoID = -1;
    private int vboID;
    private int eboID;

    private int[] shaderTypes = new int[] {
            GL40.GL_VERTEX_SHADER,
            GL40.GL_GEOMETRY_SHADER,
            GL40.GL_FRAGMENT_SHADER
    };

    private ArrayList<Integer> attributeLocations;




    public ShaderProgramG(File vertex, File geometry, File fragment, int[] types) {
        shaderTypes = types;
        makeShader(vertex, geometry, fragment);
    }
    public ShaderProgramG(File vertex, File geometry, File fragment) {
        makeShader(vertex, geometry, fragment);
    }

    private void makeShader(File vertex, File geometry, File fragment) {
        // First creates a program.
        int programID = GL40.glCreateProgram();

        // Temporarily geometry is ignored to enable easier testing.
        addShader(shaderTypes[0], vertex, programID, 0);
        addShader(shaderTypes[1], geometry, programID, 1);
        addShader(shaderTypes[2], fragment, programID, 2);

        GL40.glLinkProgram(programID);
        this.programID = programID;

        this.params = BufferUtils.newIntBuffer(1);
        this.type = BufferUtils.newIntBuffer(1);
        this.uniforms = new ObjectIntMap<>();
        this.uniformSizes = new ObjectIntMap<>();
        this.uniformTypes = new ObjectIntMap<>();
        fetchUniforms();

        attributeLocations = new ArrayList<>();

    }

    /*
    Types are determined by GL(version).GL_... which is what is passed in.
    */
    private void addShader(int type, File path, int programID, int shaderNum) {
        try {
            int ID = GL40.glCreateShader(type);
            try {
                GL40.glShaderSource(ID, Files.readString(path.toPath()));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
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

    public void setRenderTarget(MeshConstructor mesh) {
        setRenderTarget(mesh.getVertices(), mesh.getIndices());
    }

    public void setRenderTarget(float[] vertices, short[] indices) {
        // Sets the vertex array
        this.vaoID = GL40.glGenVertexArrays();
        GL40.glBindVertexArray(vaoID);
        // Creates a vertex buffer object
        this.vboID = GL40.glGenBuffers();
        GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, vboID);
        GL40.glBufferData(GL40.GL_ARRAY_BUFFER, vertices, GL40.GL_STATIC_DRAW);

        // Sets a_position
        int aPos  = GL40.glGetAttribLocation(programID, "a_position");
        GL40.glEnableVertexAttribArray(aPos);
        GL40.glVertexAttribPointer(aPos, 3, GL40.GL_FLOAT, false, 0, 0);

        // Sets the indices
        GL40.glLinkProgram(programID);
        this.indices = indices;

        this.eboID = GL40.glGenBuffers();
        GL40.glBindBuffer(GL40.GL_ELEMENT_ARRAY_BUFFER, this.eboID);
        this.indexBuffer = BufferUtils.newShortBuffer(this.indices.length);
        this.indexBuffer.flip();
        GL40.glBufferData(GL40.GL_ELEMENT_ARRAY_BUFFER, this.indices, GL40.GL_STATIC_DRAW);

        GL40.glUseProgram(0);
        GL40.glEnableVertexAttribArray(0);
        GL40.glBindVertexArray(0);
    }

    public void render() {
        render(GL40.GL_TRIANGLES);
    }
    public void render(int renderType) {
        GL40.glUseProgram(programID);
        GL40.glBindVertexArray(this.vaoID);
        GL40.glEnableVertexAttribArray(0);


        GL40.glDrawElements(renderType, this.indices.length, GL40.GL_UNSIGNED_SHORT, 0);

        GL40.glDisableVertexAttribArray(0);
        GL40.glBindVertexArray(0);
        GL40.glUseProgram(0);
    }

    // -------------------------------
    // Getters and setters
    // -------------------------------

    public int getShaderID() {
        return programID;
    }

    // Used for camera primarily
    public void setUniformMatrix(String name, Matrix4 matrix) {
        // Transpose false for now.
        GL40.glUseProgram(programID);
        GL40.glUniformMatrix4fv(fetchUniform(name), false, matrix.val);
        GL40.glUseProgram(0);
    }

    public void dispose() {
        GL40.glUseProgram(0);
        for(int x = 0; x < 3; x++) {
            GL40.glDeleteShader(shaders[x]);
        }
        GL40.glDeleteVertexArrays(vaoID);
        GL40.glDeleteBuffers(vboID);
        indexBuffer.clear();
        GL40.glDeleteBuffers(eboID);

        for(Integer attrib : attributeLocations) {
            GL40.glDeleteBuffers(attrib);
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
        return this.uniforms.get(name, -1);
    }

    public void setVertexAttribute(String attributeName, float[] attributeValues, int type, int size) {
        GL40.glLinkProgram(this.programID);
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
