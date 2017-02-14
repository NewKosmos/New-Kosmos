/*
 * Copyright (C) 2017, Equilibrium Games - All Rights Reserved
 *
 * This source file is part of New Kosmos
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package kosmos.water;

import flounder.loaders.*;
import flounder.maths.*;
import flounder.maths.matrices.*;
import flounder.maths.vectors.*;
import flounder.physics.*;
import flounder.processing.*;
import flounder.processing.opengl.*;
import kosmos.chunks.*;

import java.util.*;

/**
 * Represents the physical mesh for all the water at a certain height in the scene.
 */
public class Water {
	protected static final float COLOUR_INTENSITY = 0.75f; // 0 being 100% reflective, 0% disables reflections.

	protected static final float WAVE_SPEED = 10.0f;
	protected static final float WAVE_LENGTH = 5.0f;
	protected static final float AMPLITUDE = 0.50f;

	protected static final float SQUARE_SIZE = 0.500f * (float) Math.sqrt(3.0) * ChunkGenerator.HEXAGON_SIDE_LENGTH;
	protected static final int VERTEX_COUNT = 35; // Should create a AABB of size 'ChunkGenerator.CHUNK_WORLD_SIZE'.

	private int vao;
	private int vertexCount;
	private boolean loaded;

	private AABB aabb;
	private Colour colour;

	private Vector3f position;
	private Vector3f rotation;
	private float scale;
	private Matrix4f modelMatrix;

	/**
	 * Generates a new water mesh.
	 *
	 * @param position The position of the water plane.
	 * @param rotation The rotation of the water plane.
	 * @param scale The scale of the water plane.
	 */
	public Water(Vector3f position, Vector3f rotation, float scale) {
		this.vao = 0;
		this.vertexCount = 0;
		this.loaded = false;

		this.aabb = new AABB(new Vector3f(0.0f, -1.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f));
		this.colour = new Colour(0.2392f, 0.6824f, 1.0f, COLOUR_INTENSITY);

		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
		this.modelMatrix = new Matrix4f();

		/*FlounderEvents.addEvent(new IEvent() {
			private KeyButton keyButton = new KeyButton(GLFW.GLFW_KEY_I);

			@Override
			public boolean eventTriggered() {
				return keyButton.wasDown();
			}

			@Override
			public void onEvent() {
				if (colour.a == 1.0f) {
					colour.a = COLOUR_INTENSITY;
				} else {
					colour.a = 1.0f;
				}
			}
		});*/

		generateMesh();
	}

	/**
	 * Generates the water mesh and loads it to a VAO.
	 * Generates the {@code float[]} of vertex data which will be loaded to the water's VAO.
	 * The array contains the vertex x and z positions as well as an encoded indication of which corner of its triangle each vertex lies in.
	 */
	private void generateMesh() {
		List<Float> vertices = new ArrayList<>();

		for (int col = 0; col < Water.VERTEX_COUNT - 1; col++) {
			for (int row = 0; row < Water.VERTEX_COUNT - 1; row++) {
				int topLeft = (row * Water.VERTEX_COUNT) + col;
				int topRight = topLeft + 1;
				int bottomLeft = ((row + 1) * Water.VERTEX_COUNT) + col;
				int bottomRight = bottomLeft + 1;

				if (row % 2 == 0) {
					storeQuad1(vertices, topLeft, topRight, bottomLeft, bottomRight, col % 2 == 0);
				} else {
					storeQuad2(vertices, topLeft, topRight, bottomLeft, bottomRight, col % 2 == 0);
				}
			}
		}

		float[] array = new float[vertices.size()];

		for (int i = 0; i < array.length; i++) {
			array[i] = vertices.get(i);
		}

		vertexCount = array.length / 3;
		FlounderProcessors.sendRequest((RequestOpenGL) () -> {
			vao = FlounderLoader.createInterleavedVAO(array, 3);
			loaded = true;
		});

		position.x -= aabb.getMaxExtents().x / 2.0f;
		position.z -= aabb.getMaxExtents().z / 2.0f;
		AABB.recalculate(aabb, position, rotation, scale, aabb);

		//System.out.println(VERTEX_COUNT);
		//System.out.println(aabb);
		//System.out.println(ChunkGenerator.CHUNK_WORLD_SIZE);
	}

	private void storeQuad1(List<Float> vertices, int topLeft, int topRight, int bottomLeft, int bottomRight, boolean mixed) {
		storeVertex(vertices, topLeft, new Vector2f(0.0f, 1.0f), mixed ? new Vector2f(1.0f, 0.0f) : new Vector2f(1.0f, 1.0f));
		storeVertex(vertices, bottomLeft, mixed ? new Vector2f(1.0f, -1.0f) : new Vector2f(1.0f, 0.0f), new Vector2f(0.0f, -1.0f));

		if (mixed) {
			storeVertex(vertices, topRight, new Vector2f(-1.0f, 0.0f), new Vector2f(-1.0f, 1.0f));
		} else {
			storeVertex(vertices, bottomRight, new Vector2f(-1.0f, -1.0f), new Vector2f(-1.0f, 0.0f));
		}

		storeVertex(vertices, bottomRight, new Vector2f(0.0f, -1.0f), mixed ? new Vector2f(-1.0f, 0.0f) : new Vector2f(-1.0f, -1.0f));
		storeVertex(vertices, topRight, mixed ? new Vector2f(-1.0f, 1.0f) : new Vector2f(-1.0f, 0.0f), new Vector2f(0.0f, 1.0f));

		if (mixed) {
			storeVertex(vertices, bottomLeft, new Vector2f(1.0f, 0.0f), new Vector2f(1.0f, -1.0f));
		} else {
			storeVertex(vertices, topLeft, new Vector2f(1.0f, 1.0f), new Vector2f(1.0f, 0.0f));
		}
	}

	private void storeQuad2(List<Float> vertices, int topLeft, int topRight, int bottomLeft, int bottomRight, boolean mixed) {
		storeVertex(vertices, topRight, new Vector2f(-1.0f, 0.0f), mixed ? new Vector2f(0.0f, 1.0f) : new Vector2f(-1.0f, 1.0f));
		storeVertex(vertices, topLeft, mixed ? new Vector2f(1.0f, 1.0f) : new Vector2f(0.0f, 1.0f), new Vector2f(1.0f, 0.0f));

		if (mixed) {
			storeVertex(vertices, bottomRight, new Vector2f(0.0f, -1.0f), new Vector2f(-1.0f, -1.0f));
		} else {
			storeVertex(vertices, bottomLeft, new Vector2f(1.0f, -1.0f), new Vector2f(0.0f, -1.0f));
		}

		storeVertex(vertices, bottomLeft, new Vector2f(1.0f, 0.0f), mixed ? new Vector2f(0.0f, -1.0f) : new Vector2f(1.0f, -1.0f));
		storeVertex(vertices, bottomRight, mixed ? new Vector2f(-1.0f, -1.0f) : new Vector2f(0.0f, -1.0f), new Vector2f(-1.0f, 0.0f));

		if (mixed) {
			storeVertex(vertices, topLeft, new Vector2f(0.0f, 1.0f), new Vector2f(1.0f, 1.0f));
		} else {
			storeVertex(vertices, topRight, new Vector2f(-1.0f, 1.0f), new Vector2f(0.0f, 1.0f));
		}
	}

	private void storeVertex(List<Float> vertices, int index, Vector2f otherPoint1, Vector2f otherPoint2) {
		int gridX = index % Water.VERTEX_COUNT;
		int gridZ = index / Water.VERTEX_COUNT;
		float x = gridX * Water.SQUARE_SIZE;
		float z = gridZ * Water.SQUARE_SIZE;

		if (x > aabb.getMaxExtents().x) {
			aabb.getMaxExtents().x = x;
		} else if (x < aabb.getMinExtents().x) {
			aabb.getMinExtents().x = x;
		}

		if (z > aabb.getMaxExtents().z) {
			aabb.getMaxExtents().z = z;
		} else if (z < aabb.getMinExtents().z) {
			aabb.getMinExtents().z = z;
		}

		vertices.add(x);
		vertices.add(z);
		vertices.add(encode(otherPoint1.x, otherPoint1.y, otherPoint2.x, otherPoint2.y));
	}

	/**
	 * Encodes the position of 2 vertices in a triangle (relative to the other vertex) into a single float.
	 *
	 * @param x Relative x position of first other vertex.
	 * @param z Relative z position of first other vertex.
	 * @param x2 Relative x position of second other vertex.
	 * @param z2 Relative z position of second other vertex.
	 *
	 * @return The encoded float.
	 */
	private float encode(float x, float z, float x2, float z2) {
		float p3 = (x + 1.0f) * 27.0f;
		float p2 = (z + 1.0f) * 9.0f;
		float p1 = (x2 + 1.0f) * 3.0f;
		float p0 = (z2 + 1.0f) * 1.0f;
		return p0 + p1 + p2 + p3;
	}

	/**
	 * @return The VAO's ID.
	 */
	protected int getVao() {
		return vao;
	}

	/**
	 * @return The number of vertices stored in the VAO.
	 */
	protected int getVertexCount() {
		return vertexCount;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public Colour getColour() {
		return colour;
	}

	public AABB getAABB() {
		return aabb;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public Vector3f getRotation() {
		return rotation;
	}

	public void setRotation(Vector3f rotation) {
		this.rotation = rotation;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public Matrix4f getModelMatrix() {
		modelMatrix.setIdentity();
		Matrix4f.transformationMatrix(position, rotation, scale, modelMatrix);
		return modelMatrix;
	}

	public void delete() {
		FlounderProcessors.sendRequest((RequestOpenGL) () -> {
			loaded = false;
			FlounderLoader.deleteVAOFromCache(vao);
		});
	}
}
