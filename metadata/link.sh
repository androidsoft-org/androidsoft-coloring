#!/bin/bash
#
# Transifex creates stupid directories. Thus, we link them here.
#

set -e

transifex=../documentation/transifex-github-integration.yml
end="all ok, no errors"

function linkToLanguage() {
  local lang=$1
  local country=$2
  mkdir -p $lang
  if [ -d $lang-$country ] && ! [ -L $lang-$country ]; then
    echo "ERROR: clean up $lang-$country"
    end=""
    return
  fi
  rm -f $lang-$country $lang-r$country || true
  ln -sT $lang $lang-$country 
}

function linkToCountry() {
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
  if ! cat $transifex | grep -q "$lang-$country: $lang-r$country"; then
    echo "ERROR: please update the transifex settings in $transifex to map $lang-$country: $lang-r$country"
    end=""
  fi
}


linkToLanguage de DE
linkToCountry pt BR
linkToLanguage fr FR
linkToLanguage pl PL
linkToLanguage jp JP
linkToLanguage es ES
linkToLanguage ru RU

if [ -n "$end" ]; then
  echo $end
  ls -l
  echo $end
fi

