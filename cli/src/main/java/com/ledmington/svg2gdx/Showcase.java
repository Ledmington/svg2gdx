/*
 * svg2gdx - A converter from SVG to libGDX ShapeRenderer code.
 * Copyright (C) 2023-2025 Filippo Barbari <filippo.barbari@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ledmington.svg2gdx;

import java.util.Objects;
import java.util.function.Consumer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class Showcase extends Game {

    private final Consumer<ShapeRenderer> drawImage;
    private final int width;
    private final int height;
    private final Color background;
    private ShapeRenderer shapeRenderer;

    private Showcase(
            final int width, final int height, final Color background, final Consumer<ShapeRenderer> drawImage) {
        this.drawImage = Objects.requireNonNull(drawImage);
        this.width = width;
        this.height = height;
        this.background = Objects.requireNonNull(background);
    }

    public static void run(
            final int width, final int height, final Color background, final Consumer<ShapeRenderer> drawImage) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setForegroundFPS(60);
        config.setIdleFPS(30);
        config.setResizable(true);
        config.setTitle("svg2gdx - Showcase");
        config.useVsync(true);
        config.setDecorated(true);
        config.disableAudio(true);
        config.setWindowedMode(width, height);

        new Lwjgl3Application(new Showcase(width, height, background, drawImage), config);
    }

    @Override
    public void create() {
        final OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, this.width, this.height);
        this.shapeRenderer = new ShapeRenderer();
        this.setScreen(new ShowcaseScreen(camera, this.shapeRenderer, this.background, this.drawImage));
    }

    @Override
    public void dispose() {
        super.dispose();
        this.shapeRenderer.dispose();
    }
}
