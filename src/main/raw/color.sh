#!/bin/sh

BTN_PRE='\
        <org.androidsoft.coloring.ui.widget.ColorButton android:layout_width=\"wrap_content\"\
                                         android:layout_height=\"wrap_content\"\
                                         coloring:color=\"'
BTN_POST='\"/>'

cat colors.html \
  | grep "<\(/\?\)tr>\|<td.*</td>" \
  | sed "s#^ *<tr>#    <TableRow>#" \
  | sed "s#^ *</tr>#    </TableRow>#" \
  | sed "s#^ *<td .*\([0-9A-F]\{6\}\).*</td>#$BTN_PRE\#\1$BTN_POST#"

