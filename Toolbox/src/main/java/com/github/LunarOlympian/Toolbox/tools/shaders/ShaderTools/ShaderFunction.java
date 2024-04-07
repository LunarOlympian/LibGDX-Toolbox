package com.github.LunarOlympian.Toolbox.tools.shaders.ShaderTools;

import java.util.List;

public class ShaderFunction {
    // TL;DR adds a function to a shader. Useful if you want to run a function in multiple shaders without rewriting.
    // Make more useful if you can! Maybe stuff like checking for errors?
    // Also allows prebuilt functions to exist so people don't have to copy-paste all the time

    public final static int lightMapDistance = 0;
    public final static int lightDistancePoint = 1;


    public final static String[] prebuiltFunctions = new String[] {
            """
                float lightMapDistance(vec4 LMDcolor) {
                    return (LMDcolor.x * 255) + (LMDcolor.y * 255) + LMDcolor.z;
                }""",



            """
                float lightDistancePoint(vec3 LDPaPosi, vec3 LDPlight_position) {
                    float LDPcamera_Dist = pow((LDPaPosi.x - LDPlight_position.x), 2) + pow((LDPaPosi.y - LDPlight_position.y), 2) + pow((LDPaPosi.z - LDPlight_position.z), 2);
                    return abs(sqrt(LDPcamera_Dist));
                }""",



            """
               float lightMapDistance(vec4 LMDcolor) {
                    return (LMDcolor.x * 255) + (LMDcolor.y * 255) + LMDcolor.z;
               }
               
               float lightDistancePoint(vec3 LDPaPosi, vec3 LDPlight_position) {
                   float LDPcamera_Dist = pow((LDPaPosi.x - LDPlight_position.x), 2) + pow((LDPaPosi.y - LDPlight_position.y), 2) + pow((LDPaPosi.z - LDPlight_position.z), 2);
                   return abs(sqrt(LDPcamera_Dist));
               }
               
               
               float getPixelBrightness(vec4 GPBaPos, sampler2D GPBLightmap,
               mat4 GPBLightProj, vec3 GPBLightCoords, bool GPB_ISL, bool GPBAntiAliasing) {
                    vec4 relativeCoordsUnfin = GPBLightProj * GPBaPos;
                    vec2 relativeCoords = relativeCoordsUnfin.xy / relativeCoordsUnfin.w;
                    
                    
                    if(relativeCoords.x > 1 || relativeCoords.x < -1 ||
                    relativeCoords.y > 1 || relativeCoords.y < -1) {
                        return 0;
                    }
                    else {
                        vec2 posOnTex = (relativeCoords.xy + 1.0) / 2.0;
                        // The background has an alpha of 0 while the light texture has an alpha of 1.
                        // This can be used to determine the original light value.
                        vec4 texColor = texture(GPBLightmap, posOnTex);
                        float mult = 1;
                        if(texColor.w != 1) {
                            mult = texColor.w;
                            texColor = vec4(texColor.xyz / texColor.w, 1);
                        }
                        
                        float lightMapDistance = lightMapDistance(texColor);
                        float lightDistance = lightDistancePoint(GPBaPos.xyz, GPBLightCoords);
                        if(abs(lightDistance - lightMapDistance) <= 0.01) {
                            return 1 * mult;
                        }
                        
                        return 0;
                    }
               }"""

            // Issue here is the small pixels block light from hitting the side, but only in certain areas.
            // This creates a fairly weird effect.



    };

    /*
    0/lightMapDistance - Converts a pixel from a lightmap to a float which is the distance to the light.

    1/lightDistancePoint - Calculates the light's distance from a point

    2/getPixelBrightness - Calculates the pixel's brightness based on some factors.
        i. The coordinates of the pixel (aPos).
        ii. The lightmap
        iii. The light's projection
        iv. The light's coordinates.
        v. A bool to enable the inverse square law applying
        vi. A bool to enable or disable anti-aliasing
    */
    // TODO sort out light coordinates with plane lighting

    private String function;
    private int attatchTo = -1; // -1 just attaches it to the fragment shader.


    // Constructors
    public ShaderFunction(String function) {
        this.function = function;
    }

    public ShaderFunction(int functionCode) {
        this.function = prebuiltFunctions[functionCode];
    }


    // Getters and setters
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

    public static String functionCommentInsertion(String shader, List<String> matches) {
        for(String match : matches) {
            match = match.replace("/", "").replace("?", "").trim();
            shader = shader.replace("//?" + match + "?", prebuiltFunctions[Integer.parseInt(match)]);
        }
        return shader;
    }
}
