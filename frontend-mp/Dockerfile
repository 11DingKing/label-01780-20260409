# 小程序源码为静态资源，运行时需使用微信开发者工具打开。
# 此镜像仅用于归档或 CI 打包；实际预览/真机调试请在本地用微信开发者工具打开 frontend-mp 目录。
FROM --platform=$TARGETPLATFORM alpine:3.18
WORKDIR /app
COPY . .
# 无 CMD：此项目不作为服务运行，仅作产物拷贝
CMD ["echo", "Use WeChat DevTools to open /app directory"]
