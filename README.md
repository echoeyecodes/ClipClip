# ClipClip
An android application for trimming videos into uniform compressed chunks. For example, a 2 minutes video can be split into 4 equal parts, each part being 30 seconds each. Really perfect for sharing videos on WhatsApp stories and more!

Consider this as a sequel to the media downloader, [Dobby](https://github.com/echoeyecodes/Dobby)😅. I call this a sequel to my previous project cos I mostly use dobby to download instagram videos, then export the video to ClipClip for splitting into parts before sharing on my WhatsApp status. It can get really spammy there lol.

The reason for working on this was actually as a result of the 30secs time restrictions for videos to be shared on WhatsApp stories. I found myself spending quite a lot of time correctly splitting the videos into parts before sharing with the in-built editor on WhatsApp, and sometimes the audio and video gets out of sync with subsequent cuts.

Another reason that made this project a bit more interesting and motivating to work on is that I also got to practice writing custom views on android. I wrote the [custom view class](https://github.com/echoeyecodes/ClipClip/blob/main/app/src/main/java/com/echoeyecodes/clipclip/customviews/videoselectionview/VideoSelectionView.kt) that allows users to select the start and end timestamps of the videos to split into parts. My first implementation of this months ago did work well, but the code was quite bloated in so many ways😅 and recently did a rewrite that's a lot better than the former. You can look through the commit logs and observe the differences.


##
I wrote an article [here](https://echoeyecodes.hashnode.dev/a-quick-rundown-of-the-major-improvements-made-to-my-video-snipping-tool?showSharer=true) on the challenges I faced with the custom selection view, and a problem with using foreground services causing the app to crash on Android 12+ devices. Apparently, WorkManager was the way out of it but still don't understand why it crashed on those devices since the service gets started while the application is active.

## How to build the project
There's no special setup necessary to run this on your machine. The FFMPEG kit library was installed via gradle, so all you need to do is clone and run the project. That's it!
```
implementation 'com.arthenica:ffmpeg-kit-full-gpl:4.5.1.LTS'
```

## Screenshots
![Screenshot 1](https://res.cloudinary.com/dfzhxlgll/image/upload/v1650459520/39487289q0oweikdjfxs_dopo49.jpg)
![Screenshot 2](https://res.cloudinary.com/dfzhxlgll/image/upload/v1650459520/28374829owierfj_p2vhor.png)
![Screenshot 3](https://res.cloudinary.com/dfzhxlgll/image/upload/v1650459521/0293874829iowefn_txaocv.png)
