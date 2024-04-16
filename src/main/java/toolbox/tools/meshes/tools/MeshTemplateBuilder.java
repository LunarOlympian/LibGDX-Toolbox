package toolbox.tools.meshes.tools;

import toolbox.tools.groups.Pair;
import toolbox.tools.groups.Trio;
import toolbox.tools.meshes.meshdata.MeshData;
import toolbox.tools.meshes.meshparts.MeshCore;
import toolbox.tools.meshes.meshparts.MeshIndex;
import toolbox.tools.meshes.meshparts.MeshVertex;
import com.badlogic.gdx.utils.BufferUtils;
import org.lwjgl.opengl.GL40;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

public class MeshTemplateBuilder implements Serializable {
    private MeshTools meshTools = new MeshTools();
    // --------------------
    // Methods
    // --------------------

    public static Pair<MeshData, HashMap<String, Object>> buildFromTemplate(String template, String ID) {
        ArrayList<MeshVertex> vertices = new ArrayList<>();
        ArrayList<MeshIndex> indices = new ArrayList<>();
        ArrayList<MeshCore> cores = new ArrayList<>();

        String alias = "a_position";
        ShortBuffer indexBuffer = null;

        HashMap<String, Object> components = new HashMap<>();


        try {
            if (!template.contains("\n")) {
                // Probably a file. Interpret it as such.
                template = Files.readString(new File(template).toPath());
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
            Alias
            Vertices
            Indices
            Cores
            */


            // First checks if it's an object or a template and sets the variable
            boolean isMPT = template.split("\n")[0].replace(" ", "").trim().equalsIgnoreCase("#MPT");
            // ------------------------------
            // First just sets some defaults
            ArrayList<MeshVertex> normals = new ArrayList<>();
            ArrayList<MeshVertex> uvTextureCoords = new ArrayList<>();
            ArrayList<Trio<Integer, Integer, Integer>> faces = new ArrayList<>();
            MeshCore core = null;

            // Interprets it as an obj file.
            for (String line : template.split("\n")) {
                // isMPT is used to enable some things. Not implemented yet though
                if(line.toLowerCase().startsWith("# alias - ")) {
                    alias = line.substring(10).trim();
                }
                else if(line.toLowerCase().startsWith("# core - ")) {
                    line = line.toLowerCase().replace("# core - ", "");
                    String[] splitLine = line.split("(,?) ");

                    core = new MeshCore(new MeshVertex(Float.parseFloat(splitLine[0]), Float.parseFloat(splitLine[1]),
                            Float.parseFloat(splitLine[2])), false);
                }
                else if(line.startsWith("v ")) {
                    String[] splitLine = line.trim().split(" ");
                    vertices.add(new MeshVertex(Float.parseFloat(splitLine[1]), Float.parseFloat(splitLine[2]),
                            Float.parseFloat(splitLine[3]), vertices.size()));
                }
                else if(line.startsWith("vn ")) {
                    String[] splitLine = line.trim().split(" ");
                    normals.add(new MeshVertex(Float.parseFloat(splitLine[1]), Float.parseFloat(splitLine[2]),
                            Float.parseFloat(splitLine[3])));
                }
                else if(line.startsWith("vt ")) {
                    String[] splitLine = line.trim().split(" ");
                    uvTextureCoords.add(new MeshVertex(Float.parseFloat(splitLine[1]),
                            Float.parseFloat(splitLine[2])));
                }
                else if(line.startsWith("f ")) {
                    // Example face: f 1/1/1 2/2/1 3/3/1
                    for(String faceVertex : line.trim().split(" ")) {
                        if(!faceVertex.startsWith("f")) {
                            String[] splitFace = faceVertex.split("/");
                            faces.add(new Trio<>(Integer.parseInt(splitFace[0]), Integer.parseInt(splitFace[1]),
                                    Integer.parseInt(splitFace[2])));
                        }
                    }
                }
            }
            // TODO: Delete cores, they're horribly outdated.
            // Finished analyzing the file, just needs to fill in the needed info now.
            // First, throws an error if core is null
            if(core == null) {
                throw new NullPointerException("Please define the mesh core(s) in .obj files.");
            }

            // Builds indices. Sets the needed info for the index in the arrays below then builds it.
            MeshVertex[] buildingIndices = new MeshVertex[3];
            MeshVertex[] buildingUV = new MeshVertex[3];
            int pos = 0;
            for(Trio<Integer, Integer, Integer> trio : faces) {
                pos = pos % 3; // This makes it so it builds 3 vertices then loops

                // This adds to the builder
                buildingIndices[pos] = vertices.get(trio.getFirst() - 1);
                buildingIndices[pos].setUvVertex(uvTextureCoords.get(trio.getSecond() - 1));

                pos++;
                if(pos == 3) {
                    // This means it set all the necessary info. It builds the index now
                    MeshCore coreTest = new MeshCore(new MeshVertex(0f, 0f, 0f), false);
                    // TODO remove core requirement! This is just to get this working sooner!
                    indices.add(new MeshIndex(buildingIndices[0], buildingIndices[1], buildingIndices[2], coreTest));
                    cores.add(coreTest);
                    // --------------------------------------------------
                    indices.getLast().setUVVertices(buildingUV[0], buildingUV[1], buildingUV[2]);
                }
            }


            // --------------------------------------------------
            // OpenGL stuff
            // --------------------------------------------------
            // Sets the vertex array (a_position)

            // Creates a vertex buffer object
            // Generates the array versions of the needed buffers first.
            float[] verticesArray = new float[vertices.size() * 3];
            int spot = 0;
            for(MeshVertex vertex : vertices) {
                verticesArray[spot] = vertex.x;
                spot++;
                verticesArray[spot] = vertex.y;
                spot++;
                verticesArray[spot] = vertex.z;
                spot++;
            }

            short[] indicesArray = new short[indices.size() * 3];
            spot = 0;
            for(MeshIndex index : indices) {
                // TODO: Implement attribute order syncing
                short[] indexVals = index.getOrder();
                indicesArray[spot] = indexVals[0];
                spot++;
                indicesArray[spot] = indexVals[1];
                spot++;
                indicesArray[spot] = indexVals[2];
                spot++;
            }

            int vaoID;
            int vtoID = -1;
            int vboID;
            int eboID;
            vaoID = GL40.glGenVertexArrays();
            GL40.glBindVertexArray(vaoID);

            vboID = GL40.glGenBuffers();
            GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, vboID);
            GL40.glBufferData(GL40.GL_ARRAY_BUFFER, verticesArray, GL40.GL_STATIC_DRAW);

            // Handles index buffers.
            eboID = GL40.glGenBuffers();
            GL40.glBindBuffer(GL40.GL_ELEMENT_ARRAY_BUFFER, eboID);
            indexBuffer = BufferUtils.newShortBuffer(indices.size());
            indexBuffer.flip();
            GL40.glBufferData(GL40.GL_ELEMENT_ARRAY_BUFFER, indicesArray, GL40.GL_STATIC_DRAW);


            GL40.glBindVertexArray(0);
            GL40.glBindBuffer(GL40.GL_ELEMENT_ARRAY_BUFFER, 0);
            GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, 0);

            GL40.glEnableVertexAttribArray(0);

            return new Pair<>(new MeshData(ID, vertices, indices, cores, alias, vaoID, vtoID, vboID, eboID, indexBuffer), components);

        }
        catch(IOException exception) {
            exception.printStackTrace();
        }
        return new Pair<>(null, null);
    }

    // --------------------------------------------------
    // Internal tools/stuff used to build meshes easily
    // --------------------------------------------------
    // Builders. Used for building stuff quickly.
    private void builders(String line, int lineNum, ArrayList<MeshVertex> vertices, ArrayList<MeshIndex> indices, ArrayList<MeshCore> cores, String template) {
        if(line.toLowerCase().trim().replace(" ", "").startsWith("?construct")) {
            constructors(lineNum, vertices, indices, cores, template);
        }
    }

    private void setters(String line) {
        /*
        TODO Add when texture creation is a thing
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
        */
    }

    // Automatically builds stuff. This includes adding vertices and indices
    private void constructors(int startLine, ArrayList<MeshVertex> vertices, ArrayList<MeshIndex> indices, ArrayList<MeshCore> cores, String template) {

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
                    meshTools.generateSphere(points, vertices.size(), center, radius);

            vertices.addAll(sphereDataPair.getKey());
            indices.addAll(sphereDataPair.getValue());
        }

        else if(shape.equalsIgnoreCase("plane")) {
            AbstractMap.SimpleEntry<ArrayList<MeshVertex>, ArrayList<MeshIndex>> planeDataPair =
                    meshTools.generatePlane(xCount, xSpacing, zCount, zSpacing, vertices.size());
            vertices.addAll(planeDataPair.getKey());
            indices.addAll(planeDataPair.getValue());
        }
    }


    // ------------------------------
    // Tools
    // ------------------------------

    private String lineCommentHandler(String line) {
        String modLine;
        String[] splitLine = line.trim().split("#");
        if(splitLine.length == 1)
            return line;

        // Modifies the line to get the substring minus the comment.
        // The nightmare below gets the starting index of the comment VVV
        modLine = line.substring( 0, (line.length() - 1) - splitLine[splitLine.length - 1].length() - 2 ).trim();

        return modLine;
    }

    // ------------------------------
}
