package Toolbox.tools.renderpipeline.scenes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public interface PipelineRenderInstructions {

    void render(FrameBuffer buffer, Texture... textures);
}
