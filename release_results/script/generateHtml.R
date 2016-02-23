source("util.R")

VERSION = "1_0_3"

GENERATED_FILES = paste("../generated_files","/",VERSION,sep="")
SELECTION_FILE = paste("../generated_files","/","selection.txt",sep="")
PROBLEM_FILE = paste(GENERATED_FILES,"/","problems.txt",sep="")
CS_FILE = "sf110.txt"

DATA_DIR = paste("../data","/",VERSION,sep="")

ALL_ZIP_FILE = paste(DATA_DIR,"/","compressedData_all.zip",sep="")
SELECTION_ZIP_FILE = paste(DATA_DIR,"/","compressedData_selection.zip",sep="")
PROBLEM_ZIP_FILE = paste(DATA_DIR,"/","compressedData_problems.zip",sep="")


FIGURE_CLASSES = "barplotClasses.jpeg"
FIGURE_PROJECTS = "barplotProjects.jpeg"


processDataAll <- function(){
	dt = processData(DATA_DIR,CS_FILE,ALL_ZIP_FILE)
	return(dt)
}

processDataSelection <- function(){
	dt = processData(DATA_DIR,SELECTION_FILE,SELECTION_ZIP_FILE)
	return(dt)
}

processDataProblem <- function(){
	dt = processData(DATA_DIR,PROBLEM_FILE,PROBLEM_ZIP_FILE)
	return(dt)
}


createProblemFile <- function(){
	dt <- read.table(gzfile(ALL_ZIP_FILE),header=T)
	zeroCoverageClasses(dt,PROBLEM_FILE)
}

htmlAll <- function(){
	html(ALL_ZIP_FILE)
}

htmlSelection <- function(){
	html(SELECTION_ZIP_FILE)
}

htmlProblems <- function(){
	html(PROBLEM_ZIP_FILE)
}


makeSelection <- function(){
	sampleStratifiedSelection(CS_FILE,1000,SELECTION_FILE)
}

html <- function(zipFile){

	dir.create(GENERATED_FILES)

	dt <- read.table(gzfile(zipFile),header=T)
	figures(dt)

	classes = length(unique(dt$TARGET_CLASS))
	projects = length(unique(dt$group_id))
	version = gsub("_",".",VERSION)
	budget = unique(dt$search_budget) / 60

	line = formatC(100 * mean(dt$LineCoverage),digits=1,format="f")
	branch = formatC(100 * mean(dt$BranchCoverage),digits=1,format="f")
	mutation = formatC(100 * mean(dt$WeakMutationScore),digits=1,format="f")
	output = formatC(100 * mean(dt$OutputCoverage),digits=1,format="f")
	exception = formatC(mean(dt$Implicit_MethodExceptions),digits=1,format="f")
	tests = formatC(mean(dt$Size),digits=1,format="f")

	PAGE = paste(GENERATED_FILES,"/results.html",sep="")
	unlink(PAGE)
	sink(PAGE, append=TRUE, split=FALSE)

	cat("<!DOCTYPE html> \n")
	cat("<html> \n")
	cat("	<head> \n")
	cat("		<meta name='viewport' content='width=device-width, initial-scale=1'> \n")
	cat("		<link rel='stylesheet' href='http://code.jquery.com/mobile/1.4.5/jquery.mobile-1.4.5.min.css' /> \n")
	cat("		<script src='http://code.jquery.com/jquery-2.0.3.min.js'></script>\n")
	cat("		<script src='http://code.jquery.com/mobile/1.4.5/jquery.mobile-1.4.5.min.js'></script>\n")
	cat("       <style type='text/css'> .ui-page {margin: 40px !important;} </style>\n")
	cat("	</head> \n")
	cat("	<body> \n")
	cat("		<h1>Results for version ",version,"</h1> \n")
	cat("		<p>Data results on ",classes," classes out of ",projects," projects. Search budget ",budget," minutes per class.</p> \n" , sep="")
	cat("		<p>Barplots for line coverage:</p>\n")
	cat("		<div> \n")
	cat("			<img src='",FIGURE_CLASSES,"' alt='Coverage per class' />\n", sep="")
	cat("			<img src='",FIGURE_PROJECTS,"' alt='Coverage per project' /> \n", sep="")
	cat("		</div> \n")
	cat("		<p>Average values for the different testing results: </p> \n")
	cat("		<ul> \n")
	cat("		<li>","Line:       ",line,"%</li> \n",sep="")
	cat("		<li>","Branch:     ",branch,"%</li> \n",sep="")
	cat("		<li>","Mutation:   ",mutation,"%</li> \n",sep="")
	cat("		<li>","Output:     ",output,"%</li> \n",sep="")
	cat("		<li>","Exceptions: ",exception,"</li> \n",sep="")
	cat("		<li>","# of Tests: ",tests,"</li> \n",sep="")
	cat("		</ul> \n")
	cat("		<p>In detail results: </p> \n")
	table(dt)
	cat("	</body>\n")
	cat("</html>\n")

	sink()
}

table <- function(dt){

	classes = unique(dt$TARGET_CLASS)
	projects = unique(dt$group_id)

	cat("		<div data-role='collapsible-set'> \n", sep="")
	cat("			<div data-role='collapsible'>\n", sep="")
	allCov = formatC(100 * mean(dt$LineCoverage),digits=1,format="f")
	cat("				<h4> All ",length(classes)," classes: average ",allCov,"% line coverage</h4>\n", sep="")
	cat("				<div data-role='collapsible-set'> \n", sep="")
	for(p in projects){
		projCov = formatC(100 * mean(dt$LineCoverage[dt$group_id==p]),digits=1,format="f")
		projClasses = sort(unique(dt$TARGET_CLASS[dt$group_id==p]))
		projName = getProjectName(p)
		cat("					<div data-role='collapsible'>\n", sep="")
		cat("						<h4> ",projName," (",length(projClasses)," classes): average ",projCov,"% line coverage</h4>\n", sep="")
		cat("						<table data-role='table'  class='ui-responsive table-stripe'> \n")
		cat("							<thead><tr><th>Class Name</th><th>Line</th><th>Branch</th><th>Mutation</th><th>Output</th><th>Exceptions</th><th># Tests</th></tr></thead> \n")
		cat("							<tbody> \n")
		for(class in projClasses){
			line = formatC(100 * mean(dt$LineCoverage[dt$group_id==p & dt$TARGET_CLASS==class]),digits=1,format="f")
			branch = formatC(100 * mean(dt$BranchCoverage[dt$group_id==p & dt$TARGET_CLASS==class]),digits=1,format="f")
			mutation = formatC(100 * mean(dt$WeakMutationScore[dt$group_id==p & dt$TARGET_CLASS==class]),digits=1,format="f")
			output = formatC(100 * mean(dt$OutputCoverage[dt$group_id==p & dt$TARGET_CLASS==class]),digits=1,format="f")
			exception = formatC(mean(dt$Implicit_MethodExceptions[dt$group_id==p & dt$TARGET_CLASS==class]),digits=1,format="f")
			tests = formatC(mean(dt$Size[dt$group_id==p & dt$TARGET_CLASS==class]),digits=1,format="f")

			cat("							<tr><td>",class,"</td><td>",line,"%</td><td>",branch,"%</td><td>",mutation,"%</td><td>",output,"%</td><td>",exception,"</td><td>",tests,"</td></tr>", sep="")
		}
		cat("							</tbody> \n")
		cat("						</table> \n")
		cat("           			</div> \n")
	}
	cat("           		</div> \n")
	cat("           </div> \n")
	cat("       </div> \n")
}

figures <- function(dt){

	projects = sort(unique(dt$group_id))
	projCov = c()
	for(p in projects){
		cov = mean( dt$LineCoverage [ dt$group_id==p ] )
		projCov = c(projCov, cov)
	}

	classes = unique(dt$TARGET_CLASS)
	classCov = c()
	for(class in classes){
		cov = mean( dt$LineCoverage [ dt$TARGET_CLASS==class ] )
		classCov = c(classCov, cov)
	}

	intervals = 10
	xProj  = rep(0,times=intervals)
	xClass = rep(0,times=intervals)
	labels = rep(0,times=intervals)

	for(i in 1:intervals){
		delta = 100 / intervals
		if(i==1){
			m = -1
		} else {
			m = (i-1)*delta*0.01
		}
		M = i*delta*0.01

		xProj[[i]] =   length(projCov[projCov>m & projCov<=M]) / length(projCov)
		xClass[[i]] =   length(classCov[classCov>m & classCov<=M]) / length(classCov)

		labels[[i]] = paste(M*100,"",sep="")
		labels[[i]] = paste(M*100,"",sep="")
	}


	jpeg(file=paste(GENERATED_FILES,"/",FIGURE_PROJECTS,sep=""),quality=100)
	barplot(xProj,names.arg=labels, xlab="Coverage Intervals %", ylab="Ratio of Projects", main=paste(length(projects)," Projects",sep=""))
	dev.off()

	jpeg(file=paste(GENERATED_FILES,"/",FIGURE_CLASSES,sep=""),quality=100)
	barplot(xClass,names.arg=labels, xlab="Coverage Intervals %", ylab="Ratio of Classes", main=paste(length(classes)," Classes",sep=""))
	dev.off()
}


