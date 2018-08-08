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

@implementation RNMultipleShare

NSString* mName;

RCT_EXPORT_MODULE();

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(share:(NSArray *)shareArray shareWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    NSStringArray* strArray = [RCTConvert NSStringArray:shareArray];
    
    resolve(@"test");
}
@end
