/*
 * svg2gdx - A converter from SVG to libGDX ShapeRenderer code.
 * Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class ShowcaseScreen implements Screen {

    private final Camera camera;
    private final ShapeRenderer sr;
    private final Consumer<ShapeRenderer> drawImage;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Not needed here")
    public ShowcaseScreen(final Camera camera, final ShapeRenderer sr, final Consumer<ShapeRenderer> drawImage) {
        this.camera = Objects.requireNonNull(camera);
        this.sr = Objects.requireNonNull(sr);
        this.drawImage = Objects.requireNonNull(drawImage);
    }

    @Override
    public void show() {
        // intentionally empty
    }

    @Override
    public void render(final float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        this.camera.update();
        this.drawImage.accept(this.sr);
    }

    @Override
    public void hide() {
        // intentionally empty
    }

    @Override
    public void resize(final int width, final int height) {
        // intentionally empty
    }

    @Override
    public void pause() {
        // intentionally empty
    }

    @Override
    public void resume() {
        // intentionally empty
    }

    @Override
    public void dispose() {
        // intentionally empty
    }
}
