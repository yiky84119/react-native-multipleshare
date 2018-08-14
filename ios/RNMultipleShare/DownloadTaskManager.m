//
//  DownloadTaskManager.m
//  RNMultipleShare
//
//  Created by Nevo on 2018/8/14.
//  Copyright © 2018 Nevo. All rights reserved.
//

#include "DownloadTaskManager.h"

#define MAX_RUNNING_TASK 2
#define MaxShareImageCount 9

@implementation DownloadTaskManager

typedef void (^TaskManagerCompletionHandler)(NSMutableDictionary *result);


NSInteger mCount;
NSInteger mIndex;
NSInteger mHandledCount;
NSInteger mTaskLength;
NSMutableDictionary *mQueueResult;
TaskManagerCompletionHandler mTaskManagerCompletionHandler;

-(instancetype)init
{
    self = [super init];
    if (self) {
        NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
        _manager = [[AFURLSessionManager alloc] initWithSessionConfiguration:configuration];
        _taskArray = [NSMutableArray arrayWithCapacity: MaxShareImageCount];
        mCount = 0;
        mIndex = 0;
        mHandledCount = 0;
        mQueueResult = [NSMutableDictionary dictionaryWithCapacity: MaxShareImageCount];
    }
    return self;
}

-(void)append:(DownloadTask*) task {
    [_taskArray addObject:task];
}

-(void)start:(void (^)(NSMutableDictionary *result))completionHandler {
    mCount = 0;
    mIndex = 0;
    mHandledCount = 0;
    mTaskLength = _taskArray.count;
    [mQueueResult removeAllObjects];
    mTaskManagerCompletionHandler = completionHandler;
    [self handleNext];
}

-(void)handleNext {
    while (mCount < MAX_RUNNING_TASK && _taskArray.count > 0) {
        ++mCount;
        DownloadTask* task = [_taskArray objectAtIndex: 0];
        NSURLRequest *request = [NSURLRequest requestWithURL:[NSURL URLWithString:task.url]];
        
        NSURLSessionDownloadTask *downloadTask = [_manager downloadTaskWithRequest:request progress:nil destination:^NSURL *(NSURL *targetPath, NSURLResponse *response) {
            return [NSURL fileURLWithPath:task.path];
        } completionHandler:^(NSURLResponse *response, NSURL *filePath, NSError *error) {
            NSLog(@"File downloaded to: %@", filePath);
            if (error == nil) {
                [self next:filePath index:task.index];
            }
        }];
        [_taskArray removeObjectAtIndex: 0];
        [downloadTask resume];
    }
    
    if (mTaskLength == mHandledCount) {
        [_taskArray removeAllObjects];
        if (mTaskManagerCompletionHandler) {
            mTaskManagerCompletionHandler(mQueueResult);
        }
    }
}

-(void)next:(NSURL*) path index:(int)index {
    @synchronized (self) {
        mHandledCount++;
        mCount--;
        [mQueueResult setObject:path forKey:[NSString stringWithFormat:@"%d", index]];
        [self handleNext];
    }
}

@end
