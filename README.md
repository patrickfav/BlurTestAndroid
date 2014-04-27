BlurTestAndroid
===============

This is a simple benchmark and showcase app on whats possible with blurring in Android 2014. Noteably this app uses Android's Renderscript v8 support library for fast blurring.

Blur Benchmark
------------
Here you chose, the image sizes, blur radii and algorithm you want to benchmark. Finally you decide the accuarcy by providing the rounds each setting (image, radius, algorithm) is blurred. Be warned, some java implementations are very slow, so you could wait a bit with a high round count.

![benchmark view](https://lh5.ggpht.com/3GNMlpFqHhuEEKiuTLXdOL32OMY158wNal7Xwn7JY26BCjUsPxqzp_gAN2ZfZ39oiCg=h900-rw)

After running some benchmaks you see the results list, where you can click on each element and see a diagramm on the length of each round. This also reveals that this benchmark is polluted by garbage collection

![results view](https://lh6.ggpht.com/-MmOjYnbWUeZhQzvXoWznoPADfr40Hz1nLcbOGofcQ6ebomHNDorgBLqpaLPAH-H5XPu=h900-rw)

Later you can examine the latest runs in a table view or comparative in a diagram with diffrent options on the values to see.
![diagrams](https://lh5.ggpht.com/Azu5iUmYOny-dGW89V4uYnBpp1dpv7AZgowVQEHN2x1sYoe4QU6zr5OI9ZGl8GTjgIM=h900-rw)



Static Blur
------------

Live Blur
------------
