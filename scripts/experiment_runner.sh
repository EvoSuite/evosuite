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
  echo "(LOGGING) ${FUNCNAME[1]:-unknown}: $*"
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
  -p <parallel_instances>  limit for the number of parallel executions (default: 1)
  -r <rounds>              number of rounds to execute each experiment (default: 1)
  -t <timeout>             amount of time before EvoSuite process is killed (default: 10m)

Examples:
  $0 configurations.csv projects.csv
  $0 -r 10 -p 4 configurations.csv projects.csv
  $0 -t 1h configurations.csv projects.csv

EOF
}

# Default values
PARALLEL_INSTANCES=1 # The amount of parallel executions the experiment should use
ROUNDS=1             # The number of rounds to perform of the experiment
TIMEOUT=10m          # The amount of time before the EvoSuite process is killed

# Argument parsing
while getopts ":hp:r:t:" o; do
  case "${o}" in
    h)
      _usage
      exit 0
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
[ ! -f $CONFIGURATIONS_FILE ] && { _die "$CONFIGURATIONS_FILE file not found"; }
[ ! -f $PROJECTS_FILE ] && { _die "$PROJECTS_FILE file not found"; }

PROJECTS_DIRECTORY=$(pwd)/projects # Directory where the projects are stored
RESULTS_DIRECTORY=$(pwd)/results   # Directory where the results should be stored

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

  local _project_class_path=$(_get_project_class_path "$_project") # Project class path

  # Output locations
  local _report_dir=$RESULTS_DIRECTORY/$_configuration_name/$_project/reports/$_round
  local _test_dir=$RESULTS_DIRECTORY/$_configuration_name/$_project/tests/$_round
  local _log_dir=$RESULTS_DIRECTORY/$_configuration_name/$_project/logs/
  local _log_file=$RESULTS_DIRECTORY/$_configuration_name/$_project/logs/$_round

  mkdir -p $_log_dir # Create log directory

  # Convert configuration into seperate items
  local _old_ifs=$IFS; IFS=' '; read -ra _user_configuration_array <<< "$_user_configuration"; IFS=$_old_ifs;

  _log "Execution ($_execution / $_num_executions): Running round ($_round) of configuration ($_configuration_name) for class ($_class) in project ($_project)"

  # Run EvoSuite in background
  timeout -k $TIMEOUT $TIMEOUT /usr/bin/env java -Xmx4G -jar /evosuite-bin/evosuite.jar \
  -mem 2500 \
  -Dconfiguration_id="$_configuration_name" \
  -Dgroup_id="$_project" \
  -projectCP "$_project_class_path" \
  -class "$_class" \
  -seed "$_round" \
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
  [ -d $RESULTS_DIRECTORY ] && { _die "$RESULTS_DIRECTORY is present, cancelling experiment"; }

  local _num_configurations=$(( $(wc -l < $CONFIGURATIONS_FILE) - 1 ))      # Number of configurations in experiment
  local _num_classes=$(( $(wc -l < $PROJECTS_FILE) - 1 ))                   # Number of classes in experiment
  local _num_executions=$(($ROUNDS * $_num_configurations * $_num_classes)) # Number of total executions

  _log "Start experiment with ($_num_executions) total executions"
  _log "($ROUNDS) rounds"
  _log "($_num_configurations) configurations"
  _log "($_num_classes) classes"

  local _execution=1 # The current execution

  # Define local variables
  local _round              # Round number
  local _project            # Project name
  local _class              # Full class name
  local _configuration_name # Name of configuration
  local _configuration      # User configuration of EvoSuite

  for _round in $(seq 1 1 $ROUNDS) # Rounds loop
  do
    local _old_ifs=$IFS # Maintain the old separator
    IFS=','             # Set separator for CSV

    while read _project _class # Projects loop
    do
      while read _configuration_name _configuration # Configurations loop
      do
        # Run a single configuration of EvoSuite as a sub-process
        _run_evosuite "$_execution" "$_num_executions" "$_round" "$_configuration_name" "$_configuration" "$_project" "$_class"

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
}

# Start experiment
_run_experiment
