package MeshesPlus;

import MeshesPlus.MeshParts.MeshVertex;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Matrix4;

public class MeshClone {
    private Mesh original;
    private float scale = 1f;
    private Matrix4 offset;

    public MeshClone(Mesh original) {
        this.original = original;
    }

    public MeshClone(Mesh original, float scale, MeshVertex offset) {
        // Copy so no pointer BS
        this.original = original.copy(false);
        this.original.scale(scale, scale, scale);
        this.scale = scale;
        this.offset = new Matrix4().setToTranslation(offset.x, offset.y, offset.z);
    }

    public MeshClone(Mesh original, MeshVertex offset) {
        // Copy so no pointer BS
        this.original = original.copy(false);
        this.offset = new Matrix4().setToTranslation(offset.x, offset.y, offset.z);
    }

    // Modifications to it
    public MeshClone setScale(float scale) {
        this.original.scale(1 / this.scale, 1 / this.scale, 1 / this.scale);
        this.original.scale(scale, scale, scale);
        this.scale = scale;
        return this;
    }
    public MeshClone setOffset(MeshVertex offset) {
        this.offset = new Matrix4().setToTranslation(offset.x, offset.y, offset.z);
        return this;
    }

    public Mesh getClone() {;
        original.transform(offset);
        return original;
    }


}
