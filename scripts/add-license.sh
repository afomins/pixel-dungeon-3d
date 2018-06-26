#!/bin/bash

/bin/find .. | grep java$ | \
while read file_name; do
    echo -n $file_name
    if grep "Oleg Dolya" $file_name > /dev/null; then
        echo "... ignoring"
        continue
    fi
    
    echo "... ok"
    cat > $file_name.new <<EOF
/*
 * Pixel Dungeon 3D
 * Copyright (C) 2016-2018 Alex Fomins
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

EOF

    cat $file_name >> $file_name.new
    mv  $file_name.new $file_name
done
