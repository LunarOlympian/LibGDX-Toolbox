package MeshesPlus;

import MeshesPlus.MeshParts.MeshVertex;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class MeshClone {
    private Mesh original;
    private float scale = 1f;
    private Matrix4 offset = new Matrix4().setToTranslation(0f, 0f, 0f);

    public MeshClone(Mesh original) {
        this.original = original;
    }

    public MeshClone(Mesh original, float scale, Vector3 offset) {
        // Copy so no pointer BS
        this.original = original.copy(false);
        this.original.scale(scale, scale, scale);
        this.scale = scale;
        this.offset = new Matrix4().setToTranslation(offset.x, offset.y, offset.z);
    }

    public MeshClone(Mesh original, Vector3 offset) {
        // Copy so no pointer BS
        this.original = original.copy(false);
        this.offset = new Matrix4().setToTranslation(offset.x, offset.y, offset.z);
    }

    public MeshClone setScale(float scale) {
        this.setScale(scale, scale, scale);
        return this;
    }
    public MeshClone setScale(float scaleX, float scaleY, float scaleZ) {
        this.original.scale(1 / this.scale, 1 / this.scale, 1 / this.scale);
        this.original.scale(scale, scale, scale);
        this.scale = scale;
        return this;
    }
    public MeshClone setOffset(Vector3 offset) {
        this.offset = new Matrix4().setToTranslation(offset.x, offset.y, offset.z);
        return this;
    }

    public Mesh getClone() {;
        original.transform(offset);
        return original;
    }


}
