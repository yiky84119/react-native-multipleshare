# react-native-multipleshare
支持分享多张图片到微信Wechat QQ 朋友圈，支持本地图片和网络图片

## Getting started

`$ npm install react-native-multipleshare --save`
or
`$ yarn add react-native-multipleshare`

### Mostly automatic installation

`$ react-native link react-native-multipleshare`

## Usage
```javascript
import MultipleShare from 'react-native-multipleshare';

let array = ['http://img4.tbcdn.cn/tfscom/i1/2259324182/TB2ISF_hKtTMeFjSZFOXXaTiVXa_!!2259324182.jpg',
  'https://gd2.alicdn.com/imgextra/i1/2259324182/TB2sdjGm0BopuFjSZPcXXc9EpXa_!!2259324182.jpg',
  'http://img2.tbcdn.cn/tfscom/i1/2259324182/TB2NAMmm00opuFjSZFxXXaDNVXa_!!2259324182.jpg',
  'file://yourlocalfilepath'];

//微信会话分享
await MultipleShare.share(array, MultipleShare.Module.WECHAT, MultipleShare.Scene.SESSION);

//微信朋友圈分享
await MultipleShare.share(array, MultipleShare.Module.WECHAT, MultipleShare.Scene.TIMELINE);

//QQ会话分享
await MultipleShare.share(array, MultipleShare.Module.QQ, MultipleShare.Scene.SESSION);

```
