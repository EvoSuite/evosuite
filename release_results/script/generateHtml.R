VERSION = "0_1_1"

GENERATED_FILES = paste("../generated_files","/",VERSION,sep="")
DATA_DIR = paste("../data","/",VERSION,sep="")
ZIP_FILE = paste(DATA_DIR,"/","compressedData.zip",sep="")

CS_FILE = "sf110.txt"

FIGURE_CLASSES = "barplotClasses.jpeg"
FIGURE_PROJECTS = "barplotProjects.jpeg"

html <- function(){

	dt <- read.table(gzfile(ZIP_FILE),header=T)
	figures(dt)

	classes = length(unique(dt$TARGET_CLASS))
	projects = length(unique(dt$group_id))
	version = gsub("_",".",VERSION)
	budget = unique(dt$search_budget) / 60

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
		cat("							<thead><tr><th>Class Name</th><th>Line</th><th>Branch</th><th>Mutation</th><th>Output</th><th>Exception</th><th># Tests</th></tr></thead> \n")
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


processData <- function(){
	dt = gatherAllTables(DATA_DIR);

	dt = addMissingClasses(dt,CS_FILE)

	write.table(dt, file = gzfile(ZIP_FILE))

	return(dt)
}

# split by '.', and return last token
getClassName <- function(class){
	k = strsplit(class,'.',fixed=TRUE)
	k = k[[1]]
	return(k[length(k)])
}

# split by "_"
getProjectName <- function(proj){
	k = strsplit(proj,'_',fixed=TRUE)
	k = k[[1]]
	return(k[2])
}

checkClasses <-function(dt, pathToClassDescription){

	mp <-  read.table(pathToClassDescription,header=F)
	PROJ_COLUMN = 1
	CLASS_COLUMN = 2

	### NOTE: this check is not 100% safe, as would not recongnize classes with same full name but different projects (although such possibility should be pretty rare)
	projects = unique(as.vector(mp[,PROJ_COLUMN]))
	foundClasses = unique(as.vector(dt$TARGET_CLASS))

	### check if class names are indeed unique
	allExpectedClasses = as.vector(mp[, CLASS_COLUMN])
	if(length(allExpectedClasses) != length(unique(allExpectedClasses))){
		dif = length(allExpectedClasses) - length(unique(allExpectedClasses))
		cat("WARNING: there are classes with same full names, # =",dif,"\n")
		### TODO if this really happens, add more logging
	}


	total_counter = 0

	summary = c()

	for(proj in projects){

		expectedClasses = unique(as.vector(mp[mp$V1==proj , CLASS_COLUMN]))
		numberOfExpected = as.numeric(length(expectedClasses))

		local_counter =  0

		for(class in expectedClasses){
			if(! any(foundClasses==class)){
				cat("Missing:",class," , in project ",proj,"\n")
				local_counter = local_counter + 1
			}
		}

		if(local_counter > 0 ){
			cat("\nProject",proj,"has",local_counter,"mismatches out of a total of",numberOfExpected,"classes  \n\n")
			total_counter = total_counter + local_counter
		}

		ratio = ((numberOfExpected-local_counter) / numberOfExpected) * 100
		info = paste(proj,"\t",ratio,"%\n",sep="")
		summary = c(summary,info)
	}

	cat(summary)
	cat("\n Total number of missing classes",total_counter,"out of",length(allExpectedClasses),"\n")
}

gatherAndSaveData <- function(directory,zipFile){
	cat("Loading data...",date(),"\n")

	dt = gatherAllTables(directory)

	cat("Data is loaded. Starting compressing. ",date(),"\n")

	write.table(dt, file = gzfile(zipFile))

	cat("Data is compressed and saved. Starting reading back. ",date(),"\n")

	table <- read.table(gzfile(zipFile),header=T)

	cat("Data read back. Done! ",date(),"\n")

	return(table)
}

gatherAllTables <- function(directory){
	allTables = NULL

	for(table in list.files(directory,recursive=TRUE,full.names=TRUE,pattern="statistics.csv") ){

		#cat("Reading: ",table,"\n")

		tryCatch( {dt <- read.csv(table,header=T)} ,
				error = function(e){
					cat("Error in reading table ",table,"\n", paste(e), "\n")
				})

		if(is.null(allTables)){
			allTables = dt
		} else {
			tryCatch( {allTables = rbind(allTables,dt)} ,
					error = function(e){
						cat("Error in concatenating table ",table,"\n", paste(e), "\n")
					})
		}
	}
	return(allTables)
}

addMissingClasses <- function(dt,pathToClassDescription){

	mp <-  read.table(pathToClassDescription,header=F)
	PROJ_COLUMN = 1
	CLASS_COLUMN = 2

	projects = unique(as.vector(mp[,PROJ_COLUMN]))

	totalMissing = 0

	for(p in projects){
		allExpectedClasses = unique(as.vector(mp[mp$V1==p, CLASS_COLUMN]))
		foundClasses = unique(as.vector(dt$TARGET_CLASS[dt$group_id==p]))

		diff = length(allExpectedClasses) - length(foundClasses)
		if(diff == 0){
			next()
		}
		cat("found ",diff," missing classes in ",p,"\n")
		totalMissing = totalMissing + diff

		missing = allExpectedClasses[!areInTheSubset(allExpectedClasses,foundClasses)]

		for(class in missing){
			row = dt[1,]
			for(i in 1:length(row)){
				if(is.numeric(row[[i]])){
					row[[i]] = 0
				}
			}
			row$TARGET_CLASS=class
			row$group_id=p
			row$search_budget=dt[1,]$search_budget

			dt = rbind(dt,row)
		}
	}

	cat("Total missing classes: ",totalMissing,"\n")
	return(dt)
}

### return a boolean vector, where each position in respect to x is true if that element appear in y
areInTheSubset <- function(x,y){

	### first consider vector with all FALSE
	result = x!=x
	for(k in y){
		result = result | x==k
	}
	return(result)
}