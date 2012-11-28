# 
# This copies the .war file in TOMCAT_HOME and necessary configuration files in proper locations. See the documentation
# for details.
# 
cd "$(dirname $0)"

tomcat_home="$1"
if [ "$tomcat_home" == "" ]; then
  cat <<EOT
  
  Usage: $0 <tomcat-home>
  
It will copy configuration files and the war file into the specified Tomcat environment.
Do 'mvn -Ptest.hsql,<your-db-profile> package' before running this script


EOT
  exit 1
fi

set -x xtrace
cp -i target/myequivalents.war "$tomcat_home/webapps"
cp -i target/classes/META-INF/context.xml "$tomcat_home/conf/Catalina/localhost/myequivalents.xml"
cp -i target/classes/hibernate.properties "$tomcat_home/webapps/myequivalents.hibernate.properties"
set +o xtrace

cat <<EOT

	Copy Done. Now all the necessary files should be in the right places under "$tomcat_home".
	
  The End. 

EOT
