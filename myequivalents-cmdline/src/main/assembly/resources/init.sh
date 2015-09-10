#!/bin/sh
cd "$(dirname $0)"
cfg_file=myeq-manager-config.xml

cat <<EOT


	Going to initialise myEquivalents relational DB
	Please ensure you've correct settings in $cfg_file

	This possibly includes the line '<prop key = "hibernate.hbm2ddl.auto"></prop>'
	temporarily set with the value (insde ><) 'create' or 'update' (see [1]).
	
	---> Please be aware that this option might wipe out your existing database.
	---> Morever, it's VERY important that you set it back to empty after this initialisation. 

	[1] http://docs.jboss.org/hibernate/stable/orm/manual/en-US/html/ch03.html#configuration-misc-properties

EOT

./myeq.sh user store -y init_data/admin_user.xml
./myeq.sh service store -u admin -s admin.secret init_data/unspecified_service.xml

cat <<EOT


	Done. ---> Remeber to change the admin password, if not already done in init_data/admin_user.xml

EOT
