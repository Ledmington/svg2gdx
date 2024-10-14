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
package com.ledmington.svg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.ledmington.svg.path.Arc;
import com.ledmington.svg.path.ArcElement;
import com.ledmington.svg.path.CubicBezier;
import com.ledmington.svg.path.CubicBezierElement;
import com.ledmington.svg.path.HorizontalLineTo;
import com.ledmington.svg.path.LineTo;
import com.ledmington.svg.path.MoveTo;
import com.ledmington.svg.path.Path;
import com.ledmington.svg.path.PathElement;
import com.ledmington.svg.path.Point;
import com.ledmington.svg.path.QuadraticBezier;
import com.ledmington.svg.path.QuadraticBezierElement;
import com.ledmington.svg.path.SmoothCubicBezier;
import com.ledmington.svg.path.SmoothCubicBezierElement;
import com.ledmington.svg.path.SmoothQuadraticBezier;
import com.ledmington.svg.path.SmoothQuadraticBezierElement;
import com.ledmington.svg.path.SubPath;
import com.ledmington.util.CharacterIterator;
import com.ledmington.util.ParseUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/** Parser of SVG images. */
public final class Parser {

    private Parser() {}

    /**
     * Parses the given file into an SVGImage instance.
     *
     * @param inputFile The .svg file to be parsed.
     * @return An SVGImage instance.
     */
    public static Image parseImage(final File inputFile) {
        final Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputFile);
        } catch (final ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        doc.getDocumentElement().normalize();
        final org.w3c.dom.Element root = doc.getDocumentElement();
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
                case "preserveAspectRatio", "style" -> {
                    // ignored for now
                }
                case "xmlns", "version", "contentScriptType", "contentStyleType", "xml:space", "xmlns:xlink" -> {
                    // intentionally ignored
                }
                default -> throw new IllegalArgumentException(String.format("Unknown attribute '%s'", x.getNodeName()));
            }
        }

        if (!hasViewBox) {
            viewBoxWidth = imageWidth;
            viewBoxHeight = imageHeight;
        }

        final ViewBox vb = new ViewBox(viewBoxX, viewBoxY, viewBoxWidth, viewBoxHeight);

        return new Image(vb, imageWidth, imageHeight, parseChildren(root));
    }

    private static List<Element> parseChildren(final Node root) {
        final List<Element> elements = new ArrayList<>();
        for (int i = 0; i < root.getChildNodes().getLength(); i++) {
            final Node node = root.getChildNodes().item(i);
            switch (node.getNodeName()) {
                case "rect":
                    elements.add(parseRectangle(node));
                    break;
                case "path":
                    elements.add(parsePath(node));
                    break;
                case "polyline":
                    elements.add(parsePolyline(node));
                    break;
                case "circle":
                    elements.add(parseCircle(node));
                    break;
                case "g":
                    elements.add(parseGroup(node));
                    break;
                case "defs":
                case "metadata":
                case "#text":
                case "style": // ignored for now
                case "text": // ignored for now
                case "title":
                case "desc":
                case "#comment":
                    // we don't care about these
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Unknown element with name '%s'", node.getNodeName()));
            }
        }
        return elements;
    }

    private static Group parseGroup(final Node node) {
        Color fill = new Color((byte) 0, (byte) 0, (byte) 0, (byte) 0);
        Color stroke = new Color((byte) 0, (byte) 0, (byte) 0, (byte) 0);
        double strokeWidth = 0.0;

        for (int i = 0; i < node.getAttributes().getLength(); i++) {
            final Node n = node.getAttributes().item(i);
            final String v = n.getNodeValue();

            switch (n.getNodeName()) {
                case "fill" -> fill = parseColor(v);
                case "stroke" -> stroke = parseColor(v);
                case "stroke-width" -> strokeWidth = Double.parseDouble(v);
                default -> throw new IllegalArgumentException(String.format("Unknown attribute '%s'", n.getNodeName()));
            }
        }

        return new Group(new Style(fill, stroke, strokeWidth), parseChildren(node));
    }

    private static Circle parseCircle(final Node node) {
        if (node.getChildNodes().getLength() != 0) {
            throw new IllegalArgumentException("Weird 'circle' element with more than 0 child nodes.");
        }

        double cx = 0.0;
        double cy = 0.0;
        double r = 0.0;

        for (int i = 0; i < node.getAttributes().getLength(); i++) {
            final Node n = node.getAttributes().item(i);
            final String v = n.getNodeValue();

            switch (n.getNodeName()) {
                case "cx" -> cx = Double.parseDouble(v);
                case "cy" -> cy = Double.parseDouble(v);
                case "r" -> r = Double.parseDouble(v);
                case "class" -> {
                    // ignored for now
                }
                default -> throw new IllegalArgumentException(String.format("Unknown attribute '%s'", n.getNodeName()));
            }
        }

        return new Circle(cx, cy, r);
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

    private static Rectangle parseRectangle(final Node node) {
        if (node.getChildNodes().getLength() != 0) {
            throw new IllegalArgumentException("Weird 'rect' element with more than 0 child nodes.");
        }

        double x = 0.0;
        double y = 0.0;
        double width = 0.0;
        double height = 0.0;
        Color fill = new Color();
        Color stroke = new Color();
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
                case "class" -> {
                    // ignored for now
                }
                default -> throw new IllegalArgumentException(String.format("Unknown attribute '%s'", n.getNodeName()));
            }
        }

        return new Rectangle(x, y, width, height, fill, stroke, strokeWidth);
    }

    private static Polyline parsePolyline(final Node node) {
        if (node.getChildNodes().getLength() != 0) {
            throw new IllegalArgumentException("Weird 'polyline' element with more than 0 child nodes.");
        }

        List<Point> points = null;

        for (int i = 0; i < node.getAttributes().getLength(); i++) {
            final Node n = node.getAttributes().item(i);
            final String v = n.getNodeValue();

            switch (n.getNodeName()) {
                case "points" -> points = parsePoints(v);
                case "class" -> {
                    // ignored for now
                }
                default -> throw new IllegalArgumentException(String.format("Unknown attribute '%s'", n.getNodeName()));
            }
        }

        return new Polyline(points);
    }

    private static Point parsePoint(final CharacterIterator it) {
        final double x = parseNumber(it);
        it.skipSpacesAndCommas();
        final double y = parseNumber(it);
        it.skipSpaces();
        return new Point(x, y);
    }

    private static List<Point> parsePoints(final String v) {
        final CharacterIterator it = new CharacterIterator(v);
        final List<Point> points = new ArrayList<>();
        it.skipSpaces();
        while (it.hasNext() && (Character.isDigit(it.current()) || it.current() == '+' || it.current() == '-')) {
            points.add(parsePoint(it));
        }
        return points;
    }

    private static Path parsePath(final Node node) {
        if (node.getChildNodes().getLength() != 0) {
            throw new IllegalArgumentException("Weird 'path' element with more than 0 child nodes.");
        }

        List<SubPath> subpaths = null;
        Color fill = new Color();
        Color stroke = new Color();
        double strokeWidth = 0.0;

        for (int i = 0; i < node.getAttributes().getLength(); i++) {
            final Node n = node.getAttributes().item(i);
            final String v = n.getNodeValue();

            switch (n.getNodeName()) {
                case "d" -> subpaths = parsePath(v);
                case "fill" -> fill = parseColor(v);
                case "stroke" -> stroke = parseColor(v);
                case "stroke-width" -> strokeWidth = parseSize(v);
                case "class" -> {
                    // ignored for now
                }
                default -> throw new IllegalArgumentException(String.format("Unknown attribute '%s'", n.getNodeName()));
            }
        }

        return new Path(subpaths, fill, stroke, strokeWidth);
    }

    private static List<SubPath> parsePath(final String pathString) {
        final List<SubPath> subpaths = new ArrayList<>();
        final CharacterIterator it = new CharacterIterator(pathString);

        while (it.hasNext()) {
            it.skipSpaces();
            if (!it.hasNext()) {
                break;
            }
            final SubPath subpath = parseSubPath(it);
            subpaths.add(subpath);
        }

        return subpaths;
    }

    private static SubPath parseSubPath(final CharacterIterator it) {
        // here it is assumed that it points to a non-whitespace character

        final List<PathElement> subPathElements = new ArrayList<>();

        if (it.current() != 'm' && it.current() != 'M') {
            throw new IllegalArgumentException(
                    String.format("Invalid subpath data: must start with 'm' or 'M' but was '%s'", it.current()));
        }

        while (it.hasNext()) {
            final char curr = it.current();

            switch (curr) {
                case 'm', 'M' -> {
                    it.move();
                    subPathElements.add(parseMoveTo(it, curr == 'm'));
                }
                case 'l', 'L' -> {
                    it.move();
                    subPathElements.add(parseLineTo(it, curr == 'l'));
                }
                case 'h', 'H' -> {
                    it.move();
                    subPathElements.add(parseHorizontalLineTo(it, curr == 'h'));
                }
                case 'c', 'C' -> {
                    it.move();
                    subPathElements.add(parseCubicBezier(it, curr == 'c'));
                }
                case 's', 'S' -> {
                    it.move();
                    subPathElements.add(parseSmoothCubicBezier(it, curr == 's'));
                }
                case 'q', 'Q' -> {
                    it.move();
                    subPathElements.add(parseQuadraticBezier(it, curr == 'q'));
                }
                case 't', 'T' -> {
                    it.move();
                    subPathElements.add(parseSmoothQuadraticBezier(it, curr == 't'));
                }
                case 'a', 'A' -> {
                    it.move();
                    subPathElements.add(parseArc(it, curr == 'a'));
                }
                case 'z', 'Z' -> {
                    it.move();
                    return new SubPath(subPathElements);
                }
                default -> throw new IllegalArgumentException(
                        String.format("Unexpected character in path '%c' (U+%04x)", curr, (int) curr));
            }
        }

        return new SubPath(subPathElements);
    }

    private static Arc parseArc(final CharacterIterator it, final boolean isRelative) {
        it.skipSpaces();

        final List<ArcElement> elements = new ArrayList<>();

        while (it.hasNext() && (Character.isDigit(it.current()) || it.current() == '+' || it.current() == '-')) {
            final double rx = parseNumber(it);
            it.skipSpacesAndCommas();
            final double ry = parseNumber(it);
            it.skipSpacesAndCommas();
            final double xAxisRotation = parseNumber(it);
            it.skipSpacesAndCommas();
            final double largeArcFlag = parseNumber(it);
            it.skipSpacesAndCommas();
            final double sweepFlag = parseNumber(it);
            it.skipSpacesAndCommas();
            final double x = parseNumber(it);
            it.skipSpacesAndCommas();
            final double y = parseNumber(it);
            it.skipSpacesAndCommas();
            elements.add(new ArcElement(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y));
        }

        return new Arc(isRelative, elements);
    }

    private static SmoothQuadraticBezier parseSmoothQuadraticBezier(
            final CharacterIterator it, final boolean isRelative) {
        it.skipSpaces();

        final List<SmoothQuadraticBezierElement> elements = new ArrayList<>();

        while (it.hasNext() && (Character.isDigit(it.current()) || it.current() == '+' || it.current() == '-')) {
            final Point p = parsePoint(it);
            elements.add(new SmoothQuadraticBezierElement(p));
        }

        return new SmoothQuadraticBezier(isRelative, elements);
    }

    private static QuadraticBezier parseQuadraticBezier(final CharacterIterator it, final boolean isRelative) {
        it.skipSpaces();

        final List<QuadraticBezierElement> elements = new ArrayList<>();

        while (it.hasNext() && (Character.isDigit(it.current()) || it.current() == '+' || it.current() == '-')) {
            final Point p1 = parsePoint(it);
            final Point p = parsePoint(it);
            elements.add(new QuadraticBezierElement(p1, p));
        }

        return new QuadraticBezier(isRelative, elements);
    }

    private static SmoothCubicBezier parseSmoothCubicBezier(final CharacterIterator it, final boolean isRelative) {
        it.skipSpaces();

        final List<SmoothCubicBezierElement> elements = new ArrayList<>();

        while (it.hasNext() && Character.isDigit(it.current())) {
            final Point p2 = parsePoint(it);
            final Point p = parsePoint(it);
            elements.add(new SmoothCubicBezierElement(p2, p));
        }

        return new SmoothCubicBezier(isRelative, elements);
    }

    private static CubicBezier parseCubicBezier(final CharacterIterator it, final boolean isRelative) {
        it.skipSpaces();

        final List<CubicBezierElement> elements = new ArrayList<>();

        while (it.hasNext() && (Character.isDigit(it.current()) || it.current() == '+' || it.current() == '-')) {
            final Point p1 = parsePoint(it);
            final Point p2 = parsePoint(it);
            final Point p = parsePoint(it);
            elements.add(new CubicBezierElement(p1, p2, p));
        }

        return new CubicBezier(isRelative, elements);
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

    private static MoveTo parseMoveTo(final CharacterIterator it, final boolean isRelative) {
        it.skipSpaces();

        final List<Point> points = new ArrayList<>();

        while (it.hasNext() && (Character.isDigit(it.current()) || it.current() == '+' || it.current() == '-')) {
            points.add(parsePoint(it));
        }

        return new MoveTo(isRelative, points);
    }

    private static LineTo parseLineTo(final CharacterIterator it, final boolean isRelative) {
        it.skipSpaces();

        final List<Point> points = new ArrayList<>();

        while (it.hasNext() && (Character.isDigit(it.current()) || it.current() == '+' || it.current() == '-')) {
            points.add(parsePoint(it));
        }

        return new LineTo(isRelative, points);
    }

    private static HorizontalLineTo parseHorizontalLineTo(final CharacterIterator it, final boolean isRelative) {
        it.skipSpaces();

        final List<Double> x = new ArrayList<>();

        while (it.hasNext() && (Character.isDigit(it.current()) || it.current() == '+' || it.current() == '-')) {
            x.add(parseNumber(it));
            it.skipSpaces();
        }

        return new HorizontalLineTo(isRelative, x);
    }

    private static Color parseColor(final String v) {
        return switch (v) {
            case "none" -> new Color();
            case "black" -> new Color((byte) 0, (byte) 0, (byte) 0, (byte) 0xff);
            case "red" -> new Color((byte) 0xff, (byte) 0, (byte) 0, (byte) 0xff);
            case "blue" -> new Color((byte) 0, (byte) 0, (byte) 0xff, (byte) 0xff);
            default -> {
                if (v.charAt(0) == '#' && v.length() == 7) {
                    final byte r = ParseUtils.parseByteHex(v.substring(1, 3));
                    final byte g = ParseUtils.parseByteHex(v.substring(3, 5));
                    final byte b = ParseUtils.parseByteHex(v.substring(5, 7));
                    yield new Color(r, g, b, (byte) 0xff);
                }
                throw new IllegalArgumentException(String.format("Unknown color '%s'", v));
            }
        };
    }
}
