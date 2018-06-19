#!/bin/bash

#-------------------------------------------------------------------------------
USAGE="$0 add|del"

PD3D_DIR="../core/src/com/matalok/pd"
STUBS_DIR="../core/src-stubs"
PD_CLASSES_DIR="../core/pd-classes/com/watabou"
PD_DIR="../core/pixel-dungeon/src"

#-------------------------------------------------------------------------------
rename_in_dir() {
    src_dir=$1
    replace_from=$2
    replace_to=$3
    find_pattern="java$"

    echo "Renaming:"
    echo "  dir    : $src_dir"
    echo "  rename : $replace_from -> $replace_to"

    targets=$(/bin/find $src_dir | grep $find_pattern)
    for target in $targets 
    do
        while [ 1 ]; do
            echo -n "Renaming $target..."
            sed -i.sed.bak "s/$replace_from/$replace_to/g" $target && \
                break

            echo " failed [$?]"
            cp $target.sed.bak $target
            sleep 1
        done
        rm $target.sed.bak
        echo " done [$?]"
    done
}

#-------------------------------------------------------------------------------
add_stubs() {
    # Remove old "stubs" package
    rm -rf $STUBS_DIR/stub

    # Make "stub" package
    mkdir $STUBS_DIR/stub
    cp -r $STUBS_DIR/android $STUBS_DIR/stub/
    cp -r $STUBS_DIR/javax $STUBS_DIR/stub/

    # Add "stub" prefix to package names
    rename_in_dir $STUBS_DIR/stub/ "package android." "package stub.android."
    rename_in_dir $STUBS_DIR/stub/ "import android." "import stub.android."
    rename_in_dir $STUBS_DIR/stub/ "package javax." "package stub.javax."
    rename_in_dir $STUBS_DIR/stub/ "import javax." "import stub.javax."

    # Add "stub" prefix to all android references in source code
    rename_in_dir $PD_CLASSES_DIR "android." "stub.android."
    rename_in_dir $PD_CLASSES_DIR "javax." "stub.javax."
    rename_in_dir $PD_DIR "android." "stub.android."
    rename_in_dir $PD_DIR "javax." "stub.javax."
    rename_in_dir $PD3D_DIR "javax." "stub.javax."
}

#-------------------------------------------------------------------------------
del_stubs() {
    # Remove old "stubs" package dir
    rm -rf $STUBS_DIR/stub

    # Remove "stub" prefix from all android&javax references in source code
    rename_in_dir $PD_CLASSES_DIR "stub.android." "android."
    rename_in_dir $PD_CLASSES_DIR "stub.javax." "javax."
    rename_in_dir $PD_DIR "stub.android." "android."
    rename_in_dir $PD_DIR "stub.javax." "javax."
    rename_in_dir $PD3D_DIR "stub.javax." "javax."
}

#-------------------------------------------------------------------------------
# Validate arguments
[ $# -ne 1 ] && \
    echo "Illegal number of arguments: $USAGE" && exit 1
[ $1 != "add" ] && [ $1 != "del" ] && \
    echo "Illegal argument: $USAGE" && exit 1

#-------------------------------------------------------------------------------
# Run
[ $1 == "add" ] && add_stubs
[ $1 == "del" ] && del_stubs
