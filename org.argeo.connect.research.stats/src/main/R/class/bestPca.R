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
					#if(st){
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
					#}
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
	#pcRatio <- pc2/pc1
	
	#df <- data.frame(cols=cols,screeTest=screeTests,inertia=inertias,pc1=pc1,pc2=pc2,pca=pcas,pcRatio=pcRatio)
	
	#bestPcaInertia <- df$pca[[which.max(df$inertia)]]
	#bestPcaPc2 <- df$pca[[which.max(df$pc2)]]
	#bestPcaPcRatio <- df$pca[[which.max(df$pcRatio)]]
	
	#split.screen(c(2,2),erase = TRUE)
	#screen(n=1)
	#biplot(bestPcaInertia)
	#screen(n=2)
	#biplot(bestPcaPc2)
	#screen(n=3)
	#biplot(bestPcaPcRatio)
	
	# maximise inertia
	bestIndx <- which.max(inertias)
	print("Best index:")
	print(bestIndx)
		
	res <- list(pca=pcas[[bestIndx]],cols=cols[[bestIndx]],rawData=rawData)
	res <- prepareClassRes(res)
	res
}