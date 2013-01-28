#!/bin/sh
MYDIR="$(dirname $0)"

target="$1"

if [ "$target" == "--help" ]; then
  cat <<EOT
  
  
  usage: $0 [target]
Deploys the command line binaries on the specified destination ( ./target is the default ).

EOT
exit 1
fi

if [ "$target" == "" ]; then
  target=target
fi


cd "$MYDIR/target"

if [ "$target" != "target" ]; then
  cp -f ./myequivalents_shell_*.zip "$target"
  cd "$target"
fi

yes | unzip "./myequivalents_shell_*.zip"

exit_code=$?

echo
echo
echo The End

exit $?

  