#!/bin/sh
cd "$(dirname $0)/.."
echo "\n\n\n\tGoing to create examples, This requires prior run of init.sh\n\n"

set -o xtrace
./myeq.sh service store -u admin -s admin.secret <examples/foo_services.xml
./myeq.sh mapping store -u admin -s admin.secret test.testmain.service6:acc1 test.testmain.service7:acc2
./myeq.sh mapping store-bundle -u admin -s admin.secret test.testmain.service6:acc1 test.testmain.service7:acc3 test.testmain.service8:acc1
./myeq.sh mapping store-bundle -u admin -s admin.secret test.testmain.service6:acc10 test.testmain.service7:acc20 test.testmain.service8:acc30

# URIs are used to refer entities. In the first case the service the entity is about is given
# Second case doen't mention the service, it will be retrieved by matching the URI with URI pattern (slightly slower)
# Last case says that the URI is not associated to any service, which myEq transaltes into the special 'unspecified'
#   service ( '_:<uri>' is an equivalent form, where the unspecified service name '_' is explicitly used) 
./myeq.sh mapping store-bundle -u admin -s admin.secret \
	'test.testmain.service6:<http://somewhere.in.the.net/testmain/service6/someType1/uritest1>' \
	'<http://somewhere.in.the.net/testmain/service6/someType1/uritest2>' \
	':<http://www.you.cannnot.find.me/testmain/uri3>'


./myeq.sh mapping get test.testmain.service7:acc20

# URIs associated to services are broken into the form service:acc
./myeq.sh mapping get 'test.testmain.service6:uritest1'

# But you can still use them
./myeq.sh mapping get '<http://somewhere.in.the.net/testmain/service6/someType1/uritest2>'

set +o xtrace

printf "\n\n\n\tDone!\n\n"
