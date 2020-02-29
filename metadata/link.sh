#!/bin/bash
#
# Transifex creates stupid directories. Thus, we link them here.
#

set -e

end="all ok, no errors"

function link() {
  local lang=$1
  local country=$2
  mkdir -p $lang-r$country
  if [ -d $lang-$country ] && ! [ -L $lang-$country ]; then
    echo "ERROR: clean up $lang-$country"
    end=""
    return
  fi
  if [ -d $lang ] && ! [ -L $lang ]; then
    echo "ERROR: clean up $lang"
    end=""
    return
  fi
  rm -f $lang $lang-$country || true
  ln -sT $lang-r$country $lang-$country 
  ln -sT $lang-r$country $lang
}

link de DE
link pt BR
link fr FR
link pl PL
link jp JP
link es ES
link ru RU

if [ -n "$end" ]; then
    echo $end
fi

