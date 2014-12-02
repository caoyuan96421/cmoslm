
set term pngcairo size 1024,768 font ",15" enhanced

set output "FA4.png"

set multiplot

set xlabel "Input vector (A_3A_2A_1A_0B_3B_2B_1B_0C_0)"
set ylabel "Leakage current (nA)"

set yrange [0:*]
set xrange [0:511]

set xtics 0,64,512 format "0x%03X"
set xtics add ("0x1FF" 511)

plot "FA4.txt" using 1:($2*1e9) with step notitle, '-' with points pt 6 ps 2 lc 3 notitle
0 307
end

reset

set lmargin screen 0.5
set rmargin screen 0.9
set tmargin screen 0.65
set bmargin screen 0.3

set xlabel "Leakage (nA)" offset 0,0.6
set ylabel "Count" 


n=50
max=580
min=280
width = (max-min)/n
hist(x) = width * floor(x / width) + width / 2.0
set xrange [min:max]
set boxwidth width*0.9

plot 'FA4.txt' using (hist($2*1e9)):(1.0) smooth freq with boxes lc 4 notitle 

unset multiplot


set output