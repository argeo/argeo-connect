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

readCsvData <- function(datapath, colCount,sep=","){
	colClasses <- character(length=(colCount+1))
	colClasses[1] <- "character"
	colClasses[2:length(colClasses)] <- "numeric"
	data <- read.csv(datapath,header=TRUE,row.names=1,sep=sep,dec=".",colClasses=colClasses)
	dataNum <- data.matrix(data)
	dataNum
}

readCsvDataFrame <- function(datapath, colCount,sep=","){
	colClasses <- character(length=(colCount+1))
	colClasses[1] <- "character"
	colClasses[2:length(colClasses)] <- "numeric"
	data <- read.csv(datapath,header=TRUE,row.names=1,sep=sep,dec=".",colClasses=colClasses)
	data
}

m.dfAll <- function (){
	dataPath <- "UN-DESA-eGvt-2008.csv"
	d <- readCsvDataFrame(dataPath,11)
	d
}

m.df <- function (){
	df <- m.dfAll()[,c(5,6,7,8,9,10,11,12)]
	df
}

m.cor <- function (){
	cor <- cor(na.omit(m.df()))
	cor
}

m.lm <- function(expr){
	df <- m.df()
	lmOut <- lm(expr, data = df)
	print(summary(lmOut));
	lmOut
}

m.plot <- function(expr){
	df= m.df()
	plot(expr, data = df )
}

m.plotLm <- function(expr){
	df= m.df()
	lmOut <- lm(expr, data = df)
	plot(expr, data = df )
	abline(lmOut$coef,lty=5)
}
