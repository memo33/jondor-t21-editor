Jondor's T21 Editor
===================

This program was created by Jondor and allows editing T21 Exemplar files for SimCity 4.

Build instructions
------------------

    ant -Dno.deps=1
    cp -p -r lib/ dist/lib/
    java -jar dist/T21_editor.jar




The following is a copy of the original readme file.




T21 Editor version 0.51b

This utility is designed to make the creation and editing of Type 21 exemplars easier.

## License
This program is provided free of charge and without warranty.  The creator cannot be held responsible for the integrity of your data should you use this program.  This program is not licensed by or associtated with Maxis or Electronic Arts.

## Requirements
Java Runtime Environment 7 (JRE 7) or higher

## Version History
0.51b - Fixed a bug where using fewer than 4 zone types would crash the program on saving.  Saving dat files will no longer strip duplicate TGIs from the file.

0.5b - Now reads prop exemplars and s3d models and displays the prop name and model vertices if available.  Also saves and reloads the last directory accessed.

0.43b - Fixed a bug where prop positions would sometimes get screwed up when selecting a different prop line to edit.

0.42b - Added an editor for LotConfigPropertyLotObject Reps in excess of 13.

0.41b - Arrows added to the prop display and colors updated for better contrast.  Various changes and bug fixes applied to the jDBPF library.

0.4b - Fixed a bug that would cause certain files to crash on decompression.  Malformed T21 TGIs will be automatically fixed when using the Save or Save and Close buttons.  Files are now opened in background threads so it will no longer hang the interface.  The program can now scan the SC4 app and plugins directory and load some network textures automatically.  It will look for 128x128 textures in the following order: tile iid + 4, tile iid + 14 (E), tile iid + 0

0.31b - Fixed a slight bug that was malforming TGIs.

0.3b - Fixed a bug that caused the Exemplar Name field not to function.  Fixed the version property being missing from written exemplars.  Fixed a bug that could cause certain dialog boxes to crash the program.

0.2b - Fixed a bug that would cause T21 editors to stay open when the file they're in was closed.

0.1b - First NAM Team beta release


## Usage Guide
With JRE 7 properly installed and configured to run JAR files, double click on T21_editor.bat to run.

***IMPORTANT NOTE!  Do not modify any plugin files while this program is open.  It is not yet robust enough to detect the discrepancy and may load or save bad data.***

## Interface
Open and save DBPF files with the File menu.  Double-click exemplars in the list to edit them.  Press delete with an exemplar selected to delete it.

If no saved settings are found, the program will prompt for the SC4 app and user plugins directories.
Ex: C:\Program Files\Maxis\SimCity 4 and C:\Documents and Settings\User\My Documents\SimCity 4\Plugins

It will scan the plugins directory in the background.  Progress will be shown on the toolbar at the bottom of the main window.  You may load and edit files while this is happening.  Once matching TGIs have been found, the data will be automatically loaded.

Exemplar Editor controls:

Layout Tab:
Name: Exemplar Name.
IID: Controls both the IID of the exemplar and the content of the exemplar IID backreference property.
Cancellation Entry: Creates an exemplar with only type, name, and IID properties (useful for mods needing to cancel out a specific T21 located at the indicated IID in another file).
Network Tile: The IID of the network tile this T21 affects.
Min Slope and Max Slope: The minimum and maximum slope the T21 will appear on.  Labeled backwards in the Reader.
Pattern Size: Changes the repeat size of the pattern.  Only values 3 and 4 are useful.  >4 does not work properly in game, 1 and 2 can be generalized to 4.
Pattern: Depress the buttons to set the desired pattern.  Orientation is as in game: North = up.  Current pattern is also shown as Hex values.
Zones: Check the zones this T21 affects.
Wealths: Check the wealths this T21 affects.
Flips: Choose the flips this T21 affects.
Rotations: Check the rotations this T21 affects.  North (0) is the orientation of FSH or S3D files in the Reader, and corresponds to RUL2 rotation 0.

Prop/Flora Tab:
Double Click colums in the table to edit them.
Type: Prop (0x01) or Flora (0x04).
LOD: Graphics detail setting in game to display this prop or flora at. 0x00, 0x10, or 0x20.
Flag: Mysterious flag of dubious use.  Appears as LOD values 0x11 and 0x21 in some exemplars.
Object#: Mysterious rep 12.  Randomly generated for new lines.
IID: Prop or Flora IID.  Prop lines can have more than one IID (randomly selected in game).  Click the "..." button to open a small dialog to edit the additional IIDs.  To add a line, type the desired IID in the "Add New" line.  To delete a line, just blank the text box and hit enter.  Click close or hit ESC to close the dialog.

Up: Move selected prop/flora line up the list
Add: Add a new prop/flora line
Delete: Delete selected prop/flora
Down: Move selected prop/flora line down the list
Format: Display coordinate in Decimal meters or raw Hex values
If available, the name of the currently selected prop will be displayed.

Xpos (et al): Edit the coordinate of the selected prop/flora.  Editing the x or z position will move the bounding boxes without changing their size (subject to the edges of tbe tile).  Entering decimal values will compute the closest raw hex value and update the decimal representation accordingly, do not be alarmed.
Rotation: Prop/flora rotation, (this is a stage direction); South (0) corresponds to the zero numbered rotation for RKT1 props, the reader orientation for RKT0/3 props and the second rotation for ATC props (see the wiki for an example of the ATC rotation discrepancy).
Get Prop Bounds: If the prop exemplar has been located, this button will use the Occupant Size to automatically set the X and Z values of the bounding box.

Bounding Boxes: If checked, the bounding boxes will be drawn.  Current prop in red.
Model Vertices: If checked, and available, the vertices from the model's S3D will be drawn.  Current prop in magenta.
