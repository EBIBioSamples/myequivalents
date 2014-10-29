cd "$(dirname $0)"
export MAVEN_OPTS='-Xms2G -Xmx4G -XX:PermSize=128m -XX:MaxPermSize=256m'
mvn clean install
