BlurTestAndroid
===============

This is a simple benchmark and showcase app on whats possible with blurring in Android 2014. Noteably this app uses Android's Renderscript v8 support library for fast blurring.

Blur Benchmark
------------
Here you chose, the image sizes, blur radii and algorithm you want to benchmark. Finally you decide the accuarcy by providing the rounds each setting (image, radius, algorithm) is blurred. Be warned, some java implementations are very slow, so you could wait a bit with a high round count.

![benchmark view](https://raw.github.com/patrickfav/BlurTestAndroid/blob/master/misc/readme/readme_screen01.png)

After running some benchmaks you see the results list, where you can click on each element and see a diagramm on the length of each round. This also reveals that this benchmark is polluted by garbage collection

![results view](https://raw.github.com/patrickfav/BlurTestAndroid/blob/master/misc/readme/readme_screen02.png)

![diagrams](https://raw.github.com/patrickfav/BlurTestAndroid/blob/master/misc/readme/readme_screen04.png)


Later you can examine the latest runs in a table view or comparative in a diagram with diffrent options on the values to see.
![diagrams](https://raw.github.com/patrickfav/BlurTestAndroid/blob/master/misc/readme/readme_screen03.png)


Live Blur
------------
This is a viewpager with a life blur under the actionbar and on the bottom. Live blur means, that blur views get updated
when the view changes (so viewpager, listview or scrollview gets scrolled). There are also diffrent settings, where you can change the algorithm, blur radius and and sample size (the higher, the smaller the used image).

![diagrams](https://raw.github.com/patrickfav/BlurTestAndroid/blob/master/misc/readme/readme_screen05.png)

How is this done?

Well, everytime the blur gets updated, the view will be drawn onto a bitmap (over a canvas) scaled according to the sample size, then cropped, blurred and set as background to the two views.

How can this be reasonable fast?

* use scaled down version of your view
* bitmap reference is reused to possible prevent some gc
* it has to be on the main thread, any multi threading (even with threadpool) is to slow (meaning the blur view lags behind a good 300ms) probably because of context switching

All in all this can be tweaked so that the blur method only takes around 8-10ms on most devices (with sample settings) which is the targeted runtime for smooth live blurring



Static Blur
------------
![diagrams](https://raw.github.com/patrickfav/BlurTestAndroid/blob/master/misc/readme/readme_screen06.png)


