####A64版ICU
ICUVisitMic 是探视端 需要编码摄像头 发送流和播放流
- 使用USB Camera，或者板载Camera 需要在设置页面配置 家属端使用Mic，推车使用板载音频
- Camera使用android方式编码，解码播放使用VLC
- 发流使用公司内部的libsendFrameInterface.so库 有待改进
- usbcameralib 使用USB Camera的库
- libvlc_new是播放自定义流的库
- 音频使用曾辉版的Talk ，所需库在系统lib中

ICUMaster 是控制端，开启家属端和探视端设备，控制探视时间，查看两者的Camera流，参与对话
