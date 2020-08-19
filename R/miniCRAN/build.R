# template

# renv
if (!require(renv)) install.packages('renv')
library('renv')
# librarian
if (!require(librarian)) install.packages('librarian')
library('librarian')


# librarian - install and load all packages from any repo in one command
librarian::shelf(
  here
)


##### miniCRAN
if (!require(renv)) install.packages('miniCRAN')
library('miniCRAN')
tools:::write_PACKAGES("devtools::install_github("andrie/miniCRAN", lib="/datadrive/data_dir/code-envs/packages/R/src/contrib")

# set repo to use
repo <- "https://cran.rstudio.com"

# path for downloaded packges
minicran_pth <- '/Users/alek/MSHP/shared_drives/git_repos/packages/R/miniCRAN'


### build minicran repo
# list of packages to install
minicran_pkgs <- c("foreach", "devtools", "miniCRAN", "tidyverse", "askpass","assertthat","backports","base64enc","BH","cli","crayon","curl","digest","dplyr","ellipsis","evaluate","fansi","glue","gtools","htmltools","httr","IRdisplay","IRkernel","jsonlite","magrittr","mime","openssl","pbdZMQ","pillar","pkgconfig","plogr","purrr","R6","Rcpp","repr","RJSONIO","rlang","sys","tibble","tidyselect","utf8","uuid","vctrs","zeallot"
,"boot","class","cluster","codetools","foreign","KernSmooth","lattice","MASS","Matrix","mgcv","nlme","nnet","rpart","spatial","survival")

# grab pkg dependencies
pkgList <- pkgDep(minicran_pkgs, repos = repo, type = "source", suggests = TRUE)

# build the minicran repo
makeRepo(pkgs= pkgList, path= minicran_pth)



### add packages to minicran repo
# list of packages to add
pkgs_to_add <- c("here", "httpuv", "googlesheets4", "googledrive", "reticulate"
                 , "tidyverse","lubridate", "stringr", "readr"
                 , "jsonlite"
                  , "rJava"
                  , "RJDBC"
                  , "sqldf"
                  , "openxlsx" #for reading/writing Excel without java. Can format output landing in Excel
                  ##Transformation
                  , "plyr" #load this before dplyr, and better yet figure out how to get away from ddply summary function
                  , "dplyr"
                  , "reshape2" #for dcast to create pivot tables
                  ##Presentation - plots, tables
                  , "htmlTable"    #for nicely formatted tables
                  , "pivottabler"  #for pivottables... but see also rpivottable and reshape2::dcast
                  , "rpivotTable"
                  , "ggplot2"      #for plotting
                  , "egg"          #for multiple plots on a page
                  ## Sorry -- now I need to pull this in
                  ## tidyverse captures dplyr, plyr, ggplot2... and others
                  ## however, having issues with masking functions so it's last in line now before custom
                  , "tidyr"    #for "spread" function in the transpose reporting step
                  , "tibble"    #for "add_column"
                  , "scales"
                  , "clipr" ## For ease of cut/paste to other programs
                 , "renv"
                 , "MASS", "cluster", "codetools", "foreign", "KernSmooth",
                 "lattice", "Matrix", "mgcv", "nlme", "nnet", "rpart", "spatial",
                 "survival", "vtable", "hglm", "bayestestR",
                 "dynr", "officer", "rstan", "LassoSIR",
                 "foreach", "plumber", "covTestR", "broom",
                 "remotes", "styler", "diffobj", "sdols",
                 "smooth", "BART",  "tidytext", "pivottabler",
                 "DatabaseConnector", "workflowr", "fpeek",
                 "envnames", "memor", "measures", "rcheology", "packrat",
                 "anyLib", "jetpack", "librarian", "packagefinder", "binman"
                  )

# grab pkg dependencies
pkgList <- pkgDep(pkgs_to_add, type = "source", suggests = TRUE)


pkgList <- pkgDep("pivottabler", type = "source", suggests = TRUE)


#addPackage("Matrix", path = minicran_pth, type = "source")
addPackage(pkgs = pkgList, path = minicran_pth, type = "source")
