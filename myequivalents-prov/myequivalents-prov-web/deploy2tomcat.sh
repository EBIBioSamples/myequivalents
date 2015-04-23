cd "$(dirname $0)"
parent_dir="../../myequivalents-web"
cp -R "$parent_dir/target/distro-resources" target
. "$parent_dir/deploy2tomcat.sh" ${1+"$@"}
