cd "$(dirname $0)/.."

# 
# Invokes parallel JVMs, each running RandomMappingsTest.readRandomMappings(), the mapping reading test, to check
# the performance of such parallel invocations
#
# This exploits the LSF infrastructure we have at the EBI.
# When al the jobs are executed, you can collect results this way:
# for i in $(cat read_rnd_mappings_*.out | grep -E '.+in ([0-9,\.]+) secs.+' | sed -E s/'.+in ([0-9,\.]+) secs.+'/'\1'/); do printf "%f, " $i; done
# This will give you a comma-separated list, which I usually put in a electronic spreadsheet, under =Average()
# 

nprocs=$1

# Remove previous executions leftovers
rm -f target/read_rnd_mappings_*.out

# Do this once here, cause parallelism conflicts don't allow us to do it in the spawned jobs (nor is it efficient).
mvn -Ptest.oracle_test test-compile

# Submit the same job to LSF
for (( i=1; i <= $nprocs; i++ ))
do
	bsub -oo target/read_rnd_mappings_$i.out -M 8500 \
		./test_scripts/_read_rnd_mappings_single.sh
done
