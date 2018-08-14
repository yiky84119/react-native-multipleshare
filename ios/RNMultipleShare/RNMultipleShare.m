//
//  RNMultipleShare.m
//  RNMultipleShare
//
//  Created by Nevo on 2018/8/8.
//  Copyright © 2018 Nevo. All rights reserved.
//
#import <React/RCTLog.h>
#import <React/RCTConvert.h>

#import "RNMultipleShare.h"
#import "MultipleShareItem.h"
#include "DownloadTask.h"
#include "DownloadTaskManager.h"

#define KCompressibilityFactor 1280.00
#define MaxShareImageCount 9

@implementation RNMultipleShare

NSString* mName;
DownloadTaskManager* mTaskManager;

RCT_EXPORT_MODULE();

- (instancetype)init
{
    self = [super init];
    if (self) {
        mTaskManager = [[DownloadTaskManager alloc] init];
    }
    return self;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(share:(NSArray *)shareArray module:(NSInteger)module scene:(NSInteger)scene resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    
    UInt64 recordTime = [[NSDate date] timeIntervalSince1970]*1000;
    NSString* mPrefix = [NSString stringWithFormat:@"%lld_", recordTime];
    NSUInteger size = [shareArray count];
    NSMutableArray *mShareArray = [NSMutableArray arrayWithCapacity: MaxShareImageCount];
    NSMutableDictionary *mShare = [NSMutableDictionary dictionaryWithCapacity: MaxShareImageCount];
    
    NSString* cacheDirectory = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory,
                                                                   NSUserDomainMask,
                                                                   YES) lastObject];
    NSString *tempDirectory = [cacheDirectory stringByAppendingPathComponent:@"MShareCache"];
    
    NSFileManager *fileManager = [NSFileManager defaultManager];

    BOOL isDir = NO;
    BOOL existed = [fileManager fileExistsAtPath:tempDirectory isDirectory:&isDir];
    if (!(isDir == YES && existed == YES)) {
        [fileManager createDirectoryAtPath:tempDirectory withIntermediateDirectories:YES attributes:nil error:nil];
    } else {
        [self removeContentsOfDirectory:tempDirectory withExtension:nil ];
    }

    for (int i = 0; i < size && i < MaxShareImageCount; i++) {
        NSString *shareUrl = shareArray[i];
        if ([shareUrl hasPrefix:@"http"]) {
            NSString* name = [NSString stringWithFormat:@"%@_%d.jpg", mPrefix, i];
            NSString *fullPath = [tempDirectory stringByAppendingPathComponent: name];
            DownloadTask* task = [[DownloadTask alloc] initWithData:shareUrl filename:fullPath index: i];
            [mTaskManager append:task];
        } else {
            [mShare setObject:[NSURL fileURLWithPath:shareUrl] forKey:[NSString stringWithFormat:@"%d", i]];
        }
    }
    [mTaskManager start: ^(NSMutableDictionary *result) {
        NSLog(@"File downloaded");
        [mShare addEntriesFromDictionary:result];
        
        for (int i=0;i<mShare.count;i++) {
            NSURL* url = [mShare objectForKey:[NSString stringWithFormat:@"%d", i]];
            UIImage *imagerang = [UIImage imageWithContentsOfFile:[url path]];
            MultipleShareItem *item = [[MultipleShareItem alloc] initWithData:imagerang andFile:url];
            [mShareArray addObject:item];
        }
        
        UIActivityViewController *activityVC = [[UIActivityViewController alloc]initWithActivityItems:mShareArray applicationActivities:nil];
        UIViewController *rootViewController = [UIApplication sharedApplication].delegate.window.rootViewController;
        [rootViewController presentViewController:activityVC animated:TRUE completion:nil];
        resolve(@"test");
    }];
    

}

-(void)removeContentsOfDirectory:(NSString*)directory withExtension:(NSString*)extension
{
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSArray *contents = [fileManager contentsOfDirectoryAtPath:directory error:NULL];
    NSEnumerator *e = [contents objectEnumerator];
    NSString *filename;
    while ((filename = [e nextObject])) {
        if (extension != nil) {
            if ([[filename pathExtension] hasPrefix:extension]) {
                [fileManager removeItemAtPath:[directory stringByAppendingPathComponent:filename] error:NULL];
            }
        }else{
            [fileManager removeItemAtPath:[directory stringByAppendingPathComponent:filename] error:NULL];
        }
    }
}

#pragma mark - 压缩一张图片 最大宽高1280 类似于微信算法
- (UIImage *)getJPEGImagerImg:(UIImage *)image{
    CGFloat oldImg_WID = image.size.width;
    CGFloat oldImg_HEI = image.size.height;
    //CGFloat aspectRatio = oldImg_WID/oldImg_HEI;//宽高比
    if(oldImg_WID > KCompressibilityFactor || oldImg_HEI > KCompressibilityFactor){
        //超过设置的最大宽度 先判断那个边最长
        if(oldImg_WID > oldImg_HEI){
            //宽度大于高度
            oldImg_HEI = (KCompressibilityFactor * oldImg_HEI)/oldImg_WID;
            oldImg_WID = KCompressibilityFactor;
        }else{
            oldImg_WID = (KCompressibilityFactor * oldImg_WID)/oldImg_HEI;
            oldImg_HEI = KCompressibilityFactor;
        }
    }
    UIImage *newImg = [self imageWithImage:image scaledToSize:CGSizeMake(oldImg_WID, oldImg_HEI)];
    NSData *dJpeg = nil;
    if (UIImagePNGRepresentation(newImg)==nil) {
        dJpeg = UIImageJPEGRepresentation(newImg, 0.5);
    }else{
        dJpeg = UIImagePNGRepresentation(newImg);
    }
    return [UIImage imageWithData:dJpeg];
}
#pragma mark - 压缩多张图片 最大宽高1280 类似于微信算法
- (NSArray *)getJPEGImagerImgArr:(NSArray *)imageArr{
    NSMutableArray *newImgArr = [NSMutableArray new];
    for (int i = 0; i<imageArr.count; i++) {
        UIImage *newImg = [self getJPEGImagerImg:imageArr[i]];
        [newImgArr addObject:newImg];
    }
    return newImgArr;
}
#pragma mark - 压缩一张图片 自定义最大宽高
- (UIImage *)getJPEGImagerImg:(UIImage *)image compressibilityFactor:(CGFloat)compressibilityFactor{
    CGFloat oldImg_WID = image.size.width;
    CGFloat oldImg_HEI = image.size.height;
    //CGFloat aspectRatio = oldImg_WID/oldImg_HEI;//宽高比
    if(oldImg_WID > compressibilityFactor || oldImg_HEI > compressibilityFactor){
        //超过设置的最大宽度 先判断那个边最长
        if(oldImg_WID > oldImg_HEI){
            //宽度大于高度
            oldImg_HEI = (compressibilityFactor * oldImg_HEI)/oldImg_WID;
            oldImg_WID = compressibilityFactor;
        }else{
            oldImg_WID = (compressibilityFactor * oldImg_WID)/oldImg_HEI;
            oldImg_HEI = compressibilityFactor;
        }
    }
    UIImage *newImg = [self imageWithImage:image scaledToSize:CGSizeMake(oldImg_WID, oldImg_HEI)];
    NSData *dJpeg = nil;
    if (UIImagePNGRepresentation(newImg)==nil) {
        dJpeg = UIImageJPEGRepresentation(newImg, 0.5);
    }else{
        dJpeg = UIImagePNGRepresentation(newImg);
    }
    return [UIImage imageWithData:dJpeg];
}
#pragma mark - 压缩多张图片 自定义最大宽高
- (NSArray *)getJPEGImagerImgArr:(NSArray *)imageArr compressibilityFactor:(CGFloat)compressibilityFactor{
    NSMutableArray *newImgArr = [NSMutableArray new];
    for (int i = 0; i<imageArr.count; i++) {
        UIImage *newImg = [self getJPEGImagerImg:imageArr[i] compressibilityFactor:compressibilityFactor];
        [newImgArr addObject:newImg];
    }
    return newImgArr;
}
#pragma mark - 根据宽高压缩图片
- (UIImage *)imageWithImage:(UIImage *)image scaledToSize:(CGSize)newSize{
    UIGraphicsBeginImageContext(newSize);
    [image drawInRect:CGRectMake(0,0,newSize.width,newSize.height)];
    UIImage* newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return newImage;
}
@end
