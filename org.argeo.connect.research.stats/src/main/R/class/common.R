computePca <- function(dataNum){
	pr <- prcomp(na.omit(dataNum),scale=TRUE,retx=TRUE)
	pr
}

computePcaDesc <- function(dataNum){
	pr <- computePca(dataNum)
	print(summary(pr))
	biplot(pr)
	pr
}

readCsvData <- function(datapath, colCount){
	colClasses <- character(length=(colCount+1))
	colClasses[1] <- "character"
	colClasses[2:length(colClasses)] <- "numeric"
	data <- read.csv(datapath,header=TRUE,row.names=1,sep=",",dec=".",colClasses=colClasses)
	dataNum <- data.matrix(data)
	dataNum
}

inertia <- function(pca){
	vr <- (pca$sdev)^2
	inert <- sum(vr[1:2])/sum(vr)
	inert
}

screeTest <- function(pca){
	v <- pca$sdev^2
	a <- numeric(length=length(v)-2)
	for(k in 1:length(a)){
		a[k] <- (v[k]-2*v[k+1]+v[k+2])
	}
	# is the second eigenvalue within the acceptable ones?
	screeTest <- (which.max(a) > 1)
	screeTest
}