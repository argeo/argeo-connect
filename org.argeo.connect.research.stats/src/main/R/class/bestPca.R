source("src/main/R/class/common.R")

bestPca <- function(rawData){
	minCol <- 3
	d <- data.matrix(na.omit(rawData))
	
	cols <- c()
	inertias <- c()
	screeTests <- c()
	pcas <- c()
	pc1 <- c()
	pc2 <- c()
	
	indx <- 1
	for(k in minCol:dim(d)[2]){
		cmb <- combn(dim(d)[2],k)
		print(k)
		print(dim(cmb)[2])
		for (x in 1:dim(cmb)[2]) {
			col <- cmb[,x]
			pca <- computePca(d[,col])
			# Kaiser criterion
			if(pca$sdev[2] >= 1){
				inert <- inertia(pca)
				if(inert >= 0.9){
					st <- screeTest(pca)		
					cols[[indx]] <- col
					pcas[[indx]] <- pca
					pc1[[indx]] <- pca$sdev[1]
					pc2[[indx]] <- pca$sdev[2]
					screeTests[[indx]] <- st
					inertias[[indx]] <- inert
					
					print(indx)
					print(col)
					print(inert)
					print(st)
					
					indx <- indx + 1
				}
			}
		}
	}
	
	dim(cols) <- length(cols)
	dim(pcas) <- length(pcas)
	dim(screeTests) <- length(screeTests)
	dim(inertias) <- length(inertias)
	dim(pc1) <- length(pc1)
	dim(pc2) <- length(pc2)
	
	# maximise inertia
	bestIndx <- which.max(inertias)
	print("Best index:")
	print(bestIndx)
		
	res <- list(pca=pcas[[bestIndx]],cols=cols[[bestIndx]],rawData=rawData, inertias=inertias)
	res <- prepareClassRes(res)
	res
}

bestPcaCheck <- function(classRes){
	print("Best inertia:")
	print(inertia(classRes$pca))
	print("Active variables:")
	print(colnames(classRes$scaled[,classRes$cols]))
	print("Passive variables:")
	print(colnames(classRes$scaled[,-classRes$cols]))
	print("Mean Inertia:")
	print(mean(classRes$inertias))
	print("Inertias standard deviation:")
	print(sd(classRes$inertias))
	
	plot(classRes$inertias,type="h",ylab="Inertias",main="Inertias computed during bestPca algorithm")
}