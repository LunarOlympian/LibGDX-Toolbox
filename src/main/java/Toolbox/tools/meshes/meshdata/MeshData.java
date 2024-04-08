package Toolbox.tools.meshes.meshdata;


import Toolbox.tools.meshes.meshparts.MeshCore;
import Toolbox.tools.meshes.meshparts.MeshIndex;
import Toolbox.tools.meshes.meshparts.MeshVertex;

import java.nio.ShortBuffer;
import java.util.ArrayList;

// This is the pure, essential, data for a Mesh. You wanna build the mesh? Here's what you need.
// Basically the template for the mesh. It can be altered but all "MeshInstance"s will be altered as well.
public record MeshData(String ID, ArrayList<MeshVertex> vertices, ArrayList<MeshIndex> indices, ArrayList<MeshCore> cores,
                       String alias,
                       int vaoID, int vtoID, int vboID, int eboID, ShortBuffer indexBuffer) {

    public MeshData(String ID, ArrayList<MeshVertex> vertices, ArrayList<MeshIndex> indices, ArrayList<MeshCore> cores,
                    String alias,
                    int vaoID, int vtoID, int vboID, int eboID, ShortBuffer indexBuffer) {
        this.ID = ID;
        this.vertices = vertices;
        this.indices = indices;
        this.cores = cores;

        this.alias = alias;

        this.vaoID = vaoID;
        this.vtoID = vtoID;
        this.vboID = vboID;
        this.eboID = eboID;
        this.indexBuffer = indexBuffer;
    }

}
