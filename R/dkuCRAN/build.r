
install.packages("miniCRAN")
library("miniCRAN")

revolution <- c(CRAN = getOption("minicran.mran"))

dkupth='/Users/alek/MSHP/shared_drives/git_repos/packages/R/dkuCRAN'
# add Dataiku versions
dataikuVers <- data.frame(package = c("askpass","assertthat","backports","base64enc","BH","cli","crayon","curl","digest","dplyr","ellipsis","evaluate","fansi","glue","gtools","htmltools","httr","IRdisplay","IRkernel","jsonlite","magrittr","mime","openssl","pbdZMQ","pillar","pkgconfig","plogr","purrr","R6","Rcpp","repr","RJSONIO","rlang","sys","tibble","tidyselect","utf8","uuid","vctrs","zeallot"
,"base","boot","class","cluster","codetools","compiler","datasets","foreign","graphics","grDevices","grid","KernSmooth","lattice","MASS","Matrix","methods","mgcv","nlme","nnet","parallel","rpart","spatial","splines","stats","stats4","survival","tcltk","tools","utils"
),
                    version = c("1.0","0.2.1","1.1.5","0.1-3","1.69.0-1","1.1.0","1.3.4","4.2","0.6.21","0.8.3","0.3.0","0.14","0.4.0","1.3.1","3.8.1","0.4.0","1.4.1","0.7.0","1.0.2","1.6","1.5","0.7","1.4.1","0.3-3","1.4.2","2.0.3","0.2.0","0.3.2","2.4.0","1.0.2","1.0.1","1.3-1.3","0.4.0","3.3","2.1.3","0.2.5","1.1.4","0.1-2","0.2.0","0.1.0"
,"3.6.0","1.3-22","7.3-15","2.0.8","0.2-16","3.6.0","3.6.0","0.8-71","3.6.0","3.6.0","3.6.0","2.23-15","0.20-38","7.3-51.4","1.2-17","3.6.0","1.8-28","3.1-139","7.3-12","3.6.0","4.1-15","7.3-11","3.6.0","3.6.0","3.6.0","2.44-1.1","3.6.0","3.6.0","3.6.0"
                    ),
                    stringsAsFactors = FALSE)
pkgs <- dataikuVers$package
addOldPackage(pkgs, path = dkupth, vers = dataikuVers$version,repos = revolution, type = "source")



