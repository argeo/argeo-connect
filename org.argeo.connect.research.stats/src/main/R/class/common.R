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

readCsvData <- function(datapath, colCount,sep=","){
	colClasses <- character(length=(colCount+1))
	colClasses[1] <- "character"
	colClasses[2:length(colClasses)] <- "numeric"
	data <- read.csv(datapath,header=TRUE,row.names=1,sep=sep,dec=".",colClasses=colClasses)
	dataNum <- data.matrix(data)
	dataNum
}

writeCsvData <- function(datapath, pca){
	write.csv(pca$x, file=datapath)
}

# CLASS RES

prepareClassRes <- function(classRes){
	scaled <- scale(na.omit(classRes$rawData))
	val <- classRes$pca$x[,c(1,2)]
	quant <- quantile(val)
	valLow <- subset(val,val[,1] <= quant["25%"])
	valMid <- subset(val,(val[,1] > quant["25%"]) & (val[,1] <= quant["75%"]))
	valHig <- subset(val,val[,1] > quant["75%"])
	
	res <- list(pca=classRes$pca,scaled=scaled,cols=classRes$cols,rawData=classRes$rawData,val=val,valLow=valLow,valMid=valMid,valHig=valHig,quant=quant,inertias=classRes$inertias)
}

allColsPca <- function(rawData){
	pca  <- computePca(rawData)
	cols <- c(1:dim(rawData)[2])
	res <- list(pca=pca,cols=cols,rawData=rawData)
	res <- prepareClassRes(res)
	res
}

summaryClassRes <- function(classRes){
	print("# Principal Component Analysis")
	print(classRes$pca)
	print(summary(classRes$pca))
	print("# Quantiles")
	print(classRes$quant)
	print("# Correlations")
	valCors(classRes)
	
	drawPca(classRes)
}

# COMPUTATIONS
valCor <- function(val){
	correlation <- cor(val[,1],val[,2])
	correlation
}

valCors <- function(classRes){
	print("Low")
	print(valCor(classRes$valLow))
	print("Mid")
	print(valCor(classRes$valMid))
	print("High")
	print(valCor(classRes$valHig))
	print("All")
	print(valCor(classRes$val))
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

inertia <- function(pca){
	vr <- (pca$sdev)^2
	inert <- sum(vr[1:2])/sum(vr)
	inert
}

# GRAPHICS
drawPca <- function(classRes){
	plot(classRes$val,pch=3,col="white")
	
	abline(v=classRes$quant["25%"])
	abline(v=classRes$quant["50%"])
	abline(v=classRes$quant["75%"])

	drawLabels(classRes$valLow,col="red")
	abline(line(classRes$valLow),col="red")
	drawLabels(classRes$valMid,col="blue")
	drawLabels(classRes$valHig,col="green")
	abline(line(classRes$valHig),col="green")
}

drawLabels <- function(val,col){
	names <- row.names(val)
	names <- substr(names,0,5)
	points(val,pch=3,col=col)
	text(val,names,cex=0.6,pos=4)
}

drawCorCircle <- function(classRes){
	siz <- 1.5
	
	pca <- classRes$pca
	c1 <- pca$x[,1]
	c2 <- pca$x[,2]
	
	# active variables
	corCirc <- pca$rotation[,c(1,2)] %*% diag(pca$sdev[c(1,2)])
	plot(corCirc,xlim=c(-siz,siz),ylim=c(-siz,siz),axes=TRUE,pch=3,asp=1,col="red",xlab="PC1",ylab="PC2",main="Correlation circle")
	text(corCirc,row.names(corCirc),cex=0.8,pos=1,col="red")
	
	# supplementary variables
	if(length(classRes$cols)<dim(classRes$rawData)[2]){
		scaledSupp <- classRes$scaled[,-classRes$cols]
		colCount <- dim(scaledSupp)[2]
		suppVars <- matrix(nrow=colCount,ncol=2)
		for(k in 1:colCount){
			suppVars[k,1] <- cor(c1,scaledSupp[,k])
			suppVars[k,2] <- cor(c2,scaledSupp[,k])
		}
		points(suppVars,pch=3)
		text(suppVars,colnames(scaledSupp),cex=0.8,pos=1)
	}

	symbols(0,0,circles=c(1),add=TRUE,inches=FALSE,asp=1)
	abline(v=0)
	abline(h=0)
}

drawInfraEdu <- function(classRes){
	plot(c(),xlim=c(0,1),ylim=c(0,1),xlab="EducationIndex",ylab="InfrastructureIndex",main="Education and Infrastructure in raw data")
	drawInfraEduVal(classRes$rawData,classRes$valLow,col="red")
	drawInfraEduVal(classRes$rawData,classRes$valMid,col="blue")
	drawInfraEduVal(classRes$rawData,classRes$valHig,col="green")
}

drawInfraEduVal <- function(rawData,val,col){
	points(rawData[rownames(val),c("EducationIndex","InfrastructureIndex")],pch=19,col=col,asp=1)
}
