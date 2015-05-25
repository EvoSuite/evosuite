processData <- function(dataDir,csFile,zipFile){

	dt = gatherAllTables(dataDir);
	dt = addMissingClasses(dt,csFile)

	write.table(dt, file = gzfile(zipFile))

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



zeroCoverageClasses <- function(dt,outputFile){

	projects = sort(unique(dt$group_id))

	TABLE = outputFile
	unlink(TABLE)
	sink(TABLE, append=TRUE, split=TRUE)

	for(proj in projects){
		classes = sort(unique(dt$TARGET_CLASS[dt$group_id==proj & dt$LineCoverage==0]))

		for(cl in classes){
			cat(proj,"\t",cl,"\n")
		}
	}

	sink()
}

sampleStratifiedSelection <- function(pathToClassDescription, n, outputFile){

	mp <-  read.table(pathToClassDescription,header=F)
	PROJ_COLUMN = 1
	CLASS_COLUMN = 2

	sampled = c()

	projects = sort(unique(mp[,PROJ_COLUMN]))
	totalProj = length(projects)

	final = c()

	perProj = floor(n / totalProj)
	if(perProj > 0){ # at least one per project
		for(p in projects){
			mask = mp[,PROJ_COLUMN] == p
			classes = mp[mask , CLASS_COLUMN]

			if(length(classes) <= perProj){
				selected = classes
			} else {
				selected = sample(classes,perProj,replace = FALSE)
			}

			for(sel in selected){
				sampled = c(sampled , paste(p," \t ",sel,sep=""))
			}
		}
	}

	## fill the remaining at random
	while(length(sampled) < n){

		p = sample(projects,1)
		mask = mp[,PROJ_COLUMN] == p
		classes = mp[mask , CLASS_COLUMN]
		sel = sample(classes,1)
		val = paste(p," \t ",sel,sep="")

		if(any(sampled == val)){
			next()
		}

		sampled = c(sampled , val)
	}

	sampled = sort(sampled)

	TABLE = outputFile
	unlink(TABLE)
	sink(TABLE, append=TRUE, split=TRUE)

	for(row in sampled){
		cat(row,"\n")
	}

	sink()
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