# Documentation

Here resides the documentation on how to go about this repository.

## Adding Images

The easiest way to add images to the app is to fork the [gallery] and
add some images there.
You can request to add the gallery to the app by adding the URL to
the [Settings].
Galleries can also be added manually to the app in the settings
of the app.

If you fork the gallery, do not worry: Images will not be displayed twice.
They have a unique name depending on the folder they are in.

If you request to add new images to the gallery,
please make sure they are not violent, frightening or in other ways
controversal as kids should be able to use the app unattended.
Best if you ask your child which images it would like to paint.
I am happy to inlcude these, too.

Added images should be `600px` wide and consist of black and white
pixels while it is possible to already add colors, they may be
transformed to black and white by the app and only visible as preview.

If an app contains many galleries, the user defined galleries are
more important than the built-in galleries.

Related:
- [Issue 90](https://github.com/niccokunzmann/coloring-book/issues/90)

[Settings]: ../src/main/java/org/androidsoft/coloring/util/Settings.java
[gallery]: https://gallery.quelltext.eu

## Translations

Translations are done on [Transifex]. You must request to join the 
team and help translate. Whenever a file is translated 100%,
a new version is created as a commmit.
- [view all commits](https://github.com/niccokunzmann/coloring-book/commits/master)
- [see example commit](https://github.com/niccokunzmann/coloring-book/commit/1b081c0d905b615f340b48bf90487dabdf09ea24)

If you do not manage to translate 100% but want to have it included
in the next release, please open an issue.
We can pull the translations then.

## Releasing new Versions

To release new versions, do the following:

1. Check that the tests are running. [![Android CI](https://github.com/niccokunzmann/androidsoft-coloring/workflows/Android%20CI/badge.svg)](https://github.com/niccokunzmann/androidsoft-coloring/actions?query=workflow%3A%22Android+CI%22)
2. Increase the `versionCode` and `versionName` in the `AndroidManifest.xml`
3. Make sure the changelog file in `metadata/en/changelogs` of the corresponding `versionCode` includes the relevant changes
    - added/removed/improved features
    - changes in language
    - changes in permissions
4. commit the changes with `version <versionName>` and push the commit
5. create a tag `git tag v<versionName>` and push the tag

## Screenshots

The screen shots of the app reside in the `metadata/<lang>/images`
folder.

Documentation:
- [Google for resolution](https://support.google.com/googleplay/android-developer/answer/1078870?hl=en)
- [fastlane for naming](https://docs.fastlane.tools/actions/upload_to_play_store/#images-and-screenshots)
- [Fdroid for location](https://fdroid.gitlab.io/fdroid-website/docs/All_About_Descriptions_Graphics_and_Screenshots/)






[Transifex]: https://www.transifex.com/mundraub-android/coloring-book/dashboard/
