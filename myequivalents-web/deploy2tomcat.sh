#
# This copies the .war file in TOMCAT_HOME and necessary configuration files in proper locations. See the documentation
# for details.
# 
cd "$(dirname $0)"

tomcat_home="$1"
if [ "$tomcat_home" == "" ]; then
  cat <<EOT
  
  Usage: $0 <tomcat-home> [app-context-path] [app-config-dir] [app-deploy-dir]
  
It will copy configuration files and the war file into the specified Tomcat environment.

app-context-path will be attached to the URL of your server, e.g., test/myeq will make myequivalents available
at http://localhost:8080/test/myeq. This requires that the context file and the war file are named accordingly (using 
hashes). DO NOT USE ANY INITIAL '/' for this. Default is 'myequivalents'.

app-config-dir is a configuration directory that will be put under <tomcat-home>/conf/Catalina/localhost. 
Default is <app-context-path>-conf

app-deploy-dir is the directory under <tomcat-home> where the war is copied to. Default is webapps, in certain non-standard
configurations you might need to change this.

Do 'mvn -Ptest.hsql,<your-db-profile> package' before running this script


EOT
  exit 1
fi

context_path="$2"
if [ "$context_path" == "" ]; then
  context_path=myequivalents
fi

app_config_dir="$3"
if [ "$app_config_dir" == "" ]; then
  app_config_dir="${context_path}-conf"
fi

app_deploy_dir="$4"
if [ "$app_deploy_dir" == "" ]; then
  app_deploy_dir=webapps
fi


# Config dir in context.xml needs to be re-configured
subst_path=$(echo "$app_config_dir"| sed s/'\/'/'\\\/'/g)
sed s/'conf\/Catalina\/localhost\/myequivalents-config'/"conf\/Catalina\/localhost\/$subst_path"/g \
  target/distro-resources/META-INF/context.xml >target/_reconfigured_context.xml

context_path_hashed=$(echo "$context_path" | sed s/'\/'/'#'/g)
tomcat_localhost_conf="$tomcat_home/conf/Catalina/localhost"

set -x xtrace
cp -i target/myequivalents.war "$tomcat_home/${app_deploy_dir}/${context_path_hashed}.war"
cp -i target/_reconfigured_context.xml "${tomcat_localhost_conf}/${context_path_hashed}.xml"
if [ ! -e "${tomcat_localhost_conf}/${app_config_dir}"  ]; then mkdir -p "${tomcat_localhost_conf}/${app_config_dir}"; fi
cp -i target/distro-resources/myeq-manager-config.xml "${tomcat_localhost_conf}/${app_config_dir}/myeq-manager-config.xml"
set +o xtrace

cat <<EOT

	Copy Done. Now all the necessary files should be in the right places under "$tomcat_home".
	
  The End. 

EOT
