package Toolbox.interfaces;

public interface ToolBoxDisposable {
    // Used to enable disposal of objects
    // However some objects can be rebuilt.
    // This is useful for stuff like meshes that you don't always want in memory but recompiling may be a pain.
    // Saves some time in file interpretation.

    void dispose();

    boolean rebuild();

    boolean disposedOf();
}
