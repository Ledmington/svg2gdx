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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.ledmington.svg2gdx.path.SVGPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class SVGImage implements SVGElement {

    private final double width;
    private final double height;
    private final SVGPalette palette = new SVGPalette();
    private final List<SVGElement> elements = new ArrayList<>();

    public SVGImage(final String inputFilename) {
        final File inputFile = new File(inputFilename);
        final DocumentBuilder dBuilder;
        try {
            dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        final Document doc;
        try {
            doc = dBuilder.parse(inputFile);
        } catch (final SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        doc.getDocumentElement().normalize();
        final Element root = doc.getDocumentElement();
        if (!root.getNodeName().equals("svg")) {
            throw new IllegalArgumentException(
                    String.format("Invalid root element: expected 'svg' but was '%s'", root.getNodeName()));
        }

        width = Double.parseDouble(root.getAttributes().getNamedItem("width").getNodeValue());
        height = Double.parseDouble(root.getAttributes().getNamedItem("height").getNodeValue());

        final NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node node = children.item(i);
            switch (node.getNodeName()) {
                case "rect":
                    elements.add(convertRect(node));
                    break;
                case "path":
                    elements.add(convertPath(node));
                    break;
                case "defs":
                case "metadata":
                case "#text":
                    // we don't care about these
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Unknown element with name '%s'", node.getNodeName()));
            }
        }
    }

    private SVGRectangle convertRect(final Node node) {
        final NamedNodeMap m = node.getAttributes();

        final double width = Double.parseDouble(m.getNamedItem("width").getNodeValue());
        final double height = Double.parseDouble(m.getNamedItem("height").getNodeValue());
        final double x = Double.parseDouble(m.getNamedItem("x").getNodeValue());
        final double y = Double.parseDouble(m.getNamedItem("y").getNodeValue());

        final String style = m.getNamedItem("style").getNodeValue();
        final Map<String, String> styleValues =
                Arrays.stream(style.split(";")).collect(Collectors.toMap(s -> s.split(":")[0], s -> s.split(":")[1]));

        SVGColor color = new SVGColor();
        final boolean filled = styleValues.containsKey("fill");
        if (filled) {
            final String hexColor = styleValues.get("fill").substring(1); // removing the starting '#'
            final byte r = ParseUtils.parseByteHex(hexColor.substring(0, 2));
            final byte g = ParseUtils.parseByteHex(hexColor.substring(2, 4));
            final byte b = ParseUtils.parseByteHex(hexColor.substring(4, 6));
            byte a = (byte) 0xff;
            if (styleValues.containsKey("fill-opacity")) {
                final double opacity = Double.parseDouble(styleValues.get("fill-opacity"));
                if (opacity < 0.0 || opacity > 1.0) {
                    throw new IllegalArgumentException(
                            String.format("Invalid opacity value: expected between 0.0 and 1.0 but was %f", opacity));
                }
                a = (byte) (((int) (opacity * 255.0)) & 0x000000ff);
            }
            color = new SVGColor(r, g, b, a);
            palette.add(color);
        }

        return new SVGRectangle(x, y, width, height, filled, palette.getName(color));
    }

    private SVGPath convertPath(final Node node) {
        final NamedNodeMap m = node.getAttributes();

        if (m.getNamedItem("d") == null) {
            throw new IllegalArgumentException("Expected a 'd' attribute for 'path' element");
        }

        final String style = m.getNamedItem("style").getNodeValue();
        final Map<String, String> styleValues =
                Arrays.stream(style.split(";")).collect(Collectors.toMap(s -> s.split(":")[0], s -> s.split(":")[1]));

        SVGColor color = new SVGColor();
        final boolean filled = styleValues.containsKey("fill");
        if (filled) {
            final String hexColor = styleValues.get("fill").substring(1); // removing the starting '#'
            final byte r = ParseUtils.parseByteHex(hexColor.substring(0, 2));
            final byte g = ParseUtils.parseByteHex(hexColor.substring(2, 4));
            final byte b = ParseUtils.parseByteHex(hexColor.substring(4, 6));
            byte a = (byte) 0xff;
            if (styleValues.containsKey("fill-opacity")) {
                final double opacity = Double.parseDouble(styleValues.get("fill-opacity"));
                if (opacity < 0.0 || opacity > 1.0) {
                    throw new IllegalArgumentException(
                            String.format("Invalid opacity value: expected between 0.0 and 1.0 but was %f", opacity));
                }
                a = (byte) (((int) (opacity * 255.0)) & 0x000000ff);
            }
            color = new SVGColor(r, g, b, a);
            palette.add(color);
        }
        final String colorName = palette.getName(color);

        return new SVGPath(m.getNamedItem("d").getNodeValue(), colorName);
    }

    @Override
    public String toGDXShapeRenderer() {
        final StringBuilder sb = new StringBuilder();
        sb.append("private void draw(final float x, final float y) {\n")
                .append(String.format("final double width = %s;", width))
                .append('\n')
                .append(String.format("final double height = %s;", height))
                .append('\n')
                .append(palette.toGDXShapeRenderer())
                .append("final ShapeRenderer sr = @Place here your ShapeRenderer@;\n")
                .append("float currentX=0.0f;\n")
                .append("float currentY=0.0f;\n")
                .append("float initialX=0.0f;\n")
                .append("float initialY=0.0f;\n")
                .append("sr.setAutoShapeType(true);\n")
                .append("sr.begin();\n");
        for (final SVGElement elem : elements) {
            sb.append(elem.toGDXShapeRenderer());
        }
        sb.append("sr.end();\n}\n");
        return sb.toString();
    }
}
