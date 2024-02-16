package MeshesPlus;

import MeshesPlus.MeshParts.MeshCore;
import MeshesPlus.MeshParts.MeshIndex;
import MeshesPlus.MeshParts.MeshVertex;
import MeshesPlus.MeshParts.TextureConstructor;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MeshConstructor {
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

    private MeshVertex meshPosition = new MeshVertex(0f, 0f, 0f);


    // --------------------------------------------------
    // Getters and setters
    // --------------------------------------------------
    public MeshConstructor(File template) {
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

    public MeshConstructor(String template) {
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
    // Constructor
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
                        setters(line, lineCount);
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
        }
        catch(IOException exception) {
            exception.printStackTrace();
        }
    }

    // Can be overridden. If it is this returns null that means it's been overridden. If it's overridden it checks
    // if it's set to be positive or negative. If it isn't it defaults to positive.

    // --------------------------------------------------
    // Mesh creation
    // --------------------------------------------------
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

    // Just stuff that isn't mandatory, like a texture.
    private void setters(String line, int lineNum) {
        if(line.trim().toLowerCase().startsWith("?texture apply ")) {
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
        else if(line.toLowerCase().trim().replace(" ", "").startsWith("?construct")) {
            constructors(lineNum);
        }
    }

    // Automatically builds stuff. This includes adding vertices and indices
    private void constructors(int startLine) {

        // Just some info it will need and fill in as it loops.
        String shape = "";
        double radius = 0;
        int points = 0;
        MeshVertex center = null;

        // Gets the line count and starts looping there.
        String[] lines = template.split("\n");
        while(true) {

            String line = lines[startLine];

            // ----------------------------------------------------------------------------------
            // Checks what shape you want to make.
            if(line.trim().equalsIgnoreCase("?construct sphere")) {
                // Sets mode to sphere.
                shape = "Sphere";
            }
            // ----------------------------------------------------------------------------------

            // ----------------------------------------------------------------------------------
            // Checks info, namely point count and center of the mesh
            if(line.toLowerCase().startsWith("center ")) {
                String[] splitLine = line.toLowerCase().replace("center ", "").replace(",", "").trim().split(" ");
                center = new MeshVertex(Float.parseFloat(splitLine[0]), Float.parseFloat(splitLine[1]), Float.parseFloat(splitLine[2]));
            }

            if(line.toLowerCase().startsWith("radius ")) {
                radius = Double.parseDouble(line.toLowerCase().replace("radius ", "").trim());
            }

            // Points are the points between the set vertices
            if(line.toLowerCase().startsWith("points ")) {
                points = Integer.parseInt(line.toLowerCase().replace("points ", "").trim());
                if(points < 0) {
                    throw new IllegalArgumentException("Points in sphere cannot be less than 0");
                }
                else if(points > 20000) {
                    throw new IllegalArgumentException("Points cannot be greater than 20,000.");
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
            // Gets the total count of points.
            int pointCount = (int) ((points * 12) + 6 + (Math.pow(points, 2) * 8));
            // * 12 gets the 3 interlocking circles points that aren't on a circle intersection.
            // + 6 gets the intersections, including the top and bottom points.
            // (Math.pow(points, 2) * 8) gets the remaining points.

            ArrayList<MeshVertex> spherePoints = new ArrayList<>(Arrays.asList(new MeshVertex[pointCount]));
            int sizeOffset = vertices.size();

            // Sets the defined points.
            // The ID is calculated relative to the points already in the list.
            // The names are based on staring at the circle from the z axis.
            MeshVertex topPoint = new MeshVertex(center.x, center.y + (float) radius, center.z, sizeOffset);
            // Bottom point is the last point in the index to make locating it and calculating
            MeshVertex bottomPoint = new MeshVertex(center.x, center.y - (float) radius, center.z, sizeOffset + pointCount - 1);

            // Adds them to their proper spots. First and last respectively
            spherePoints.set(0, topPoint);
            spherePoints.set(pointCount - 1, bottomPoint);


                    /*
                    Builds a circle.
                    Calculates the x, y, and z values of points then alters the radian values to complete a circle.
                    */

            int pointsInCircle = 4 + (points * 4);
            // This is the size in radians it moves each loop.
            // The + 1 ensures it will not do 360 and instead stop 1 jump away. This prevents making a copy of a point.
            float degreeInc = (float) (360.0 / (pointsInCircle));
            float incrementSize = (float) Math.toRadians(degreeInc);
            int pointsIncrease = (points * 2) + 1;
            // Loop to set the points.

            int setCount = 0;
            // Yes these end late. This is to allow for proper setting of values where x, y, or z equal 0 (relative to the center of course)
            for(int i = 1; i <= pointsIncrease; i++) { // Top to bottom.
                // Starts at 1 as 0 is the top point and is already calculated
                // To prevent ridiculous values all coordinates are rounded to 3 decimal places
                float yVal = Math.round( (radius * Math.cos(incrementSize * i)) * 1000f ) / 1000f;
                float xVal = Math.round( (radius * Math.sin(incrementSize * i)) * 1000f ) / 1000f;

                for(int h = 0; h < pointsInCircle; h++) { // Left to right. Loops around.
                    // pointsInCircle - 1 is to prevent a full loop.
                    // Sets the degree/radian value here.
                    // This is the value in radians it has incremented around the circle.
                    // It should never reach pointsInCircle as that would be 360.
                    float radVal = incrementSize * h;

                    // Calculates the z and x coordinates. X is the radius of this circle. Y remains unchanged and unused.
                    float zVal = Math.round(((xVal * Math.sin(radVal)) * 1000f)) / 1000f;
                    float xValNew = Math.round(((xVal * Math.cos(radVal)) * 1000f)) / 1000f;

                    // Ok! So these both assume the circle is at 0, 0, 0.
                    // We can use this to our advantage and treat the values as the difference between the center and the point.
                    // First, however, it defines the point.
                    spherePoints.set(i + (pointsIncrease * h), new MeshVertex(center.x + xValNew, center.y + yVal, center.z + zVal, sizeOffset + i + (pointsIncrease * h)) );
                    setCount++;
                }
            }

            vertices.addAll(spherePoints);
            ArrayList<MeshIndex> sphereIndices = new ArrayList<>();

            MeshCore core = new MeshCore(center, false);

            // Now it sets indices.
            // Starts by setting all connections to the top and bottom.
            for(int i = 0; i < pointsInCircle; i++) {
                // Sets the ID it's connecting to the top point
                int ID = 1 + (i * pointsIncrease);
                // Sets the ID next to it. If it's the last number it connects to 1.
                // Otherwise, it connects to the next ID up.
                int nextID = (i == (pointsInCircle - 1) ? 1 : ID + pointsIncrease);
                // Adds the index.
                sphereIndices.add(new MeshIndex(spherePoints.get(0), spherePoints.get(ID), spherePoints.get(nextID), core));
            }

            // Same but with the bottom.
            for(int i = 0; i < pointsInCircle; i++) {
                // Sets the ID it's connecting to the top point
                int ID = pointsIncrease + (i * pointsIncrease);
                // Sets the ID next to it. If it's the last number it connects to 1.
                // Otherwise, it connects to the next ID up.
                int nextID = (i == (pointsInCircle - 1) ? pointsIncrease : ID + pointsIncrease);
                // Adds the index.
                sphereIndices.add(new MeshIndex(spherePoints.get(spherePoints.size() - 1), spherePoints.get(ID), spherePoints.get(nextID), core));
            }

            for(int h = 0; h < pointsIncrease - 1; h++) {
                for(int i = 0; i < pointsInCircle; i++) {
                    // Sets the ID it's connecting to the top point
                    int ID = (h + 1) + (i * pointsIncrease);
                    int ID_P = (i == (pointsInCircle - 1) ? 1 + h : ID + pointsIncrease);
                    int ID_D = ID + 1;
                    int ID_P_D = ID_P + 1;
                    sphereIndices.add(new MeshIndex(spherePoints.get(ID), spherePoints.get(ID_P), spherePoints.get(ID_D), core));
                    sphereIndices.add(new MeshIndex(spherePoints.get(ID_P_D), spherePoints.get(ID_P), spherePoints.get(ID_D), core));
                }
            }


            indices.addAll(sphereIndices);
        }
    }

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


}
