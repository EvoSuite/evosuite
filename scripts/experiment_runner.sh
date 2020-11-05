#!/usr/bin/env bash
# Purpose: Run large scale experiments with EvoSuite
# Author: Mitchell Olsthoorn
# ------------------------------------------

set -u # Treat unset variables as an error when substituting

# Warn helper functions
_warn() {
  echo >&2 ":: $*"
}

# Die helper functions
_die() {
  echo >&2 ":: $*"
  exit 1
}

# Log helper functions
_log() {
  echo "RUNNER: $*"
}

# Interruption handler
_int_handler() {
    _warn "Interrupted"
    kill $PPID # Kill the parent process of the script.
    exit 1
}
trap '_int_handler' INT

# Usage prompt
_usage() {
cat << EOF

Usage
  $0 [options] <configurations_file> <projects_file>

Options:
  <configurations_file>    CSV file with configurations for EvoSuite (columns: name,configuration)
  <projects_file>          CSV file with projects and their corresponding classes to run (columns: project,class)
  -h                       print help and exit
  -m <memory>              memory limit (MB) for the EvoSuite client process (default: 2500)
  -p <parallel_instances>  limit for the number of parallel executions (default: 1)
  -r <rounds>              number of rounds to execute each experiment (default: 1)
  -s <seeds_file>          file with the seeds for the executions of the experiment (default: SEEDS)
  -t <timeout>             amount of time before EvoSuite process is killed (default: 10m)

Examples:
  $0 configurations.csv projects.csv
  $0 -r 10 -p 4 configurations.csv projects.csv
  $0 -t 1h -m 4096 configurations.csv projects.csv

EOF
}

# Default values
MEMORY=2500          # The memory limit (MB) for the EvoSuite client process
PARALLEL_INSTANCES=1 # The amount of parallel executions the experiment should use
ROUNDS=1             # The number of rounds to perform of the experiment
SEEDS_FILE=SEEDS     # The file for storing the random seeds for the experiment
TIMEOUT=10m          # The amount of time before the EvoSuite process is killed

# Constants
PROJECTS_DIRECTORY=projects # Directory where the projects are stored
RESULTS_DIRECTORY=results   # Directory where the results should be stored

# Argument parsing
while getopts ":hm:p:r:s:t:" o; do
  case "${o}" in
    h)
      _usage
      exit 0
      ;;
    m)
      MEMORY=${OPTARG}

      # Exit when the MEMORY is below 1
      (( MEMORY < 1 )) && { _warn "The amount of memory should be at least 1"; _usage; exit 1; }
      ;;
    p)
      PARALLEL_INSTANCES=${OPTARG}

      # Exit when the PARALLEL_INSTANCES is below 1
      (( PARALLEL_INSTANCES < 1 )) && { _warn "The number of parallel instances should be at least 1"; _usage; exit 1; }
      ;;
    r)
      ROUNDS=${OPTARG}

      # Exit when the ROUNDS are below 1
      (( ROUNDS < 1 )) && { _warn "Rounds should be at least 1"; _usage; exit 1; }
      ;;
    s)
      SEEDS_FILE=${OPTARG}
      ;;
    t)
      TIMEOUT=${OPTARG}
      ;;
    \?)
      _warn "Invalid option: -$OPTARG"
      _usage
      exit 1
      ;;
    :)
      _warn "Option -$OPTARG requires an argument"
      _usage
      exit 1
      ;;
    *)
      _usage
      exit 1
      ;;
  esac
done

shift "$((OPTIND-1))" # Remove parsed flags from argument pool

# Exit when the required arguments are not specified
if [ "$#" -ne 2 ]; then
  _warn "Configurations and projects file have to be specified"
  _usage
  exit 1
fi

CONFIGURATIONS_FILE=$1 # CSV file with configurations for EvoSuite
PROJECTS_FILE=$2       # CSV file with projects and their corresponding classes to run

# Exit when the configurations or projects file could not be found
[ ! -f $CONFIGURATIONS_FILE ] && { _die "($CONFIGURATIONS_FILE) file not found"; }
[ ! -f $PROJECTS_FILE ] && { _die "($PROJECTS_FILE) file not found"; }

# Determines the classpath based on the project and outputs this
# Expects the following file structure: ./projects/<project>/<jars>
_get_project_class_path() {
  local _project=$1           # Project name
  local _search_depth=${2:-1} # Directory search depth (default: 1)

  # Find all paths of jars in the search directory and combine with a colon separator
  local _project_cp=$(find $PROJECTS_DIRECTORY/$_project -maxdepth $_search_depth -type f -name "*.jar" -printf '%p:' | sed 's/:$//')

  echo $_project_cp # Output value
}

# Runs a single configuration of EvoSuite
_run_evosuite() {
  local _execution=$1          # Execution number
  local _num_executions=$2     # Number of total executions
  local _round=$3              # Round number
  local _configuration_name=$4 # Name of configuration
  local _user_configuration=$5 # User configuration of EvoSuite
  local _project=$6            # Project name
  local _class=$7              # Full class name
  local _seed=$8               # Seed

  local _project_class_path=$(_get_project_class_path "$_project") # Project class path

  # Output locations
  local _report_dir=$RESULTS_DIRECTORY/$_configuration_name/$_project/reports/$_round
  local _test_dir=$RESULTS_DIRECTORY/$_configuration_name/$_project/tests/$_round
  local _log_dir=$RESULTS_DIRECTORY/$_configuration_name/$_project/logs/
  local _log_file=$RESULTS_DIRECTORY/$_configuration_name/$_project/logs/$_round

  mkdir -p $_log_dir # Create log directory

  # Convert configuration into seperate items
  local _old_ifs=$IFS; IFS=' '; read -ra _user_configuration_array <<< "$_user_configuration"; IFS=$_old_ifs;

  _log "Execution ($_execution / $_num_executions): Running round ($_round) of configuration ($_configuration_name) for class ($_class) in project ($_project) with seed ($_seed)"

  # Run EvoSuite in background
  timeout -k $TIMEOUT $TIMEOUT /usr/bin/env java -Xmx4G -jar /evosuite-bin/evosuite.jar \
  -mem "$MEMORY" \
  -Dconfiguration_id="$_configuration_name" \
  -Dgroup_id="$_project" \
  -projectCP "$_project_class_path" \
  -class "$_class" \
  -seed "$_seed" \
  -Dreport_dir="$_report_dir" \
  -Dtest_dir="$_test_dir" \
  -Dshow_progress=false \
  -Dplot=false \
  -Dclient_on_thread=false \
  "${_user_configuration_array[@]}" \
  &> "$_log_file" &

  local _pid=$! # PID number of background job
}

# Run large scale experiment
_run_experiment() {
  # Cancel execution if the results directory is present
  [ -d $RESULTS_DIRECTORY ] && { _die "($RESULTS_DIRECTORY) directory is present, cancelling experiment"; }

  local _num_configurations=$(( $(wc -l < $CONFIGURATIONS_FILE) - 1 ))      # Number of configurations in experiment
  local _num_classes=$(( $(wc -l < $PROJECTS_FILE) - 1 ))                   # Number of classes in experiment
  local _num_executions=$(($ROUNDS * $_num_configurations * $_num_classes)) # Number of total executions

  _log "Start experiment with ($_num_executions) total executions across ($PARALLEL_INSTANCES) parallel instances"
  _log "Perform ($ROUNDS) rounds with ($_num_configurations) configurations of ($_num_classes) classes"
  _log "Run the EvoSuite client with ($MEMORY) MBs of memory and a timeout of ($TIMEOUT)"

  local _seeds=()   # Array containing all seeds for the experiment
  local _seed_value # Seed value for creating and loading the seeds file

  # Seeds file creating or loading
  if [ ! -f $SEEDS_FILE ]; then # Create random seeds file if it doesn't exist and load it in memory
    _log "Creating random seeds file ($SEEDS_FILE)"
    _log "Store this file with your experiment to replicate the experiment later"

    for i in $(seq 1 1 $_num_executions)
    do
      local _seed_value=$(od -vAn -N4 -t u4 < /dev/urandom | tr -d ' ')
      local _seeds+=("$_seed_value")
      echo "$_seed_value" >> $SEEDS_FILE
    done
  else # Load random seeds file in memory
    _log "Using existing seeds file ($SEEDS_FILE)"
    _log "REPLICATING EXPERIMENT"

    local _seeds_entries=$(wc -l < $SEEDS_FILE)
    (( _seeds_entries != $_num_executions )) && { _die "Number of entries ($_seeds_entries) in ($SEEDS_FILE) does not match the number of executions ($_num_executions)"; }

    while read _seed_value; do
      local _seeds+=("$_seed_value")
    done < $SEEDS_FILE
  fi

  local _execution=1 # The current execution

  # Define local variables
  local _round              # Round number
  local _project            # Project name
  local _class              # Full class name
  local _configuration_name # Name of configuration
  local _configuration      # User configuration of EvoSuite
  local _seed               # Seed

  for _round in $(seq 1 1 $ROUNDS) # Rounds loop
  do
    local _old_ifs=$IFS # Maintain the old separator
    IFS=','             # Set separator for CSV

    while read _project _class # Projects loop
    do
      while read _configuration_name _configuration # Configurations loop
      do
        local _seed_index=$((_execution - 1))
        local _seed=${_seeds[_seed_index]}

        # Run a single configuration of EvoSuite as a sub-process
        _run_evosuite "$_execution" "$_num_executions" "$_round" "$_configuration_name" "$_configuration" "$_project" "$_class" "$_seed"

        ((_execution++)) # Increment execution number

        # Wait when the program reaches the limit of parallel executions
        while [ $(jobs -p | wc -l) -ge $PARALLEL_INSTANCES ]
        do
      	  wait -n        # Wait for the first sub-process to finish
      	  local _code=$? # Exit code of sub-process
        done
      # Load configurations file without header
      done < <(tail -n +2 $CONFIGURATIONS_FILE)
    # Load projects file without header
    done < <(tail -n +2 $PROJECTS_FILE)

    IFS=$_old_ifs # Restore old separator
  done

  wait     # Wait for all sub-processes (individual EvoSuite configurations) to be done
  sleep 30 # Allow some extra time for the sub-process to write out the files

  _log "Experiment done with ($_num_executions) total executions"
  _log "Store $CONFIGURATIONS_FILE, $PROJECTS_FILE, $SEEDS_FILE, and the 'projects' directory with the results to replicate the experiment"
}

# Start experiment
_run_experiment
