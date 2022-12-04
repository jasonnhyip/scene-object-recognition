Android application for object and scene recognition
=========
The project is built based on the Hello JNI Android sample that uses JNI to call C code from a Android Java Activity.

It aims to provide information of objects and scene in the environment for the visually impaired people.

YOLOv3-tiny in C is modified to pass object information through JNI to the Android app in Java for notification and scene recognition. The scene is classified by Support Vector Machine with the object information only.

YOLOv3-tiny: https://github.com/pjreddie/darknet (original source code) and https://github.com/AlexeyAB/darknet (forked version with training improvement)
A YOLOv3-tiny model pre-trained on COCO 2014 dataset is used. It can recognize 80 object categories.

MIT Indoor 67 dataset is used for training the SVM scene recognition model. A feature vector containing the object frequency for each image in scene dataset is obtained for scene classification using SVM.

![Program Flow](/images/uml2.jpg)


Improvement Made According to the Feedback
--------------
Object Position Notification
![Position](/images/Position.png)


Pre-requisites
--------------
- Android Studio 2.2+ with [NDK](https://developer.android.com/ndk/) bundle.
- Python - v3.6.2
- Microsoft Visual Studio 2017
- Android Studio – v3.3
- Android NDK – v19.0.5232133
- CMake – v3.14.1
- CUDA – v10.0
- OpenCV – v3.4.0
- cuDNN – v7.4.1
- TensorFlow - v1.13.1
- TensorBoard - v1.12.2
- LIBSVM – v3.23
- Gnuplot – v5.2


Screenshots
-----------
![Screenshot](/images/Screenshot_20190405-022149_Yolo.jpg)

