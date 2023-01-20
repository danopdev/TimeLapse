# TimeLapse

Create timelapse videos from another video or a series of photos (the photos will be sorted alphabetically).

* [Main screen](#main-screen)
  * [Open](#open)
  * [Parameters](#parameters)
  * [Generate and preview](#generate-and-preview)
  * [Save](#save)
* [Examples](#examples)
  * [Smooth](#smooth)
  * [Transition](#transition)

## Main screen
![](screenshots/main_screen_small.jpg)


### Open
![](screenshots/source.jpg)

Allow to open a videso a series of photos or a photo folder.

### Parameters
![](screenshots/parameters.jpg)

* Speed: allow to keep only one of "speed" frames. Example: 3x means use only one of 3 frames. 1x means use all frames.
* Smooth: allow to do the moving average of N frames. 1x means disabled.
* Transition: make a linear transition between 2 consecutive frames. 1x means disabled
* Output FPS

### Generate and preview
![](screenshots/generate.jpg)

Generate the video based on the current parametes.
Once generated the Play / Stop buttons will be enabled and allow to see the result.

### Save
![](screenshots/menu_save.jpg)

Allow to save the current generated video to Gallery.

## Examples

### Smooth

Original
https://user-images.githubusercontent.com/7062741/213679622-93adda9e-bbf1-48a0-a1e2-2852dea5c259.mp4

Smooth 10x
https://user-images.githubusercontent.com/7062741/213680133-d53dbd9d-dbcf-4fb3-bee8-278dbb0bfc72.mp4
 
### Transition

Input images
1 | 2 | 3 | 4
-- | -- | -- | --
![](examples/transition/input_1_small.jpg) | ![](examples/transition/input_2_small.jpg) | ![](examples/transition/input_3_small.jpg) | ![](examples/transition/input_4_small.jpg)

Crop
https://user-images.githubusercontent.com/7062741/213680470-6aa47ff3-3bdf-441e-b035-b6b0e459885e.mp4

No Crop
https://user-images.githubusercontent.com/7062741/213680565-9cc22183-58d9-485b-99c0-36a1046e2c43.mp4


