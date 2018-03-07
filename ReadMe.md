##A64版ICU
###硬件设备
有两种类型设备：探视端ICUVisitMic和控制端ICUMaster

- 探视端，家属和病房使用，大屏幕，用于视频对讲。
- 控制端，医院护士使用，电话机尺寸，用于开启探视端设备，控制探视时间。

###通信方式
探视端和控制端与服务器的连接是通过Http。探视端和控制端的通信是TCP，探视端作为Server监听33334端口，控制端作为Client连接探视端。

###功能

探视端ICUVisitMic：需要编码摄像头 发送流和播放对方视频流

- 摄像头使用USB Camera，或者板载Camera 需要在设置页面配置。探视家属端使用外置麦克风，推车使用板载内置麦克风。
- USB Camera需要使用第三方usbcameralib库来获取视频帧，都使用android方式编码，发流使用libsendFrameInterface.so库，解码播放使用VLC，由底层开发封装成库。

- 音频使用曾辉修改的WebRtc,是一个名为talk的进程，执行音频采集 发送 接收 播放 等相关操作。上层应用仅负责开启 关闭，异常处理

控制端ICUMaster：开启探视端设备，控制探视时间，查看两者的Camera流，参与对话。


###核心类
- VideoTalkFragment 使用板载Camera的视频对讲页面
- USBCameraVideoTalkFragment 使用USB Camera的视频对讲页面
- TextureMovieEncoder是对摄像头视频帧编码的核心类
- 视频播放参考VideoTalkFragment::playVideoStream（）方法,