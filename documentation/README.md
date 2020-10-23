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

What to keep in mind when creating an image:
- Added images should be `600px` wide.
- They consist of black and white pixels.
    While it is possible to already add colors, they may be
    transformed to black and white by the app and only be visible as preview.
- The outlines should be `6` to `10` pixels wide.
  This allows scaling down images for fast drawing on smaller phones.
  If they are too small, it might be that areas are joined i.e.
  coloring the head will also color the background.
  

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

### Create a new release

Changes go to the master branch of the app.
Follow this process to publish the latest version.

1. Check that the tests are running. [![Android CI](https://github.com/niccokunzmann/androidsoft-coloring/workflows/Android%20CI/badge.svg)](https://github.com/niccokunzmann/androidsoft-coloring/actions?query=workflow%3A%22Android+CI%22)
2. Fetch all the tags from this repository.
    ```
    git fetch --tags origin
    ```
2. List the releases.
    ```
    git tag
    ```
3. See the changes since the latest release
    ```
    git diff HEAD v1.1.4
    ```
    or the commits - you should see the tags in the commit history.
    ```
    git log
    ```
4. Edit [src/main/AndroidManifest.xml](src/main/AndroidManifest.xml) and increase the `versionCode` and the `versionName`.
5. Create or edit the file for the changes in the [metadata/en/changelogs/](metadata/en/changelogs) folder with the number of the `versionCode`.
    Make sure the changelog file includes the relevant changes:
    - added/removed/improved features
    - changes in language
    - changes in permissions
6. Create a commit with the changes, named `version <versionName>`, tag it as `v<versionName>` and push it as branch and tag
    ```
    git chechout master
    git add src/main/AndroidManifest.xml metadata/en/changelogs/
    git commit -m"version 1.1.5"
    git tag v1.1.5
    git push
    git push origin v1.1.5
    ```


## Screenshots

The screen shots of the app reside in the `metadata/<lang>/images`
folder.

Documentation:
- [Google for resolution](https://support.google.com/googleplay/android-developer/answer/1078870?hl=en)
- [fastlane for naming](https://docs.fastlane.tools/actions/upload_to_play_store/#images-and-screenshots)
- [Fdroid for location](https://fdroid.gitlab.io/fdroid-website/docs/All_About_Descriptions_Graphics_and_Screenshots/)






[Transifex]: https://www.transifex.com/mundraub-android/coloring-book/dashboard/
