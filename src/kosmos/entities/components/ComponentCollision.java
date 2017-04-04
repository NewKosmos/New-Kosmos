/*
 * Copyright (C) 2017, Equilibrium Games - All Rights Reserved
 *
 * This source file is part of New Kosmos
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package kosmos.entities.components;

import flounder.entities.*;
import flounder.entities.components.*;
import flounder.helpers.*;
import flounder.maths.vectors.*;
import flounder.physics.*;

import javax.swing.*;

/**
 * Component that detects collision between two engine.entities.
 * <p>
 * Note: this component requires that both engine.entities have a ComponentCollider. Should one entity not have a ComponentCollider, then no collisions will be detected, because there is no collider to detect collisions against.
 */
public class ComponentCollision extends IComponentEntity implements IComponentMove, IComponentEditor {
	/**
	 * Creates a new ComponentCollision.
	 *
	 * @param entity The entity this component is attached to.
	 */
	public ComponentCollision(Entity entity) {
		super(entity);
	}

	@Override
	public void update() {
	}

	/**
	 * Resolves AABB collisions with any other CollisionComponents encountered.
	 *
	 * @param amount The amount attempting to be moved.
	 *
	 * @return New verifyMove vector that will not cause collisions after movement.
	 */
	public Vector3f resolveAABBCollisions(Vector3f amount) {
		Vector3f result = new Vector3f(amount.getX(), amount.getY(), amount.getZ());

		Collider collider1 = getEntity().getCollider();

		if (collider1 == null || !(collider1 instanceof AABB)) {
			return result;
		}

		final AABB collisionRange = AABB.stretch((AABB) collider1, null, amount); // The range in where there can be collisions!

		getEntity().visitInRange(ComponentCollision.class, collisionRange, (Entity entity, IComponentEntity component) -> {
			if (entity.equals(getEntity())) {
				return;
			}

			Collider collider2 = entity.getCollider();

			if (collider2 == null || !(collider2 instanceof AABB)) {
				return;
			}

			if (collider2.intersects(collisionRange).isIntersection()) {
				AABB.resolveCollision((AABB) collider1, (AABB) collider2, result, result);
			}
		});

		return result;
	}

	@Override
	public void verifyMove(Entity entity, Vector3f moveAmount, Vector3f rotateAmount) {
		moveAmount.set(resolveAABBCollisions(moveAmount));
		// rotateAmount = rotateAmount; // TODO: Stop some rotations?
	}

	@Override
	public void addToPanel(JPanel panel) {
	}

	@Override
	public void editorUpdate() {
	}

	@Override
	public Pair<String[], String[]> getSaveValues(String entityName) {
		return new Pair<>(
				new String[]{}, // Static variables
				new String[]{} // Class constructor
		);
	}

	@Override
	public void dispose() {
	}
}
