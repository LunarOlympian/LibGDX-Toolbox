package MeshesPlus.MeshParts;

public class MeshCore {
    MeshVertex location;
    public Float x, y, z;
    private boolean inverse = false;

    // Intended, but not required.
    public MeshCore(MeshVertex location, boolean inverse) {
        this.location = location;
        this.x = location.x;
        this.y = location.y;
        this.z = location.z;
        // A non-inverted mesh renders sides facing away from the core, an inverted one renders sides facing towards
        // the core.
        this.inverse = inverse;
    }

    public MeshVertex getLocation() {
        return location;
    }

    public boolean getInverse() {
        return inverse;
    }

    @Override
    public String toString() {
        return location.x + ", " + location.y + ", " + location.z + ". " + (inverse ? "Inverted." : "Not inverted.");
    }


}
