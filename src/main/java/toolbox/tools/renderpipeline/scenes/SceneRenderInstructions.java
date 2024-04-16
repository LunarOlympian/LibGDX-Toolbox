package toolbox.tools.renderpipeline.scenes;

@FunctionalInterface
public interface SceneRenderInstructions {

    // Used to let people write instructions to render a scene.
    // For example, they could render some meshes black, and render them slightly larger to the frame buffer.
    // Then they could restore their shape and render them normally, creating an outline effect.
    // Some meshes could also be rendered normally.

    void render(Scene scene);
}
