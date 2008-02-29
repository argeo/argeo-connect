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
