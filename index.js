import { NativeModules } from "react-native";

var RNMultipleShare = NativeModules.RNMultipleShare;

const Module = { QQ: 0, WECHAT: 1 };
const Scene = { SESSION: 0, TIMELINE: 1 };

function share(array: Array<string>, module: Module, scene: Scene): Promise<boolean> {
    return RNMultipleShare.share(array, module, scene);
}

module.exports = {
    Module,
    Scene,
    share
}
