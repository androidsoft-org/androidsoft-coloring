# Coloring Book
[![Android CI](https://github.com/niccokunzmann/androidsoft-coloring/workflows/Android%20CI/badge.svg)](https://github.com/niccokunzmann/androidsoft-coloring/actions?query=workflow%3A%22Android+CI%22)

<img src="metadata/en/images/phoneScreenshots/02choose.png" width="24%"> <img src="metadata/en/images/phoneScreenshots/03colors.png" width="24%"> <img src="metadata/en/images/phoneScreenshots/05classify.png" width="24%"> <img src="metadata/en/images/phoneScreenshots/06paint.png" width="24%">

## Download

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/eu.quelltext.coloring/)

## Contribution

- You can translate the app on [Transifex](https://www.transifex.com/mundraub-android/coloring-book/)
- You can donate with [Liberapay](https://liberapay.com/Fruit-Radar-Development)

## Development

This app is developed using Android Studio.

You are welcome to contribute!
- Translate the app - [read the Documentation]
- Add features, see HowTo.
- Solve [issues].

If so and you need help, do not hesitate to open an [issue][issues] to ask!
If you like to maintain this app, please leave a note.

## How To

### Add more pictures to paint

1. Open Android Studio
2. Click right on `res` > `drawable`
3. Click right > `New` > `Image Asset`
4. Make sure to name your image asset starting with `outline` followed by the
    number, underscore and name.
    Also, we do need PNG images of size 600x480, so the app stays small.

### Translate the app

- Go to [Transifex] and [read the Documentation].
- If you do not find your language, you are invited to request it!
- The updated version of the translations will be pushed automatically to the master branch of the repository.

## License

This software is open-source under the [GPLv3](LICENSE).

## Credits

- see the app's credits/about page
- [androidsoft-lib-utils](https://github.com/niccokunzmann/androidsoft-lib-utils)
- [comic clustering](https://github.com/niccokunzmann/comic-cluster)

## Components
- [coloring-book-gallery](https://github.com/niccokunzmann/coloring-book-gallery)
- [coloringbook-lib-utils](https://github.com/niccokunzmann/coloringbook-lib-utils)
- [androidsoft-lib-credits](https://github.com/androidsoft-org/androidsoft-lib-credits/)
- [Weka-for-Android](https://github.com/rjmarsan/Weka-for-Android)

[issues]: https://github.com/niccokunzmann/androidsoft-coloring/issues
[Transifex]: https://www.transifex.com/mundraub-android/coloring-book/dashboard/
[read the Documentation]: documentation/README.md#readme

