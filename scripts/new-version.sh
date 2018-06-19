#!/bin/bash

#-------------------------------------------------------------------------------
USAGE="$0 1|2|3"

VERSION_FILE=../version.txt

#-------------------------------------------------------------------------------
# Validate arguments
[ $# -ne 1 ] && \
    echo "Illegal number of arguments: $USAGE" && exit 1
[ $1 != "1" ] && [ $1 != "2" ] && [ $1 != "3" ] && \
    echo "Illegal argument: $USAGE" && exit 1

#-------------------------------------------------------------------------------
# Run

# Old version
v1=`cat $VERSION_FILE | cut -d '.' -f1`
v2=`cat $VERSION_FILE | cut -d '.' -f2`
v3=`cat $VERSION_FILE | cut -d '.' -f3`
old_version="$v1.$v2.$v3"

# New version
[ $1 == "1" ] && v1=$((v1 + 1))
[ $1 == "2" ] && v2=$((v2 + 1))
[ $1 == "3" ] && v3=$((v3 + 1))
new_version="$v1.$v2.$v3"

# Confirm
read -s -n 1 -p "Incrementing version from $old_version to $new_version. Continue [y/n]?:" ans_yn
case "$ans_yn" in
  [Yy]) echo " ...continue";;

  *) echo " ...quit" && exit 1;;
esac

# update
echo $new_version > $VERSION_FILE
cd ../../pd3d/
sed -i.sed.bak "s/$old_version/$new_version/g" ./core/src/com/matalok/pd3d/Config.java
sed -i.sed.bak "s/$old_version/$new_version/g" ./android/AndroidManifest.xml
