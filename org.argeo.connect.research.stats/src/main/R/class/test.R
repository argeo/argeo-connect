compute <- function(datapath){

data <- na.omit(data.matrix(read.csv(datapath,header=TRUE,row.names=1)))
pr <- prcomp(data,scale=TRUE,retx=TRUE)
summary(pr)
print(pr->x)
biplot(pr)

}

compute("src/main/data/class/UN-DESA-eGvt-2008/UN-DESA-eGvt-2008.csv")