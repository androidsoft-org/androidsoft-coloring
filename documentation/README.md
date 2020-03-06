# Documentation

Here resides the documentation on how to go about this repository.

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
1. Increase the `versionCode` and `versionName` in the `AndroidManifest.xml`
2. Make sure the changelog file in `metadata/en/changelogs` of the corresponding `versionCode` includes the relevant changes
    - added/removed/improved features
    - changes in language
    - changes in permissions
3. commit the changes with `version <versionName>` and push the commit
4. create a tag `git tag v<versionName>` and push the tag

## Images

See the documentation
- [Google for resolution](https://support.google.com/googleplay/android-developer/answer/1078870?hl=en)
- [fastlane for naming](https://docs.fastlane.tools/actions/upload_to_play_store/#images-and-screenshots)
- [Fdroid for location](https://fdroid.gitlab.io/fdroid-website/docs/All_About_Descriptions_Graphics_and_Screenshots/)






[Transifex]: https://www.transifex.com/mundraub-android/coloring-book/dashboard/
