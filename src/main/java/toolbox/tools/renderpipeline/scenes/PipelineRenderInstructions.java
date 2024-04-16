package toolbox.tools.renderpipeline.scenes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public interface PipelineRenderInstructions {

    void render(FrameBuffer buffer, SpriteBatch spriteBatch, Texture... textures);
}
