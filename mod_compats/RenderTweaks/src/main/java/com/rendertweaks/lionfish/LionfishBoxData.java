package com.rendertweaks.lionfish;

import java.lang.reflect.Field;
import org.joml.Vector3f;

/**
 * Flattens a Lionfish {@code ModelBox} into one float array, once per box. Its quad and vertex
 * classes are package-private, so they are read by reflection here and never touched per frame.
 *
 * <p>Layout per quad: normal x, y, z, then 4 vertices of x, y, z (already divided by 16), u, v.
 */
public final class LionfishBoxData {

    public static final int FLOATS_PER_VERTEX = 5;
    public static final int FLOATS_PER_QUAD = 3 + 4 * FLOATS_PER_VERTEX;

    private static final String TOOLS = "com.github.L_Ender.lionfishapi.client.model.tools.LionfishModelRenderUtils$";

    private static Field quads;
    private static Field quadNormal;
    private static Field quadVertices;
    private static Field vertexPosition;
    private static Field vertexU;
    private static Field vertexV;

    private LionfishBoxData() {}

    public static float[] build(Object box) throws ReflectiveOperationException {
        if (quads == null) {
            resolveFields();
        }
        Object[] boxQuads = (Object[]) quads.get(box);
        float[] data = new float[boxQuads.length * FLOATS_PER_QUAD];
        int o = 0;
        for (Object quad : boxQuads) {
            Vector3f normal = (Vector3f) quadNormal.get(quad);
            data[o++] = normal.x();
            data[o++] = normal.y();
            data[o++] = normal.z();
            Object[] vertices = (Object[]) quadVertices.get(quad);
            if (vertices.length != 4) {
                throw new IllegalStateException("Lionfish quad with " + vertices.length + " vertices");
            }
            for (Object vertex : vertices) {
                Vector3f position = (Vector3f) vertexPosition.get(vertex);
                data[o++] = position.x() / 16.0F;
                data[o++] = position.y() / 16.0F;
                data[o++] = position.z() / 16.0F;
                data[o++] = vertexU.getFloat(vertex);
                data[o++] = vertexV.getFloat(vertex);
            }
        }
        return data;
    }

    private static void resolveFields() throws ReflectiveOperationException {
        Class<?> quad = Class.forName(TOOLS + "TexturedQuad");
        Class<?> vertex = Class.forName(TOOLS + "PositionTextureVertex");
        quadNormal = accessible(quad.getDeclaredField("normal"));
        quadVertices = accessible(quad.getDeclaredField("vertexPositions"));
        vertexPosition = accessible(vertex.getDeclaredField("position"));
        vertexU = accessible(vertex.getDeclaredField("textureU"));
        vertexV = accessible(vertex.getDeclaredField("textureV"));
        // Assigned last: quads doubles as the "resolved" flag, so it is only set once all others are.
        quads = accessible(Class.forName(TOOLS + "ModelBox").getDeclaredField("quads"));
    }

    private static Field accessible(Field field) {
        field.setAccessible(true);
        return field;
    }
}
