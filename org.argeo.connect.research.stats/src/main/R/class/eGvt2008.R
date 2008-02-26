source("src/main/R/class/common.R")

eGvt2008.data <- function (){
	dataPath <- "src/main/data/class/UN-DESA-eGvt-2008/UN-DESA-eGvt-2008.csv"
	d <- readCsvData(dataPath,11)
	d
}

eGvt2008.bestPca <- function(){
	# as found by previously running bestPca algorithm
	cols <- c(2,5,8,9,11)
	rawData <- eGvt2008.data()
	pca <- computePca(rawData[,cols])
	res <- list(pca=pca,cols=cols,rawData=rawData)
	res <- prepareClassRes(res)
	res
}

eGvt2008.dataIndex <- function (){
	d <- eGvt2008.data()[,c(1,2,3,4,12)]
	d
}

eGvt2008.dataRaw <- function (){
	#d <- eGvt2008.data()[,c(5,6,7,8,9,10,11)]
	d <- eGvt2008.data()[,c(5,6,7,8,9,10,11)]
	d
}

