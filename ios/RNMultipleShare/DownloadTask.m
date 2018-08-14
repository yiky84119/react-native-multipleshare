//
//  DownloadTask.m
//  RNMultipleShare
//
//  Created by Nevo on 2018/8/14.
//  Copyright © 2018 Nevo. All rights reserved.
//



#import "DownloadTask.h"

@implementation DownloadTask

-(instancetype)initWithData:(NSString*)url filename:(NSString*)path index:(int)index
{
    self = [super init];
    if (self) {
        _url = url;
        _path = path;
        self.index = index;
    }
    return self;
}

@end
