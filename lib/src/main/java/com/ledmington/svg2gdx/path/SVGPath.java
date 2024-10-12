package com.ledmington.svg2gdx.path;

import com.ledmington.svg2gdx.SVGElement;

import java.util.List;

public record SVGPath(String colorName,List<SVGSubPath> subpaths)implements SVGElement {
	@Override
	public String toGDXShapeRenderer() {
		throw new Error("Not implemented");
	}
}
