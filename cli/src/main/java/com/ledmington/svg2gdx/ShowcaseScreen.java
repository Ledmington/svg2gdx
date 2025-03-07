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

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public final class ShowcaseScreen implements Screen {

    private final Camera camera;
    private final ShapeRenderer sr;
    private final Consumer<ShapeRenderer> drawImage;
    private final Color background;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Not needed here")
    public ShowcaseScreen(
            final Camera camera,
            final ShapeRenderer sr,
            final Color background,
            final Consumer<ShapeRenderer> drawImage) {
        this.camera = Objects.requireNonNull(camera);
        this.sr = Objects.requireNonNull(sr);
        this.background = Objects.requireNonNull(background);
        this.drawImage = Objects.requireNonNull(drawImage);
    }

    @Override
    public void show() {
        // intentionally empty
    }

    @Override
    public void render(final float delta) {
        ScreenUtils.clear(this.background, true);

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
        sr.dispose();
    }
}
