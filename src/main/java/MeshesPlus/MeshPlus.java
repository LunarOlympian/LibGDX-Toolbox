package MeshesPlus;

import MeshesPlus.MeshParts.MeshCore;
import MeshesPlus.MeshParts.MeshIndex;
import MeshesPlus.MeshParts.MeshVertex;
import MeshesPlus.MeshParts.TextureConstructor;
import MeshesPlus.tools.MeshPlusTools;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.BufferUtils;
import org.lwjgl.opengl.GL40;

import java.io.File;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MeshPlus {

    private MeshPlusTools meshPlusTools = new MeshPlusTools();
    // --------------------------------------------------
    // Variable declaration
    // --------------------------------------------------
    private String template;

    // -------------------------
    // Variables from template
    // -------------------------
    private String alias = "a_position";
    // Vertex attribute usage
    private String VAU = "Position";
    private int componentNum = 3;
    private boolean staticMesh = true;

    // Multiplies point count by factor specified
    private double flexiblePointCount = 1;

    // Used to enable a mode which properly builds the vertex attribute needed for showing connections between lines.
    // Disabling should increase rendering speed.
    private boolean connectedVerticesAtt = true;

    // Null by default
    private MeshCore defaultCore = null;

    private ArrayList<MeshVertex> vertices = new ArrayList<>();
    private ArrayList<MeshIndex> indices = new ArrayList<>();
    private ArrayList<MeshCore> cores = new ArrayList<>();

    // --------------------
    // Texture stuff
    // --------------------

    // Only if the texture is set.
    private Texture texture;
    // Used to store potential textures. To reduce memory load textures are stored as files until explicitly called.
    private ArrayList<File> texturePaths = new ArrayList<>();


    // -------------------------
    // Variables from setters
    // -------------------------
    private MeshVertex meshPosition = new MeshVertex(0f, 0f, 0f); // Maybe update to a vector3

    // -------------------------
    // Buffer info
    // -------------------------
    private int vaoID = -1;
    private int vboID;
    private int eboID;

    private ShortBuffer indexBuffer;


    // --------------------------------------------------
    // Constructor
    // --------------------------------------------------
    public MeshPlus(File template) {
        if(!template.getName().endsWith("_Mesh")) {
            // Accesses contents
            for(File file : Objects.requireNonNull(template.listFiles())) {
                String fileName = file.getName();
                if(fileName.endsWith("_Mesh")) {
                    // This is the mesh template.
                    try {
                        this.template = Files.readString(file.toPath());
                    } catch(IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                else if(fileName.endsWith("_Texture")) {
                    this.texturePaths.add(file);
                }
            }
        }
        else {
            try {
                this.template = Files.readString(template.toPath());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        makeMesh();
    }

    public MeshPlus(String template) {
        this.template = template;
        makeMesh();
    }
    public void setMeshPos(float x, float y, float z) {
        meshPosition = new MeshVertex(x, y, z);
    }

    public Texture getTexture() {
        return texture;
    }

    // Rebuilds the indices with a new camera position. Used to decide which side of indices to render.
    // Unfinished.

    // Possibly add in rotation.

    // --------------------------------------------------
    // Makes the mesh
    // --------------------------------------------------
    private void makeMesh() {
        try {
            String template = this.template;


            if (!template.contains("\n")) {
                // Probably a file. Interpret it as such.
                this.template = Files.readString(new File(template).toPath());
                template = this.template;
            }
            /*
            Simple formatting is the goal here.

            First line declares name of the mesh.
            Second declares Vertex attribute
            !vert declares vertices until the next !
            !ind declares Indices until the next !
            // Declares a comment
            */

            /*
            Splits it up into lines. Sets the mode for what it's
            looking for.
            -1. Prevents any form of swapping.
            0. Default mode. Nothing is set and any mode can be swapped to.
            1. Alias
            2. Vertex attribute usage (Think that's what this is...?)
            3. Number of components
            4. Static
            5. Flexible point count (multiplier)
            6. connected_vertices attribute

            Below here is lists
            7. Vertices
            8. Indices
            9. Cores
            */


            int mode = 1;
            int lineCount = 0;
            for (String line : template.split("\n")) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("//")) {
                    // Quick little edit to allow for comments at the end of lines ^-^
                    line = line.split("//")[0].trim();

                    // Doesn't ignore. Uses mode to fill in the blanks.

                    if (line.startsWith("!")) {
                        // Has a ! in front so it changes the mode.
                        mode = updateMode(line, mode);
                    }
                    else if(line.startsWith("?")) {
                        // Sets the proper mode. -1 prevents any interference.
                        if(line.trim().equalsIgnoreCase("?")) {
                            mode = 0;
                        }
                        else {
                            mode = -1;
                        }
                        builders(line, lineCount);
                    }
                    else if(line.startsWith(".")) {
                        setters(line);
                    }
                    else {
                        String[] optimizedLineString = line.trim().replace(" ", "")
                                // Allows for separation of vertex IDs and direction/core info.
                                .replace("|", "")
                                .split(",");
                        // Chose double as it can be transformed into float or int.
                        double[] optimizedLine = new double[optimizedLineString.length];
                        if (mode == 7 || mode == 8 || mode == 9) {
                            Arrays.setAll(optimizedLine, i -> Double.parseDouble(optimizedLineString[i]));
                        }
                        // Long ass switch statement for all modes. They are currently kept in separate variables, but
                        // you can make it a list if that works better later.
                        switch (mode) {
                            case 1:
                                // Alias
                                alias = line;
                                mode++;
                                break;

                            case 2:
                                // Vertex attribute usage
                                VAU = line;
                                mode++;
                                break;

                            case 3:
                                // Component count
                                componentNum = Integer.parseInt(line);
                                mode++;
                                break;

                            case 4:
                                // Static
                                staticMesh = Boolean.parseBoolean(line);
                                mode++;
                                break;

                            case 5:
                                // Increases the max points and verticies
                                flexiblePointCount = Double.parseDouble(line);
                                // Set to 0 to force inclusion of !vert or !ind
                                mode = 6;
                                break;

                            case 6:
                                connectedVerticesAtt = Boolean.parseBoolean(line);
                                mode = 0;
                                break;


                            /*
                            Adds them separately.
                            So you do NOT need to treat it like a list.
                            Each line is a set of verticies points or an indices.
                            Formatted like x, y, z
                            */
                            case 7:
                                // Extraction is whatever. I prefer this.
                                MeshVertex vert = null;
                                if(componentNum == 3) {
                                    vert = new MeshVertex(
                                                    (float) optimizedLine[0],
                                                    (float) optimizedLine[1],
                                                    (float) optimizedLine[2],
                                                    vertices.size() // ID
                                            );
                                }
                                else if(componentNum == 4) {
                                    vert = new MeshVertex(
                                                    (float) optimizedLine[0],
                                                    (float) optimizedLine[1],
                                                    (float) optimizedLine[2],
                                                    (float) optimizedLine[3],
                                                    vertices.size() // ID
                                            );
                                }

                                vertices.add(vert);
                                break;

                            case 8:
                                /*
                                So you have 3 options:
                                1. You do specify a direction (1 or -1), in which case it overrides everything and uses that direction.
                                2. You specify adaptive direction (or don't specify anything). In this case it defaults to
                                the default core. If that doesn't exist it defaults to positive.
                                3. You specify adaptive direction and a core, in which case it uses that core.
                                */

                                // Direction defaults to 0
                                MeshCore indexCore = getMeshCore(optimizedLine);
                                if(indexCore == null) {
                                    // Declares with a set direction.
                                    int setDirection;
                                    // Checks conditions that make it default to positive.
                                    if(optimizedLine.length <= 3) {
                                        setDirection = 1;
                                    }
                                    else if(Math.abs(optimizedLine[3]) != 1) {
                                        setDirection = 1;
                                    }
                                    else {
                                        setDirection = (int) optimizedLine[3];
                                    }

                                    indices.add(new MeshIndex(
                                            vertices.get((int) optimizedLine[0]),
                                            vertices.get((int) optimizedLine[1]),
                                            vertices.get((int) optimizedLine[2]),
                                            // Returns set direction. If direction isn't set it returns 1.
                                            // Double checks it isn't 0. If it is
                                            setDirection
                                    ));

                                    // For connected vertices it first checks if the vert
                                }
                                else {
                                    indices.add(new MeshIndex(
                                            vertices.get((int) optimizedLine[0]),
                                            vertices.get((int) optimizedLine[1]),
                                            vertices.get((int) optimizedLine[2]),
                                            indexCore
                                    ));
                                }
                                break;

                            // Core mode!
                            case 9:
                                boolean inverse = false;
                                if(optimizedLineString.length >= 4) {
                                    inverse = optimizedLine[3] == 1;
                                }
                                cores.add(new MeshCore(
                                    new MeshVertex((float) optimizedLine[0], (float) optimizedLine[1], (float) optimizedLine[2]),
                                        inverse
                                ));
                                break;
                            case 10:
                                int lineInt = Integer.parseInt(line);
                                // Double-checks the set core is a valid index
                                if(cores.size() > lineInt) {
                                    // It is! Get setting
                                    defaultCore = cores.get(lineInt);
                                }
                                break;
                        }

                    }
                }
                lineCount++;
            }
            bufferize();
        }
        catch(IOException exception) {
            exception.printStackTrace();
        }
    }

    // This makes the points into a buffer.
    private void bufferize() {
        // Sets the vertex array
        this.vaoID = GL40.glGenVertexArrays();
        GL40.glBindVertexArray(vaoID);

        // Creates a vertex buffer object
        this.vboID = GL40.glGenBuffers();
        GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, vboID);
        GL40.glBufferData(GL40.GL_ARRAY_BUFFER, getVertices(), GL40.GL_STATIC_DRAW);

        // Handles index buffers.
        this.eboID = GL40.glGenBuffers();
        GL40.glBindBuffer(GL40.GL_ELEMENT_ARRAY_BUFFER, this.eboID);
        this.indexBuffer = BufferUtils.newShortBuffer(this.indices.size());
        this.indexBuffer.flip();
        GL40.glBufferData(GL40.GL_ELEMENT_ARRAY_BUFFER, getIndices(), GL40.GL_STATIC_DRAW);

        GL40.glBindVertexArray(0);
        GL40.glEnableVertexAttribArray(0);
    }

    // Can be overridden. If it is this returns null that means it's been overridden. If it's overridden it checks
    // if it's set to be positive or negative. If it isn't it defaults to positive.

    // --------------------------------------------------
    // Mesh creation
    // --------------------------------------------------

    // This is for LibGDX support. Entirely possible to use just this with LibGDX Toolbox
    public Mesh toMesh() {
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
    }


    // --------------------------------------------------
    // Internal tools
    // --------------------------------------------------
    private int updateMode(String line, int currentMode) {
        line = line.trim();
        if(line.equalsIgnoreCase(("!"))) {
            return 0;
        }
        if(line.equalsIgnoreCase("!vert")) {
            return 7;
        }
        if(line.equalsIgnoreCase("!ind")) {
            return 8;
        }
        if(line.equalsIgnoreCase("!core")) {
            return 9;
        }
        if(line.equalsIgnoreCase("!def core")) {
            return 10;
        }
        return currentMode;
    }

    // Builders. Used for building stuff quickly.
    private void builders(String line, int lineNum) {
        if(line.toLowerCase().trim().replace(" ", "").startsWith("?construct")) {
            constructors(lineNum);
        }
    }

    private void setters(String line) {
        if(line.trim().toLowerCase().startsWith(".texture apply ")) {
            // Searches to see if the file exists.
            String searchingTexture = line.split(" ")[2];
            for(File file : texturePaths) {
                String fileName = file.getName();
                if(fileName.equals(searchingTexture + "_Texture")) {
                    texture = new TextureConstructor(file).getTexture();
                    break;
                }
            }
        }
    }

    // Automatically builds stuff. This includes adding vertices and indices
    private void constructors(int startLine) {

        // Just some info it will need and fill in as it loops.
        String shape = "";

        // For spheres
        double radius = 0;
        int points = 0;
        MeshVertex center = null;

        // Planes
        int xSpacing = 0;
        int xCount = 0;
        int zSpacing = 0;
        int zCount = 0;

        // Gets the line count and starts looping there.
        String[] lines = template.split("\n");
        while(true) {
            String line = lineCommentHandler(lines[startLine]);

            // ----------------------------------------------------------------------------------
            // Checks what shape you want to make.
            if(line.trim().equalsIgnoreCase("?construct sphere")) {
                // Sets mode to sphere.
                shape = "Sphere";
            }
            else if(line.trim().equalsIgnoreCase("?construct plane")) {
                // Sets mode to sphere.
                shape = "plane";
            }
            // ----------------------------------------------------------------------------------

            // ----------------------------------------------------------------------------------
            // For specific shapes.
            if(shape.equalsIgnoreCase("sphere")) {
                if (line.toLowerCase().startsWith("center ")) {
                    String[] splitLine = line.toLowerCase().replace("center ", "").replace(",", "").trim().split(" ");
                    center = new MeshVertex(Float.parseFloat(splitLine[0]), Float.parseFloat(splitLine[1]), Float.parseFloat(splitLine[2]));
                }

                if (line.toLowerCase().startsWith("radius ")) {
                    radius = Double.parseDouble(line.toLowerCase().replace("radius ", "").trim());
                }

                // Points are the points between the set vertices
                if (line.toLowerCase().startsWith("points ")) {
                    points = Integer.parseInt(line.toLowerCase().replace("points ", "").trim());
                    if (points < 0) {
                        throw new IllegalArgumentException("Points in sphere cannot be less than 0");
                    } else if (points > 20000) {
                        throw new IllegalArgumentException("Points cannot be greater than 20,000.");
                    }
                }
            }
            else if(shape.equalsIgnoreCase("plane")) {
                // Needs dimensions and points in each direction.
                if(line.toLowerCase().startsWith("x ")) {
                    String[] splitLine = line.
                            replace("x ", "").
                            replace(" ", "").
                            trim().
                            split(",");

                    xSpacing = Integer.parseInt(splitLine[0]);
                    xCount = Integer.parseInt(splitLine[1]);
                }

                else if(line.toLowerCase().startsWith("z ")) {
                    String[] splitLine = line.
                            replace("z ", "").
                            replace(" ", "").
                            trim().
                            split(",");

                    zSpacing = Integer.parseInt(splitLine[0]);
                    zCount = Integer.parseInt(splitLine[1]);
                }
            }

            // ----------------------------------------------------------------------------------


            if(line.replace(" ", "").trim().equalsIgnoreCase("?")) {
                // Done with the constructor.
                break;
            }

            startLine++;

        }

        // Generates the actual mesh.
        // Not a TON of code, just a lot of comments :)
        if(shape.equalsIgnoreCase("Sphere") && center != null) {
            AbstractMap.SimpleEntry<ArrayList<MeshVertex>, ArrayList<MeshIndex>> sphereDataPair =
                    meshPlusTools.generateSphere(points, vertices.size(), center, radius);

            vertices.addAll(sphereDataPair.getKey());
            indices.addAll(sphereDataPair.getValue());
        }

        else if(shape.equalsIgnoreCase("plane")) {
            AbstractMap.SimpleEntry<ArrayList<MeshVertex>, ArrayList<MeshIndex>> planeDataPair =
                    meshPlusTools.generatePlane(xCount, xSpacing, zCount, zSpacing, vertices.size());
            vertices.addAll(planeDataPair.getKey());
            indices.addAll(planeDataPair.getValue());
        }
    }


    // ------------------------------
    // Getters and setters
    // ------------------------------

    private MeshCore getMeshCore(double[] optimizedLine) {
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


    public float[] getVertices() {
        float[] floats = new float[vertices.size() * 3];
        int spot = 0;
        for(MeshVertex vertex : vertices) {
            floats[spot] = vertex.x;
            spot++;
            floats[spot] = vertex.y;
            spot++;
            floats[spot] = vertex.z;
            spot++;
        }
        return floats;
    }

    public short[] getIndices() {
        short[] shorts = new short[indices.size() * 3];
        int spot = 0;
        for(MeshIndex index : indices) {
            // TODO: Implement attribute order syncing
            short[] indexVals = index.getOrder();
            shorts[spot] = indexVals[0];
            spot++;
            shorts[spot] = indexVals[1];
            spot++;
            shorts[spot] = indexVals[2];
            spot++;
        }
        return shorts;
    }

    public int getVBO() {
        return vboID;
    }

    public int getVAO() {
        return vaoID;
    }

    public int getEBO() {
        return eboID;
    }


    // ------------------------------
    // Tools
    // ------------------------------

    private String lineCommentHandler(String line) {
        String modLine;
        String[] splitLine = line.trim().split("//");
        if(splitLine.length == 1)
            return line;

        // Modifies the line to get the substring minus the comment.
        // The nightmare below gets the starting index of the comment VVV
        modLine = line.substring( 0, (line.length() - 1) - splitLine[splitLine.length - 1].length() - 2 ).trim();

        return modLine;
    }

    public void dispose() {
        GL40.glDeleteBuffers(vboID);
        GL40.glDeleteVertexArrays(vaoID);
        GL40.glDeleteBuffers(eboID);
    }


}
