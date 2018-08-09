//
//  MultipleShareItem.h
//  RNMultipleShare
//
//  Created by Nevo on 2018/8/9.
//  Copyright © 2018 Nevo. All rights reserved.
//

#ifndef MultipleShareItem_h
#define MultipleShareItem_h

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>


@interface MultipleShareItem : NSObject<UIActivityItemSource>

-(instancetype)initWithData:(UIImage*)img andFile:(NSURL*)file;

@property (nonatomic, strong) UIImage *img;
@property (nonatomic, strong) NSURL *path;

@end

#endif /* MultipleShareItem_h */
