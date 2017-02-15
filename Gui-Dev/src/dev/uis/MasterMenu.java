/*
 * Copyright (C) 2017, Equilibrium Games - All Rights Reserved
 *
 * This source file is part of New Kosmos
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package dev.uis;

import dev.*;
import flounder.framework.*;
import flounder.guis.*;
import flounder.visual.*;

import java.util.*;

public class MasterMenu extends GuiComponent {
	protected static final float SLIDE_TIME = 0.75f;

	private MasterSlider masterSlider;

	private ValueDriver slideDriver;
	private float backgroundAlpha;
	private boolean displayed;

	public MasterMenu() {
		masterSlider = new MasterSlider(this);

		slideDriver = new ConstantDriver(0.0f);
		backgroundAlpha = 0.0f;
		displayed = false;

		addComponent(masterSlider, 0.0f, 0.0f, 1.0f, 1.0f);
	}

	public void display(boolean display) {
		if (displayed == display) {
			return;
		}

		displayed = display;
		masterSlider.show(displayed);

		if (display) {
			if (!isShown()) {
				show(true);
			}

			slideDriver = new SlideDriver(backgroundAlpha, 1.0f, SLIDE_TIME);
			((DevGuis) FlounderGuis.getGuiMaster()).getMasterOverlay().show(false);
		} else {
			slideDriver = new SlideDriver(backgroundAlpha, 0.0f, SLIDE_TIME);
			((DevGuis) FlounderGuis.getGuiMaster()).getMasterOverlay().show(true);
		}
	}

	public boolean isDisplayed() {
		return displayed;
	}

	public float getBlurFactor() {
		return backgroundAlpha;
	}

	public ValueDriver getSlideDriver() {
		return slideDriver;
	}

	public MasterSlider getMasterSlider() {
		return masterSlider;
	}

	@Override
	protected void updateSelf() {
		backgroundAlpha = slideDriver.update(Framework.getDelta());

		if (!displayed && !masterSlider.isShown() && backgroundAlpha == 0.0f) {
			show(false);
		}
	}

	@Override
	protected void getGuiTextures(List<GuiTexture> guiTextures) {
	}
}
