package MeshesPlus;

import MeshesPlus.MeshParts.MeshVertex;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class MeshClone {
    private Mesh original;
    private float scaleX = 1f;
    private float scaleY = 1f;
    private float scaleZ = 1f;
    private Matrix4 offset = new Matrix4().setToTranslation(0f, 0f, 0f);

    public MeshClone(Mesh original) {
        this.original = original;
    }

    public MeshClone(Mesh original, float setScale, Vector3 offset) {
        // Copy so no pointer BS
        this.original = original.copy(false);
        this.original.scale(setScale, setScale, setScale);
        this.scaleX = setScale;
        this.scaleY = setScale;
        this.scaleZ = setScale;
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
        this.original.scale(1 / this.scaleX, 1 / this.scaleY, 1 / this.scaleZ);
        this.original.scale(scaleX, scaleY, scaleZ);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
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
