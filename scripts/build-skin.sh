#!/bin/bash

#-------------------------------------------------------------------------------
USAGE="$0 skin_name"
GDX_TOOLS_PATH="$USERPROFILE/bin/gdx-tools"
TP="runnable-texturepacker.jar"
SKINS_PATH="../assets-raw/skins"
SKIN_NAME="$1"
PACK_NAME="uiskin"

#-------------------------------------------------------------------------------
# Validate arguments
[ $# -ne 1 ] && \
    echo "Illegal number of arguments: $USAGE" && exit 1
[ ! -d $SKINS_PATH/$SKIN_NAME ] && \
    echo "Target skin does not exist: $USAGE" && exit 1

#-------------------------------------------------------------------------------
# Run

# Prepare tmp&output directories
tmp_dir="$SKINS_PATH/$SKIN_NAME/tmp"
output_dir="$SKINS_PATH/$SKIN_NAME-packed"
rm -rf $tmp_dir $output_dir
mkdir $tmp_dir

# Copy all resources to temporary directory
echo " >>> Copying resources"
cp -v $SKINS_PATH/$SKIN_NAME/png/*.png $tmp_dir 
cp -v $SKINS_PATH/$SKIN_NAME/font/*.png $tmp_dir
cp -v $SKINS_PATH/$SKIN_NAME/pack.json $tmp_dir

# Pack skin into single texture
echo " >>> Packing skin texture"
java -jar $GDX_TOOLS_PATH/$TP $tmp_dir $output_dir $PACK_NAME
rm -rf $tmp_dir

# Copy fnt&json to output
echo " >>> Packing fnt&json"
cp -v $SKINS_PATH/$SKIN_NAME/font/*.fnt $output_dir
cp -v $SKINS_PATH/$SKIN_NAME/$PACK_NAME.json $output_dir
