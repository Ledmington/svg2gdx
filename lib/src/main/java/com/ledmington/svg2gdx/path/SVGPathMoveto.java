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

/**
 * The SVG path element relative to 'moveto' commands. Official documentation available <a
 * href="https://www.w3.org/TR/SVG2/paths.html#PathDataMovetoCommands">here</a>.
 *
 * @param isRelative True if this is a relative 'moveto' command ("m"), false if it is an absolute 'moveto' command
 *     ("M").
 * @param initialPoint The first point of this command.
 * @param implicitLines All the points after the first one which all represent implicit 'lineto' commands. May be empty.
 */
public record SVGPathMoveto(boolean isRelative, SVGPathPoint initialPoint, List<SVGPathPoint> implicitLines)
        implements SVGPathElement {}
