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
package com.ledmington.svg2gdx.path;

import java.util.List;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.ledmington.svg2gdx.SVGElement;
import com.ledmington.svg2gdx.SVGPalette;

public record SVGPath(String colorName, List<SVGSubPath> subpaths) implements SVGElement {
    public void draw(final ShapeRenderer sr, final SVGPalette palette) {
        for (final SVGSubPath subpath : subpaths) {
            subpath.draw(
                    sr, palette, colorName, ((SVGPathMoveto) subpath.elements().getFirst()).initialPoint());
        }
    }

    @Override
    public String toGDXShapeRenderer() {
        throw new Error("Not implemented");
    }
}
