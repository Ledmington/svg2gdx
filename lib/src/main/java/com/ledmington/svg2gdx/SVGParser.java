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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.ledmington.svg2gdx.path.SVGPath;
import com.ledmington.svg2gdx.path.SVGPathElement;
import com.ledmington.svg2gdx.path.SVGPathMoveTo;
import com.ledmington.svg2gdx.path.SVGPathPoint;
import com.ledmington.svg2gdx.path.SVGSubPath;
import com.ledmington.svg2gdx.util.CharacterIterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Parser of SVG images. */
public final class SVGParser {

    private SVGParser() {}

    /**
     * Parses the given file into an SVGImage instance.
     *
     * @param inputFile The .svg file to be parsed.
     * @return An SVGImage instance.
     */
    public static SVGImage parseImage(final File inputFile) {
        final Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputFile);
        } catch (final ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        doc.getDocumentElement().normalize();
        final Element root = doc.getDocumentElement();
        if (!root.getNodeName().equals("svg")) {
            throw new IllegalArgumentException(
                    String.format("Invalid root element: expected 'svg' but was '%s'", root.getNodeName()));
        }

        boolean hasViewBox = false;
        double viewBoxX = 0.0;
        double viewBoxY = 0.0;
        double viewBoxWidth = 0.0;
        double viewBoxHeight = 0.0;
        double imageWidth = 0.0;
        double imageHeight = 0.0;

        for (int i = 0; i < root.getAttributes().getLength(); i++) {
            final Node x = root.getAttributes().item(i);
            final String v = x.getNodeValue();
            switch (x.getNodeName()) {
                case "width" -> imageWidth = parseSize(v);
                case "height" -> imageHeight = parseSize(v);
                case "viewBox" -> {
                    hasViewBox = true;
                    final String[] splitted = v.split(" ");
                    if (splitted.length != 4) {
                        throw new IllegalArgumentException(String.format(
                                "Expected 4 arguments in viewBox attribute but were %d", splitted.length));
                    }
                    viewBoxX = Double.parseDouble(splitted[0]);
                    if (viewBoxX < 0.0) {
                        throw new IllegalArgumentException("Negative min-x value in viewBox");
                    }
                    viewBoxY = Double.parseDouble(splitted[1]);
                    if (viewBoxY < 0.0) {
                        throw new IllegalArgumentException("Negative min-y value in viewBox");
                    }
                    viewBoxWidth = Double.parseDouble(splitted[2]);
                    if (viewBoxWidth < 0.0) {
                        throw new IllegalArgumentException("Negative width value in viewBox");
                    }
                    viewBoxHeight = Double.parseDouble(splitted[3]);
                    if (viewBoxHeight < 0.0) {
                        throw new IllegalArgumentException("Negative height value in viewBox");
                    }
                }
                case "xmlns", "version" -> {
                    // intentionally ignored
                }
                default -> throw new IllegalArgumentException(String.format("Unknown attribute '%s'", x.getNodeName()));
            }
        }

        if (!hasViewBox) {
            viewBoxWidth = imageWidth;
            viewBoxHeight = imageHeight;
        }

        final SVGViewBox vb = new SVGViewBox(viewBoxX, viewBoxY, viewBoxWidth, viewBoxHeight);

        final SVGPalette.SVGPaletteBuilder palette = SVGPalette.builder();

        final List<SVGElement> elements = new ArrayList<>();
        final NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node node = children.item(i);
            switch (node.getNodeName()) {
                case "rect":
                    elements.add(parseRectangle(node, palette));
                    break;
                case "path":
                    elements.add(parsePath(node, palette));
                    break;
                case "defs":
                case "metadata":
                case "#text":
                case "title":
                case "desc":
                    // we don't care about these
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Unknown element with name '%s'", node.getNodeName()));
            }
        }

        return new SVGImage(vb, imageWidth, imageHeight, palette.build(), elements);
    }

    private static double parseSize(final String input) {
        if (input.length() < 2) {
            return Double.parseDouble(input);
        }

        // Conversion constants taken from
        // https://oreillymedia.github.io/Using_SVG/guide/units.html#units-absolute-reference
        final double inchesToPixels = 96.0;
        final double centimetersToPixels = 37.795;
        final double millimetersToPixels = 3.7795;
        final double pointsToPixels = 1.3333;
        final double picasToPixels = 16.0;

        final String s = input.substring(0, input.length() - 2);

        if (input.endsWith("in")) {
            return Double.parseDouble(s) * inchesToPixels;
        } else if (input.endsWith("cm")) {
            return Double.parseDouble(s) * centimetersToPixels;
        } else if (input.endsWith("mm")) {
            return Double.parseDouble(s) * millimetersToPixels;
        } else if (input.endsWith("pt")) {
            return Double.parseDouble(s) * pointsToPixels;
        } else if (input.endsWith("pc")) {
            return Double.parseDouble(s) * picasToPixels;
        } else if (input.endsWith("px")) {
            return Double.parseDouble(s);
        } else {
            return Double.parseDouble(input);
        }
    }

    private static Map<String, String> parseStyle(final String style) {
        final Map<String, String> m = new HashMap<>();
        final String[] styles = style.split(";");
        for (final String s : styles) {
            final int idx = s.indexOf(':');
            final String key = s.substring(0, idx);
            switch (key) {
                case "fill", "fill-opacity", "stroke", "display" -> {}
                default -> throw new IllegalArgumentException(String.format("Unknown style element '%s'", key));
            }
            m.put(key, s.substring(idx + 1));
        }
        return m;
    }

    private static SVGRectangle parseRectangle(final Node node, final SVGPalette.SVGPaletteBuilder palette) {
        if (node.getChildNodes().getLength() != 0) {
            throw new IllegalArgumentException("Weird 'rect' element with more than 0 child nodes.");
        }

        double x = 0.0;
        double y = 0.0;
        double width = 0.0;
        double height = 0.0;
        SVGColor fill = new SVGColor();
        SVGColor stroke = new SVGColor();
        double strokeWidth = 0.0;

        for (int i = 0; i < node.getAttributes().getLength(); i++) {
            final Node n = node.getAttributes().item(i);
            final String v = n.getNodeValue();

            switch (n.getNodeName()) {
                case "x" -> x = parseSize(v);
                case "y" -> y = parseSize(v);
                case "width" -> width = parseSize(v);
                case "height" -> height = parseSize(v);
                case "fill" -> fill = parseColor(v);
                case "stroke" -> stroke = parseColor(v);
                case "stroke-width" -> strokeWidth = parseSize(v);
                default -> throw new IllegalArgumentException(String.format("Unknown attribute '%s'", n.getNodeName()));
            }
        }

        return new SVGRectangle(x, y, width, height, fill, stroke, strokeWidth);
    }

    private static SVGPath parsePath(final Node node, final SVGPalette.SVGPaletteBuilder palette) {
        if (node.getChildNodes().getLength() != 0) {
            throw new IllegalArgumentException("Weird 'path' element with more than 0 child nodes.");
        }

        SVGPath path = null;

        for (int i = 0; i < node.getAttributes().getLength(); i++) {
            final Node n = node.getAttributes().item(i);
            final String v = n.getNodeValue();

            switch (n.getNodeName()) {
                case "d" -> path = parsePath(v);
                default -> throw new IllegalArgumentException(String.format("Unknown attribute '%s'", n.getNodeName()));
            }
        }

        return null;
    }

    /** @deprecated Use the new 'parseNumber' method. */
    @Deprecated(forRemoval = true)
    private static SVGPathPoint parsePathPoint(final String pointData) {
        final int idx = pointData.indexOf(',');
        return new SVGPathPoint(
                Double.parseDouble(pointData.substring(0, idx)), Double.parseDouble(pointData.substring(idx + 1)));
    }

    private static SVGPath parsePath(final String pathString) {
        final List<SVGSubPath> subpaths = new ArrayList<>();
        final CharacterIterator it = new CharacterIterator(pathString);

        while (it.hasNext()) {

            it.skipSpaces();

            if (!it.hasNext()) {
                break;
            }

            final SVGSubPath subpath = parseSubPath(it);
            subpaths.add(subpath);
        }

        return new SVGPath(subpaths);
    }

    private static SVGSubPath parseSubPath(final CharacterIterator it) {
        // here it is assumed that it points to a non-whitespace character

        final List<SVGPathElement> subPathElements = new ArrayList<>();

        if (it.current() != 'm' && it.current() != 'M') {
            throw new IllegalArgumentException(
                    String.format("Invalid subpath data: must start with 'm' or 'M' but was '%s'", it.current()));
        }

        while (it.hasNext()) {
            final char curr = it.current();

            final SVGPathElement elem =
                    switch (curr) {
                        case 'm', 'M' -> {
                            it.move();
                            yield parseMoveTo(it, curr == 'm');
                        }
                        default -> throw new IllegalArgumentException(
                                String.format("Unexpected character in path '%c' (U+%04x)", curr, (int) curr));
                    };

            subPathElements.add(elem);

            // if (elem.equals("m") || elem.equals("M")) {
            // // Relative/Absolute "moveto" command
            // final boolean isRelative = elem.equals("m");
            // final List<SVGPathPoint> points = new ArrayList<>();
            // for (; i + 1 < pathData.length && pathData[i + 1].indexOf(',') != -1; i++) {
            // points.add(parsePathPoint(pathData[i + 1]));
            // }
            // subPathElements.add(new SVGPathMoveTo(isRelative, points));
            // } else if (elem.equals("c") || elem.equals("C")) {
            // // Relative/Absolute "Bezier" command
            // final boolean isRelative = elem.equals("c");
            // final List<SVGPathBezierElement> elements = new ArrayList<>();
            // for (; i + 3 < pathData.length && pathData[i + 1].indexOf(',') != -1; i += 3)
            // {
            // elements.add(new SVGPathBezierElement(
            // parsePathPoint(pathData[i + 1]),
            // parsePathPoint(pathData[i + 2]),
            // parsePathPoint(pathData[i + 3])));
            // }
            // subPathElements.add(new SVGPathBezier(isRelative, elements));
            // } else if (elem.equals("l") || elem.equals("L")) {
            // // Relative/Absolute "lineto" command
            // final boolean isRelative = elem.equals("l");
            // final List<SVGPathPoint> points = new ArrayList<>();
            // for (; i + 1 < pathData.length && pathData[i + 1].indexOf(',') != -1; i++) {
            // points.add(parsePathPoint(pathData[i + 1]));
            // }
            // subPathElements.add(new SVGPathLineto(isRelative, points));
            // } else if (elem.equals("z") || elem.equals("Z")) {
            // // "closepath" commands
            // // reference: https://www.w3.org/TR/SVG2/paths.html#PathDataClosePathCommand
            // break;
            // } else {
            // throw new IllegalArgumentException(String.format("Unknown subpath element
            // '%s'", elem));
            // }
        }

        return new SVGSubPath(subPathElements);
    }

    private static double parseNumber(final CharacterIterator it) {
        final StringBuilder sb = new StringBuilder();
        for (; it.hasNext(); it.move()) {
            final char c = it.current();
            if (Character.isDigit(c) || c == '+' || c == '-' || c == 'e' || c == 'E' || c == '.') {
                sb.append(c);
            } else {
                break;
            }
        }
        return Double.parseDouble(sb.toString());
    }

    private static SVGPathMoveTo parseMoveTo(final CharacterIterator it, final boolean isRelative) {
        it.skipSpaces();

        final List<SVGPathPoint> points = new ArrayList<>();

        while (it.hasNext() && Character.isDigit(it.current())) {
            final double x = parseNumber(it);
            it.skipSpacesAndCommas();
            final double y = parseNumber(it);
            it.skipSpaces();
            points.add(new SVGPathPoint(x, y));
        }

        return new SVGPathMoveTo(isRelative, points);
    }

    private static SVGColor parseColor(final String v) {
        return switch (v) {
            case "none" -> new SVGColor();
            case "red" -> new SVGColor((byte) 0xff, (byte) 0, (byte) 0, (byte) 0xff);
            case "blue" -> new SVGColor((byte) 0, (byte) 0, (byte) 0xff, (byte) 0xff);
            default -> {
                if (v.charAt(0) == '#' && v.length() == 7) {
                    final byte r = ParseUtils.parseByteHex(v.substring(1, 3));
                    final byte g = ParseUtils.parseByteHex(v.substring(3, 5));
                    final byte b = ParseUtils.parseByteHex(v.substring(5, 7));
                    yield new SVGColor(r, g, b, (byte) 0xff);
                }
                throw new IllegalArgumentException(String.format("Unknown color '%s'", v));
            }
        };
    }
}
