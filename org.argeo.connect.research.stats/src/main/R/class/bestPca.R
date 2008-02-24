source("src/main/R/class/common.R")
source("src/main/R/class/eGvt2008.R")

minCol <- 5
d <- data.matrix(na.omit(eGvt2008.data()))

cols <- c()
inertias <- c()
screeTests <- c()
pcas <- c()
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
					pc2[[indx]] <- pca$sdev[2]
					screeTests[[indx]] <- st
					inertias[[indx]] <- inert
					
					print(col)
					print(pca$sdev)
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
dim(pc2) <- length(pc2)

df <- data.frame(cols=cols,screeTest=screeTests,inertia=inertias,pc2=pc2,pca=pcas)

bestPcaInertia <- df$pca[[which.max(df$inertia)]]
bestPcaPc2 <- df$pca[[which.max(df$pc2)]]
