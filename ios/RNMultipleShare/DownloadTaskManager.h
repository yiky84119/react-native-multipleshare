//
//  DownloadTaskManager.h
//  RNMultipleShare
//
//  Created by Nevo on 2018/8/14.
//  Copyright © 2018 Nevo. All rights reserved.
//

#ifndef DownloadTaskManager_h
#define DownloadTaskManager_h

#import <Foundation/Foundation.h>
#import "AFURLSessionManager.h"
#include "DownloadTask.h"

@interface DownloadTaskManager : NSObject

-(void)append:(DownloadTask*) task;
-(void)start:(void (^)(NSMutableDictionary *result))completionHandler;
@property (atomic, strong) AFURLSessionManager* manager;
@property (atomic, strong) NSMutableArray* taskArray;
@end

#endif /* DownloadTaskManager_h */
