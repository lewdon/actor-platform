//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/content/ServiceGroupUserAdded.java
//

#ifndef _AMServiceGroupUserAdded_H_
#define _AMServiceGroupUserAdded_H_

#include "J2ObjC_header.h"
#include "im/actor/model/entity/content/ServiceContent.h"

@class ImActorModelEntityContentInternalContentRemoteContainer;

@interface AMServiceGroupUserAdded : AMServiceContent

#pragma mark Public

- (instancetype)initWithImActorModelEntityContentInternalContentRemoteContainer:(ImActorModelEntityContentInternalContentRemoteContainer *)contentContainer;

+ (AMServiceGroupUserAdded *)createWithInt:(jint)uid;

- (jint)getAddedUid;

@end

J2OBJC_EMPTY_STATIC_INIT(AMServiceGroupUserAdded)

FOUNDATION_EXPORT AMServiceGroupUserAdded *AMServiceGroupUserAdded_createWithInt_(jint uid);

FOUNDATION_EXPORT void AMServiceGroupUserAdded_initWithImActorModelEntityContentInternalContentRemoteContainer_(AMServiceGroupUserAdded *self, ImActorModelEntityContentInternalContentRemoteContainer *contentContainer);

FOUNDATION_EXPORT AMServiceGroupUserAdded *new_AMServiceGroupUserAdded_initWithImActorModelEntityContentInternalContentRemoteContainer_(ImActorModelEntityContentInternalContentRemoteContainer *contentContainer) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMServiceGroupUserAdded)

typedef AMServiceGroupUserAdded ImActorModelEntityContentServiceGroupUserAdded;

#endif // _AMServiceGroupUserAdded_H_
