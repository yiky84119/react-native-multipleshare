declare module "react-native-multipleshare" {
    enum Module { QQ = 0, WECHAT }
    enum Scene { SESSION = 0, TIMELINE }
    export default class MultipleShare {
        static share(array: Array<string>, module: Module, scene: Scene): Promise<boolean>;
    }
}
