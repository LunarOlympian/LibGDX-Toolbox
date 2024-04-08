package Toolbox.tools.meshes.tools;



import Toolbox.tools.meshes.meshparts.MeshCore;
import Toolbox.tools.meshes.meshparts.MeshIndex;
import Toolbox.tools.meshes.meshparts.MeshVertex;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;

public class MeshTools {

    public AbstractMap.SimpleEntry<ArrayList<MeshVertex>, ArrayList<MeshIndex>> generateSphere
                    (int points, int sizeOffset, MeshVertex center, double radius) {
        // Gets the total count of points.
        int pointCount = (int) ((points * 12) + 6 + (Math.pow(points, 2) * 8));
        // * 12 gets the 3 interlocking circles points that aren't on a circle intersection.
        // + 6 gets the intersections, including the top and bottom points.
        // (Math.pow(points, 2) * 8) gets the remaining points.

        ArrayList<MeshVertex> spherePoints = new ArrayList<>(Arrays.asList(new MeshVertex[pointCount]));

        // Sets the defined points.
        // The ID is calculated relative to the points already in the list.
        // The names are based on staring at the circle from the z axis.
        MeshVertex topPoint = new MeshVertex(center.x, center.y + (float) radius, center.z, sizeOffset);
        // Bottom point is the last point in the index to make locating it and calculating
        MeshVertex bottomPoint = new MeshVertex(center.x, center.y - (float) radius, center.z, sizeOffset + pointCount - 1);

        // Adds them to their proper spots. First and last respectively
        spherePoints.set(0, topPoint);
        spherePoints.set(pointCount - 1, bottomPoint);


                    /*
                    Builds a circle.
                    Calculates the x, y, and z values of points then alters the radian values to complete a circle.
                    */

        int pointsInCircle = 4 + (points * 4);
        // This is the size in radians it moves each loop.
        // The + 1 ensures it will not do 360 and instead stop 1 jump away. This prevents making a copy of a point.
        float degreeInc = (float) (360.0 / (pointsInCircle));
        float incrementSize = (float) Math.toRadians(degreeInc);
        int pointsIncrease = (points * 2) + 1;
        // Loop to set the points.

        int setCount = 0;
        // Yes these end late. This is to allow for proper setting of values where x, y, or z equal 0 (relative to the center of course)
        for(int i = 1; i <= pointsIncrease; i++) { // Top to bottom.
            // Starts at 1 as 0 is the top point and is already calculated
            // To prevent ridiculous values all coordinates are rounded to 3 decimal places
            float yVal = Math.round( (radius * Math.cos(incrementSize * i)) * 1000f ) / 1000f;
            float xVal = Math.round( (radius * Math.sin(incrementSize * i)) * 1000f ) / 1000f;

            for(int h = 0; h < pointsInCircle; h++) { // Left to right. Loops around.
                // pointsInCircle - 1 is to prevent a full loop.
                // Sets the degree/radian value here.
                // This is the value in radians it has incremented around the circle.
                // It should never reach pointsInCircle as that would be 360.
                float radVal = incrementSize * h;

                // Calculates the z and x coordinates. X is the radius of this circle. Y remains unchanged and unused.
                float zVal = Math.round(((xVal * Math.sin(radVal)) * 1000f)) / 1000f;
                float xValNew = Math.round(((xVal * Math.cos(radVal)) * 1000f)) / 1000f;

                // Ok! So these both assume the circle is at 0, 0, 0.
                // We can use this to our advantage and treat the values as the difference between the center and the point.
                // First, however, it defines the point.
                spherePoints.set(i + (pointsIncrease * h), new MeshVertex(center.x + xValNew, center.y + yVal, center.z + zVal, sizeOffset + i + (pointsIncrease * h)) );
                setCount++;
            }
        }

        // Vertices are just returned and added in the main class. SpherePoints is what to return.

        ArrayList<MeshIndex> sphereIndices = new ArrayList<>();

        MeshCore core = new MeshCore(center, false);

        // Now it sets indices.
        // Starts by setting all connections to the top and bottom.
        for(int i = 0; i < pointsInCircle; i++) {
            // Sets the ID it's connecting to the top point
            int ID = 1 + (i * pointsIncrease);
            // Sets the ID next to it. If it's the last number it connects to 1.
            // Otherwise, it connects to the next ID up.
            int nextID = (i == (pointsInCircle - 1) ? 1 : ID + pointsIncrease);
            // Adds the index.
            sphereIndices.add(new MeshIndex(spherePoints.get(0), spherePoints.get(ID), spherePoints.get(nextID), core));
        }

        // Same but with the bottom.
        for(int i = 0; i < pointsInCircle; i++) {
            // Sets the ID it's connecting to the top point
            int ID = pointsIncrease + (i * pointsIncrease);
            // Sets the ID next to it. If it's the last number it connects to 1.
            // Otherwise, it connects to the next ID up.
            int nextID = (i == (pointsInCircle - 1) ? pointsIncrease : ID + pointsIncrease);
            // Adds the index.
            sphereIndices.add(new MeshIndex(spherePoints.get(spherePoints.size() - 1), spherePoints.get(ID), spherePoints.get(nextID), core));
        }

        for(int h = 0; h < pointsIncrease - 1; h++) {
            for(int i = 0; i < pointsInCircle; i++) {
                // Sets the ID it's connecting to the top point
                int ID = (h + 1) + (i * pointsIncrease);
                int ID_P = (i == (pointsInCircle - 1) ? 1 + h : ID + pointsIncrease);
                int ID_D = ID + 1;
                int ID_P_D = ID_P + 1;
                sphereIndices.add(new MeshIndex(spherePoints.get(ID), spherePoints.get(ID_P), spherePoints.get(ID_D), core));
                sphereIndices.add(new MeshIndex(spherePoints.get(ID_P_D), spherePoints.get(ID_P), spherePoints.get(ID_D), core));
            }
        }

        return new AbstractMap.SimpleEntry<>(spherePoints, sphereIndices);
    }

    public AbstractMap.SimpleEntry<ArrayList<MeshVertex>, ArrayList<MeshIndex>> generatePlane
                    (int xCount, float xSpacing, int zCount, float zSpacing, int sizeOffset) {
        ArrayList<MeshVertex> vertices = new ArrayList<>();
        ArrayList<MeshIndex> indices = new ArrayList<>();

        // Centers the plane by going to the top left corner and getting its coordinates
        float xCoord = ((xCount - 1) * xSpacing) / 2f;
        float zCoord = -(((zCount - 1) * zSpacing) / 2f);

        int pointNum = sizeOffset;
        for(int x = 0; x < xCount; x++) {
            for(int z = 0; z < zCount; z++) {
                // Places the point
                vertices.add(new MeshVertex(xCoord, 0f, zCoord, pointNum));
                zCoord += zSpacing; // Increments the point.
                pointNum++; // Increments the point ID.
            }

            // Resets the coordinates
            xCoord -= xSpacing; // Minus as it goes down
            zCoord = -(((zCount - 1) * zSpacing) / 2f);
        }

        MeshCore core = new MeshCore(new MeshVertex(0f, -1f, 0f), false);


        int endSpot = (xCount * (zCount - 1)) - 1; // -1 at the end to prevent an extra loop. Slightly more efficient

        int id = 1; // Done to allow mod to check if it's on the last vertex in a row.
        for(MeshVertex vertex : vertices) {
            /*
            For each vertex passover it connects to:
            1. The vertex below it and the vertex below the one to right.
            2. The vertex to the right and the vertex below the one to the right

            UNLESS it is the final vertex in a row.
            */

            // To prevent it trying to connect to non-existent vertices when on the last row.
            if(endSpot < id)
                break;
            if(id % zCount != 0) {
                indices.add(new MeshIndex(
                        vertex,
                        vertices.get((id - 1) + zCount),
                        vertices.get(id + zCount),
                        core
                ));

                indices.add(new MeshIndex(
                        vertex,
                        vertices.get(id), // Due to the id starting at 1 this gets one to the right of the mesh vertex
                        vertices.get(id + zCount),
                        core
                        ));
            }

            id++;
        }


        return new AbstractMap.SimpleEntry<>(vertices, indices);
    }
}
