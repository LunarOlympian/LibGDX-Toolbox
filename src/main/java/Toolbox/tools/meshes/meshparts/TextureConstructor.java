package Toolbox.tools.meshes.meshparts;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TextureConstructor {
    private String template;
    private Pixmap texture;
    private int width; // x
    private int height; // y
    private Color baseColor;

    // Loads in the template as a String
    public TextureConstructor(File template) {
        try {
            this.template = Files.readString(template.toPath());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        generateTexture();
    }

    // Same rough logic as MeshConstruction. Declare the important stuff at the top.
    private void generateTexture() {
        /*
        1. Get dimensions.
        2. Set base color.
        */
        int mode = 1;
        // Loops through line by line.
        for(String line : template.split("\n")) {
            line = line.trim();
            switch (mode) {
                case 1:
                    // Dimensions
                    String[] splitLine = line.replace(" ", "").trim().split(",");
                    width = Integer.parseInt(splitLine[0]);
                    height = Integer.parseInt(splitLine[1]);
                    mode++;
                    break;
                case 2:
                    // Base color.
                    interpretColor(line);
                    break;
            }
        }

        // Sets texture
        texture = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        // If baseColor isn't null sets texture to be it.
        if(baseColor != null) {
            texture.setColor(baseColor);
            texture.fillRectangle(0, 0, width, height);
        }
    }

    public Texture getTexture() {
        return new Texture(texture);
    }

    // --------------------------------------------------
    // Internal tools
    // --------------------------------------------------

    private void interpretColor(String line) {
        // Switch to check if it's a simple color.
        line = line.toLowerCase();

        try {
            // Ok now it just gets the RGB values.
            String[] rgbValues = line.replace(" ", "").split(",");
            baseColor = new Color().add(Float.parseFloat(rgbValues[0]),
                    Float.parseFloat(rgbValues[1]),
                    Float.parseFloat(rgbValues[2]),
                    // Alpha.
                    Float.parseFloat(rgbValues[3]));
        } catch(NumberFormatException exception) {
            switch(line) {
                case "red":
                    baseColor = Color.RED;
                    break;
                case "green":
                    baseColor = Color.GREEN;
                    break;
                case "blue":
                    baseColor = Color.BLUE;
                    break;
                case "yellow":
                    baseColor = Color.YELLOW;
                    break;
                case "orange":
                    baseColor = Color.ORANGE;
                    break;
                case "purple":
                    baseColor = Color.PURPLE;
                    break;
                case "white":
                    baseColor = Color.WHITE;
                    break;
                case "black":
                    baseColor = Color.BLACK;
                    break;
                default:
                    break;
            }
        }

    }
}
