#!/bin/sh
cd "$(dirname $0)/.."
./myeq.sh user store -y examples/admin_user.xml
./myeq.sh service store -u admin -s admin.secret <examples/foo_services.xml
./myeq.sh mapping store -u admin -s admin.secret test.testmain.service6:acc1 test.testmain.service7:acc2
./myeq.sh mapping store-bundle -u admin -s admin.secret test.testmain.service6:acc1 test.testmain.service7:acc3 test.testmain.service8:acc1
./myeq.sh mapping store-bundle -u admin -s admin.secret test.testmain.service6:acc10 test.testmain.service7:acc20 test.testmain.service8:acc30
./myeq.sh mapping get -u admin -s admin.secret test.testmain.service7:acc20
