/*
 * svg2gdx - A converter from SVG to LibGDX ShapeRenderer code.
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

public class Main {
    public static void main(final String[] args) {
        if (args.length < 1) {
            System.out.println("Expected the name of the SVG image to convert.");
            System.exit(-1);
        }

        final SVGImage parsed = new SVGImage(args[0]);
        System.out.println(parsed.toGDXShapeRenderer());
    }
}
