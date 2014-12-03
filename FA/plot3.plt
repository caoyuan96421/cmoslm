set term pngcairo size 640,480 font ",15" enhanced

e = 20
n = 0

do for [i=0:11571:e]{
	reset 
	n = n + 1
	set output "anime/anime".sprintf("%04d",n).".png"
	set multiplot
	
	unset key
	set xlabel "Simulation steps"
	set ylabel "Leakage current (nA)"

	set yrange [*:*]
	set xrange [0:*]
	set autoscale xfix

	plot "FA16-n.dat" using ($3*1e9) with step title "Leakage", "FA16.dat" using (i):($3*1e9) index i with point pt 7 ps 2 lc 0
	
	reset
	set lmargin screen 0.2
	set rmargin screen 0.9
	set tmargin screen 0.99
	set bmargin screen 0.96
	unset key
	unset xtics
	unset ytics
	unset border
	#set xlabel "Simulation steps"
	#set ylabel "Leakage current (nA)"

	plot "FA16.dat" using 1:(0):2 index i with labels
	
	unset multiplot
	set output
}
