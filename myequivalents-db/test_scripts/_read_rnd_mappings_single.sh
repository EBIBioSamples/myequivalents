cd "$(dirname $0)/.."

# 
# Helper for read_rnd_mappings.sh, do not invoke it directly
#

nprocs=$1

# Wait that all my brothers are put on RUN by the LSF. This is to ensure real parallelism, it's necessary cause 
# often the LSF keeps jobs in queue for a significant time
#
while [ "$(bjobs |grep --count PEND )" -gt 0 ]; do sleep 1; done

# Run the test
export JVM_OPT='-Xms2G -Xmx4G -XX:PermSize=1G -XX:MaxPermSize=2G'
mvn -DargLine="$JVM_OPT" -Ptest.oracle_test,no_copy -Dtest=RandomMappingsTest#readRandomMappings test
