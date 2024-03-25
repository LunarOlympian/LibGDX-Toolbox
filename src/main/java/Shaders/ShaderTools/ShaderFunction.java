package Shaders.ShaderTools;

public class ShaderFunction {
    // TL;DR adds a function to a shader. Useful if you want to run a function in multiple shaders without rewriting.
    // Make more useful if you can! Maybe stuff like checking for errors?

    private String function;
    private int attatchTo = -1; // -1 just attaches it to the fragment shader.

    public ShaderFunction(String function) {
        this.function = function;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction() {
        this.function = function;
    }

    // Attaches to a shader defined by the ID of its type.
    public void setAttachTo(int attachTo) {
        this.attatchTo = attachTo;
    }

    public int getAttatchTo() {
        return attatchTo;
    }
}
