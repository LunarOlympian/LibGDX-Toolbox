package Toolbox.tools.meshes.meshparts;

import java.util.ArrayList;
import java.util.Objects;

public class MeshVertex {
    // Complains if w isn't 1
    public Float x, y, z = 0f;
    public Float w = 1f;
    private final MeshVertex A = this;

    // Used for classification of vertices
    private short id;

    // This is all the end points of the lines.
    private ArrayList<MeshVertex> connections = new ArrayList<>();
    private ArrayList<Double> angles = new ArrayList<>();
    private ArrayList<MeshVertex> intersections = new ArrayList<>();

    // The ID of recent clone that has been dealt with
    private int dealingWithCloneInd = 0;

    private boolean alreadyCalculatedCloneInds = false;

    private MeshVertex uvVertex;

    public MeshVertex(Float x, Float y, Float z, int id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = (short) id;
    }

    public MeshVertex(Float x, Float y, Float z, Float w, int id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.id = (short) id;
    }

    // If no id for some reason.
    public MeshVertex(Float x, Float y, Float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public MeshVertex(Float x, Float y) {
        this.x = x;
        this.y = y;
    }

    public MeshVertex subtract(MeshVertex B) {
        // Ignoring Ws for now.
        return new MeshVertex(B.x - A.x, B.y - A.y, B.z - A.z);
    }

    public MeshVertex calculateNormal(MeshVertex B, MeshVertex C) {
        MeshVertex h = B.subtract(A);
        MeshVertex i = C.subtract(A);

        // w is ignored.
        return new MeshVertex(
                (h.y * i.z) - (h.z * i.y),
                (h.z * i.x) - (h.x * i.z),
                (h.x * i.y) - (h.y * i.x)
        );
    }

    // --------------------
    // Connections and shit
    // --------------------

    // ind1 and ind2 are the indices containing vertex and vertex2
    public void addConnections(MeshVertex vertex, MeshVertex vertex2) {
        if(!connections.contains(vertex)) {
            connections.add(vertex);
            angles.add(null);
        }
        if(!connections.contains(vertex2)) {
            connections.add(vertex2);
            angles.add(null);
        }
    }

    public void setX(float x) {
        this.x = x;
    }
    public void setY(float y) {
        this.y = y;
    }
    public void setZ(float z) {
        this.z = z;
    }

    // Sets the angle for the border formed by this vertex and the vertex define in addAngle.
    public void addAngle(MeshVertex connVertex, double angle) {
        int spot = 0;
        for(MeshVertex vertex : connections) {
            if(vertex == connVertex) {
                angles.set(spot, angle);
            }
            spot++;
        }
    }

    public ArrayList<MeshVertex> getConnections() {
        return connections;
    }

    public ArrayList<Double> getAngles() {
        return angles;
    }

    // Gets the ID of the clone it needs to be updating
    public short getIDWithClones() {
        // Returns relative to the original's ID.

        short returnMe = (short) (id + Math.floor(dealingWithCloneInd / 4.0));
        dealingWithCloneInd++;
        return returnMe;
    }

    public void resetIDCount() {
        this.dealingWithCloneInd = 0;
    }

    public int getTotalSize() {
        // This is the total size in the final thing the element takes up. So it's:
        // (Vertex component num * (connections size * 4)) + 4. So this is 20.
        return (int) (20 *
                // Idea here is it's 4 values for each thing including clone.
                // So it calculates clone count (+ 1 is to include self) then it multiplies by 4.
                Math.ceil((double) (connections.size() + 1) / 4));
    }

    public void flipCalculatedCloneInds() {
        alreadyCalculatedCloneInds = !alreadyCalculatedCloneInds;
    }

    public boolean getCalculatedInds() {
        return alreadyCalculatedCloneInds;
    }

    public void setUvVertex(MeshVertex vertex) {
        uvVertex = vertex;
    }

    public MeshVertex getUVVertex() {
        return uvVertex;
    }

    public int getID() {
        return id;
    }

    @Override
    public String toString() {
        return x + ", " + y + ", " + z;
    }

    @Override
    public boolean equals(Object pointObj) {
        if(!(pointObj instanceof MeshVertex point)) {
            return false;
        }
        return Objects.equals(point.x, x) && Objects.equals(point.y, y) && Objects.equals(point.z, z) && Objects.equals(point.w, w);
    }
}
