//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/Config.java
//


#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/Config.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "java/io/IOException.h"

@interface ImActorModelApiConfig () {
 @public
  jint maxGroupSize_;
}

@end

@implementation ImActorModelApiConfig

- (instancetype)initWithInt:(jint)maxGroupSize {
  ImActorModelApiConfig_initWithInt_(self, maxGroupSize);
  return self;
}

- (instancetype)init {
  ImActorModelApiConfig_init(self);
  return self;
}

- (jint)getMaxGroupSize {
  return self->maxGroupSize_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->maxGroupSize_ = [((BSBserValues *) nil_chk(values)) getIntWithInt:1];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeIntWithInt:1 withInt:self->maxGroupSize_];
}

- (NSString *)description {
  NSString *res = @"struct Config{";
  res = JreStrcat("$$", res, JreStrcat("$I", @"maxGroupSize=", self->maxGroupSize_));
  res = JreStrcat("$C", res, '}');
  return res;
}

@end

void ImActorModelApiConfig_initWithInt_(ImActorModelApiConfig *self, jint maxGroupSize) {
  (void) BSBserObject_init(self);
  self->maxGroupSize_ = maxGroupSize;
}

ImActorModelApiConfig *new_ImActorModelApiConfig_initWithInt_(jint maxGroupSize) {
  ImActorModelApiConfig *self = [ImActorModelApiConfig alloc];
  ImActorModelApiConfig_initWithInt_(self, maxGroupSize);
  return self;
}

void ImActorModelApiConfig_init(ImActorModelApiConfig *self) {
  (void) BSBserObject_init(self);
}

ImActorModelApiConfig *new_ImActorModelApiConfig_init() {
  ImActorModelApiConfig *self = [ImActorModelApiConfig alloc];
  ImActorModelApiConfig_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiConfig)
