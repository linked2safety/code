
cd $(dirname $0)
R --slave --args $1 $2 $3 < assocBoxplot.R
