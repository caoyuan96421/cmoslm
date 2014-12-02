
set term pngcairo size 1024,768 font ",15" enhanced

set output "annealing.png"


set xlabel "Simulation steps"
set ylabel "Leakage current (nA)"

set yrange [0:600]
set xrange [0:100]
set y2range [-1:511]

set ytics nomirror
set y2tics 0,64,512 format "0x%03X"
set y2tics add ("0x1FF" 511)
set y2label "Input vector"

plot "FA4.dat" using ($3*1e9) with step title "Leakage", "FA4.dat" using ($1*1e9) with line title "T", "FA4.dat" using 2 with step title "Input vector" axis x1y2


set output