# svg2gdx
## A converter from SVG to libGDX `ShapeRenderer` code.

## How to use
````bash
java -jar svg2gdx.jar myimage.svg
````

## How to build
You need gradle and java >=21 to compile and run the project.

If you just want the converter as an executable jar, you can run:
```bash
./gradlew fatjar
```

Otherwise, if you wish to contribute, you should use:
```bash
./gradlew build
```

## TODO / Future works
- [ ] Output preview through a libGDX miniapp.
- [ ] Static conversion through stdin/stdout
- [ ] Bulk static conversion (regex?)
- [ ] Runtime conversion
- [ ] Java 8 compatibility

## License
This project is licensed under the GNU GPLv3 license.
