//
//  DownloadTask.h
//  RNMultipleShare
//
//  Created by Nevo on 2018/8/14.
//  Copyright © 2018 Nevo. All rights reserved.
//

#ifndef DownloadTask_h
#define DownloadTask_h
#import <Foundation/Foundation.h>

@interface DownloadTask : NSObject

-(instancetype)initWithData:(NSString*)url filename:(NSString*)path index:(int)index;

@property (nonatomic, strong) NSString *url;
@property (nonatomic, strong) NSString *path;
@property (nonatomic, assign) int index;

@end

#endif /* DownloadTask_h */
