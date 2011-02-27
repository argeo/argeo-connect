# Copyright 2008 Mathieu Baudier
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

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
