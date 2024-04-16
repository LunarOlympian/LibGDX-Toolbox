package toolbox.tools.meshes.meshparts;

import java.util.ArrayList;
import java.util.List;

public class MeshIndex {

    private MeshVertex[] vertices = new MeshVertex[3];
    private MeshVertex normal;
    private MeshCore core;

    // This should remain consistent as indices are never associated with another mesh.
    // However, if the mesh is dynamic they can be adjusted.
    private short[] order;

    public MeshIndex(MeshVertex vert1, MeshVertex vert2, MeshVertex vert3, MeshCore core) {
        vertices[0] = vert1;
        vertices[1] = vert2;
        vertices[2] = vert3;

        // Used to keep track of the positions of exactly what vertex is being referred to.
        order = new short[] {
                (short) vert1.getID(),
                (short) vert2.getID(),
                (short) vert3.getID()
        };

        normal = vert1.calculateNormal(vert2, vert3);
        this.core = core;
        orderInds();
    }

    public MeshIndex(MeshVertex vert1, MeshVertex vert2, MeshVertex vert3, int direction) {
        vertices[0] = vert1;
        vertices[1] = vert2;
        vertices[2] = vert3;

        // Used to keep track of the positions of exactly what vertex is being referred to.
        order = new short[]{
                (short) vert1.getID(),
                (short) vert2.getID(),
                (short) vert3.getID()
        };

        normal = vert1.calculateNormal(vert2, vert3);
        core = calculatePosOrNegCore(direction);
        orderInds();
    }

    public void setUVVertices(MeshVertex vert1, MeshVertex vert2, MeshVertex vert3) {
        vertices[0].setUvVertex(vert1);
        vertices[1].setUvVertex(vert2);
        vertices[2].setUvVertex(vert3);

    }

    // Calculates a core depending on the overridden vale
    public MeshCore calculatePosOrNegCore(int direction) {
        // Averages the points first
        float avgX = (vertices[0].x + vertices[1].x + vertices[2].x) / 3;
        float avgY = (vertices[0].y + vertices[1].y + vertices[2].y) / 3;
        float avgZ = (vertices[0].z + vertices[1].z + vertices[2].z) / 3;

        // Subtracts direction to each (Subtracting seems to be necessary, otherwise it renders the wrong direction)
        avgX -= direction;
        avgY -= direction;
        avgZ -= direction;

        return new MeshCore(new MeshVertex(avgX, avgY, avgZ), false);
    }

    // Ok yes this is super weird but it works. Please just roll with it.
    public MeshVertex getNormal() {
        return normal;
    }

    // Used to figure out what direction to render the inds in.
    // THE 4TH ELEMENT OF THE LIST IS WHETHER IT'S VIEWED FROM POSITIVE, NEGATIVE, OR ADAPTIVE DIRECTION.
    // 1 is positive, -1 is negative, 0 adapts depending on the camera position.
    public void orderInds() {
        // w is ignored, if it's even present. It is seemingly irrelevant here.
        MeshVertex point1 = vertices[0], point2 = vertices[1],
                point3 = vertices[2];

        if(point1.equals(point2) || point1.equals(point3) || point2.equals(point3)) {
            // A tie so rendering doesn't even matter.
            return;
        }

        // The idea is the direction should equal to set
        int direction = getIndexDirection();

        // If negative does not match the direction mode the last 2 are swapped.
        if(-1 == direction) {
            order = new short[] {
                    order[0],
                    order[2],
                    order[1]
            };
        }

    }

    private int getIndexDirection() {
        // Ok, somewhat simple. Runs this calculation and if it's negative that means the core is under the index.
        // Inverts the result if needed.
        MeshVertex knownPoint = vertices[0];
        double answer = (
                        ( normal.x * (knownPoint.x - core.x) ) +
                        ( normal.y * (knownPoint.y - core.y) ) +
                        ( normal.z * (knownPoint.z - core.z) )
        );
        // Sets it to 1 or -1
        // Defaults to rendering the positive side for 0.
        int direction = answer < 0 ? -1 : 1;

        // Inverts it if the core is inverse;
        direction = core.getInverse() ? direction * -1 : direction;
        return direction;
    }

    public MeshVertex[] getVertices() {
        return vertices;
    }

    public void setVertices(MeshVertex[] vertices) {
        this.vertices = vertices;
    }

    public short[] getOrder() {
        return order;
    }

    public boolean sharedSide(MeshVertex vert1, MeshVertex vert2) {
        ArrayList<MeshVertex> vertexArrayList = new ArrayList<>(List.of(vertices));
        // Can't easily think of a more elegant way to do this.
        int score = 0;
        if(vertexArrayList.contains(vert1)) {
            score++;
        }
        if(vertexArrayList.contains(vert2)) {
            score++;
        }

        return score == 2;
    }

    // If the mesh is changed.
    public void updateOrder(float[] changes) {

    }

    @Override
    public String toString() {
        return vertices[0].getID() + ", " + vertices[1].getID() + ", " + vertices[2].getID();
    }


    @Override
    public boolean equals(Object pointObj) {
        if(!(pointObj instanceof MeshVertex point)) {
            return false;
        }
        return false; // Objects.equals(point.x, x) && Objects.equals(point.y, y) && Objects.equals(point.z, z) && Objects.equals(point.w, w);
    }
}
