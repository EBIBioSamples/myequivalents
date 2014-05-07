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

# This is needed at the EBI, it should be safely ignored elsewhere
export PATH="$PATH:/ebi/microarray/home/bamboo/maven-3.0.4/bin"

if [ "$target" == "" ]; then
  target=target
fi

cd "$MYDIR"
export PRJ_VERSION=$(mvn help:evaluate -Dexpression=project.version |grep -E '^[0-9,.]+.*')

cd target

if [ "$target" != "target" ]; then
  cp -f ./myequivalents_shell_${PRJ_VERSION}.zip "$target"
  cd "$target"
fi

yes | unzip "./myequivalents_shell_${PRJ_VERSION}.zip"

exit_code=$?

echo
echo
echo The End

exit $exit_code
  